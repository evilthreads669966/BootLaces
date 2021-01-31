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

import android.app.Service
import android.content.*
import android.os.Build
import android.util.Log
import androidx.core.app.ServiceCompat
import dagger.hilt.EntryPoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.CoroutineContext
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
 * */
@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class WorkService: Service(), ComponentCallbacks2 {
    @Inject internal lateinit var foregroundProvider: Provider<ForegroundComponent.Builder>
    @Inject internal lateinit var intentFactory: IntentFactory
    @Inject internal lateinit var workSchedulerFacade: WorkShedulerFacade
    @Inject internal lateinit var mutex: Mutex
    @Inject internal lateinit var supervisor: CoroutineScope
    private lateinit var foreground: ForegroundActivator
    private var startId: Int? = null
    private val receivers = mutableListOf<BroadcastReceiver>()

    private var workerCount: Int by Delegates.observable(0) { _, _, newValue ->
        if (newValue == 0)
            stopWorkService()
    }

    companion object{
        internal var state: ServiceState = ServiceState.STOPPED
        internal fun isStarted() = !state.equals(ServiceState.STOPPED)
    }

    internal fun stopWorkService(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            if(state == ServiceState.FOREGROUND)
                foreground.deactivate()
        if(startId != null)
            stopSelfResult(startId!!)
        else
            stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        state = ServiceState.BACKGROUND
        foreground = EntryPoints.get(foregroundProvider.get().build(),ForegroundEntryPoint::class.java).getForeground()
        foreground.activate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        supervisor.launch { handleRequest(intent) }
        super.onStartCommand(intent, flags, startId)
        this.startId = startId
        return START_NOT_STICKY
    }

    private suspend fun handleRequest(intent: Intent?) = coroutineScope{
            val work: Work? = intent?.getParcelableExtra(Work.KEY_PARCEL)
            when (intent?.action ?: return@coroutineScope ) {
                Actions.ACTION_SCHEDULE_REBOOT.action -> workSchedulerFacade.scheduleWorkForReboot(work!!, this)
                Actions.ACTION_EXECUTE_WORKER.action -> work?.executeWorker(this)
                else -> return@coroutineScope
            }
        }

    override fun onDestroy() {
        supervisor.coroutineContext.cancelChildren()
        supervisor.cancel()
        startId = 0
        runBlocking {
            mutex.withLock {
                receivers.clear()
                workerCount = 0
            }
        }
        receivers.forEach { unregisterReceiver(it) }
        if(state.equals(ServiceState.FOREGROUND))
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        state = ServiceState.STOPPED
        super.onDestroy()
    }

    private suspend fun Work.executeWorker(scope: CoroutineScope){
        val worker = Worker.createFromWork(this)
        mutex.withLock {
            worker.receiver?.let { receivers.add(it) }
            workerCount++
        }
        val intent = intentFactory.createWorkNotificationIntent(worker)
        if(worker.withNotification == true)
            NotificatonService.enqueue(this@WorkService, intent)
        worker.registerReceiver(this@WorkService)
        worker.doWork(this@WorkService)
        worker.unregisterReceiver(this@WorkService)
        if(worker.withNotification == true)
            NotificatonService.enqueue(this@WorkService, intent.apply { setAction(Actions.ACTION_FINISH.action) })
        mutex.withLock { workerCount-- }
    }

    override fun onBind(intent: Intent?) = null

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if(level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
            System.gc()
    }
}