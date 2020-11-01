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

import android.content.IntentFilter
import androidx.lifecycle.lifecycleScope
import dagger.hilt.EntryPoints
import kotlinx.coroutines.*
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
abstract class WorkService: BaseLifecycleService() {
    @Inject lateinit var provider: Provider<ForegroundComponent.Builder>
    private lateinit var foreground: ForegroundActivator
    private val workers = Collections.synchronizedSet(mutableSetOf<Worker>())
    private var workerCount: Int by Delegates.observable(0){property, oldValue, newValue ->
        if(newValue == 0){
            foreground.deactivate()
        }
    }

    init {
        lifecycleScope.launchWhenCreated { handleWork() }
    }

    override fun onCreate() {
        super.onCreate()
        foreground = EntryPoints.get(provider.get().build(),ForegroundEntryPoint::class.java).getActivator()
    }

    override fun onDestroy() {
        foreground.scope.also { it.coroutineContext.cancelChildren() }.cancel()
        foreground.channel.close()
        workers.forEach { it.unregisterWorkReceiver() }
        workers.clear()
        super.onDestroy()
    }

    private suspend fun Flow<Work>.processWorkRequests(){
        this.map { it.toWorker() }
            .onEach { worker -> assignWorker(foreground.scope, worker) }
            .launchIn(foreground.scope)
    }

    private suspend fun handleWork(){
        withContext(Dispatchers.IO) {
            foreground.database.getAll().filterNotNull().processWorkRequests()
        }
        withContext(Dispatchers.Default){
            foreground.channel.consumeAsFlow().processWorkRequests()
        }
    }

    @InternalCoroutinesApi
    private suspend fun assignWorker(coroutineScope: CoroutineScope, worker: Worker){
        if(workers.contains(worker)) return
        if(!BootServiceState.isForeground()) foreground.activate()
        coroutineScope.launch{
            workers -= worker.apply {
                workerCount = workers.also { it += this }.size
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