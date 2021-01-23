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

import android.app.AlarmManager
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ServiceCompat
import com.evilthreads.wakescopelib.suspendedWakeScope
import dagger.hilt.EntryPoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
@AndroidEntryPoint
class WorkService: Service(), ComponentCallbacks2,IWorkHandler<Worker, Flow<Work>, CoroutineScope> {
    @Inject internal lateinit var foregroundProvider: Provider<ForegroundComponent.Builder>
    @Inject internal lateinit var alarmMgr: AlarmManager
    @Inject lateinit var database: WorkDao
    @Inject internal lateinit var channel: Channel<Work>
    @Inject internal lateinit var supervisor: CompletableJob
    @Inject internal lateinit var mutex: Mutex
    @Inject internal lateinit var workers: MutableCollection<Worker>
    @Inject lateinit var intentFactory: IntentFactory
    private var startId: Int = -666
    private lateinit var scope: CoroutineScope
    private lateinit var foreground: ForegroundActivator
    private var workerCount: Int by Delegates.observable(0){_, _, newValue ->
        if(newValue == 0){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                foreground.deactivate()
            if(!stopSelfResult(startId))
                stopSelf()
        }
    }

    companion object{
        internal var state: ServiceState = ServiceState.STOPPED

        internal fun isStarted() = !state.equals(ServiceState.STOPPED)

        internal fun persist(ctx: Context){
            val componentName = ComponentName(ctx, BootReceiver::class.java)
            val state = ctx.packageManager.getComponentEnabledSetting(componentName)
            if(state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
                ctx.packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    override fun onCreate() {
        super.onCreate()
        state = ServiceState.BACKGROUND
        foreground = EntryPoints.get(foregroundProvider.get().build(),ForegroundEntryPoint::class.java).getForeground()
        scope = CoroutineScope(Dispatchers.Default + supervisor)
        scope.launch { suspendedWakeScope { handleWork() } }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runBlocking {
            scope.launch {
                val work = intent?.getParcelableExtra<Work>(Work.KEY_PARCEL) ?: return@launch
                when(intent.action ?: return@launch){
                    Actions.ACTION_WORK_PERSISTENT.action -> { withContext(Dispatchers.IO){ database.insert(work) } }
                    Actions.ACTION_WORK_NOW.action -> { channel.send(work) }
                    else -> return@launch
                }
            }.join()
        }
        this.startId = startId
        super.onStartCommand(intent, flags, this.startId)
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        Log.d("WorkService", "onDestroy()")
        supervisor.also { it.cancelChildren() }.cancel()
        channel.close()
        workers.forEach { worker -> worker.unregisterReceiver(this) }
        runBlocking {
            mutex.withLock {
                workers.clear()
                workerCount = 0
            }
        }
        if(state.equals(ServiceState.FOREGROUND))
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        state = ServiceState.STOPPED
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    override suspend fun Flow<Work>.processWork(scope: CoroutineScope){
        this.map { work -> work.toWorker() }
            .onEach { worker -> this@WorkService.scope.assignWork(worker) }
            .launchIn(scope)
    }

    override suspend fun CoroutineScope.handleWork(){
        val ioScope = CoroutineScope(this.coroutineContext + Dispatchers.IO)
        database.getPersistentWork().filterNotNull().processWork(ioScope)
        channel.consumeAsFlow().processWork(scope)
    }



    override suspend fun CoroutineScope.assignWork(worker: Worker) {
        if(workers.contains(worker)) return
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            if(!state.equals(ServiceState.FOREGROUND))
                foreground.activate()
        launch{
            with(worker){
                mutex.withLock {
                    workers.add(this)
                    workerCount++
                }
                val intent = intentFactory.createWorkNotificationIntent(this)
                if(withNotification == true)
                    NotificatonService.enqueue(this@WorkService, intent)
                registerReceiver(this@WorkService)
                doWork(this@WorkService)
                unregisterReceiver(this@WorkService)
                if(withNotification == true)
                    NotificatonService.enqueue(this@WorkService, intent.apply { setAction(
                        Actions.ACTION_FINISH.action) })
            }
            mutex.withLock { workerCount-- }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if(level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
            System.gc()
    }
}

internal interface IWorkHandler<in T, in S, in R>{
   suspend fun R.handleWork()
    suspend fun S.processWork(scope: R)
    suspend fun R.assignWork(worker: T)
}