/*Copyright 2019 Chris Basinger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.candroid.bootlaces

import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import dagger.hilt.EntryPoints
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.properties.Delegates

/*
            (   (                ) (             (     (
            )\ ))\ )    *   ) ( /( )\ )     (    )\ )  )\ )
 (   (   ( (()/(()/(  ` )  /( )\()|()/((    )\  (()/( (()/(
 )\  )\  )\ /(_))(_))  ( )(_)|(_)\ /(_))\((((_)( /(_)) /(_))
((_)((_)((_|_))(_))   (_(_()) _((_|_))((_))\ _ )(_))_ (_))
| __\ \ / /|_ _| |    |_   _|| || | _ \ __(_)_\(_)   \/ __|
| _| \ V /  | || |__    | |  | __ |   / _| / _ \ | |) \__ \
|___| \_/  |___|____|   |_|  |_||_|_|_\___/_/ \_\|___/|___/
....................../´¯/)
....................,/¯../
.................../..../
............./´¯/'...'/´¯¯`·¸
........../'/.../..../......./¨¯\
........('(...´...´.... ¯~/'...')
.........\.................'...../
..........''...\.......... _.·´
............\..............(
..............\.............\...
*/
/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * activates [Worker]
 * */
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@FlowPreview
@ForegroundScope
abstract class WorkService: LifecycleService() {
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    private var startId: Int = 0
    @Inject lateinit var provider: Provider<ForegroundComponent.Builder>
    @Inject lateinit var channel: Channel<Work>
    private lateinit var foreground: ForegroundActivator
    private val workers = Collections.synchronizedSet(mutableSetOf<Worker>())
    private var workerCount: Int by Delegates.observable(0){property, oldValue, newValue ->
        if(newValue == 0) foreground.deactivate()
    }

    init {
        lifecycle.addObserver(BootServiceState)
        lifecycleScope.launchWhenCreated { handleWork() }
    }

    override fun getLifecycle() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        foreground = EntryPoints.get(provider.get().build(),ForegroundEntryPoint::class.java).getActivator()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mDispatcher.onServicePreSuperOnStart()
        super.onStartCommand(intent, flags, startId)
        this.startId = startId
        return START_STICKY
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        if(BootServiceState.isForeground())
            ServiceCompat.stopForeground(this,ServiceCompat.STOP_FOREGROUND_REMOVE)
        foreground.scope.also { it.coroutineContext.cancelChildren() }.cancel()
        channel.close()
        workers.filterNot { it.receiver == null }.forEach { unregisterReceiver(it.receiver) }
        workers.clear()
        lifecycle.removeObserver(BootServiceState)
        stopSelfResult(startId)
        super.onDestroy()
    }

    private suspend fun Flow<Work>.processWorkRequests(){
        this.filterNotNull()
            .map { it.toWorker() }
            .onEach { worker -> assignWorker(foreground.scope, worker) }
            .launchIn(foreground.scope)
    }

    private suspend fun handleWork(){
        withContext(Dispatchers.IO) {
            foreground.database.getAll().processWorkRequests()
        }
        withContext(Dispatchers.Default){
            channel.consumeAsFlow().processWorkRequests()
        }
    }

    @InternalCoroutinesApi
    private suspend fun assignWorker(coroutineScope: CoroutineScope, worker: Worker){
        if(workers.contains(worker)) return
        if(!BootServiceState.isForeground()) foreground.activate()
        coroutineScope.launch{
            workers -= worker.apply {
                workers += this
                workerCount++
                val intent = IntentFactory.createWorkNotificationIntent(this)
                NotificatonService.enqueue(this@WorkService, intent)
                registerWorkReceiver()
                doWork(this@WorkService)
                unregisterWorkReceiver()
                NotificatonService.enqueue(this@WorkService, intent.apply { setAction(Actions.ACTION_FINISH.action) })
            }
            workerCount--
        }
    }

    private fun Worker.unregisterWorkReceiver() = this.receiver?.let { unregisterReceiver(it) }

    private fun Worker.registerWorkReceiver(){
        this.receiver?.run {
            val filter = IntentFilter(this.action)
            registerReceiver(this, filter)
        }
    }
}