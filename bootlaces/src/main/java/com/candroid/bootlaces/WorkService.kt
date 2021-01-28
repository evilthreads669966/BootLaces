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
import android.os.Build
import android.util.Log
import androidx.core.app.ServiceCompat
import dagger.hilt.EntryPoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
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
 * */
@AndroidEntryPoint
class WorkService: Service(), ComponentCallbacks2 {
    @Inject internal lateinit var foregroundProvider: Provider<ForegroundComponent.Builder>
    @Inject internal lateinit var alarmMgr: AlarmManager
    @Inject internal lateinit var database: WorkDao
    @Inject internal lateinit var mutex: Mutex
    @Inject internal lateinit var workers: MutableCollection<Worker>
    @Inject internal lateinit var intentFactory: IntentFactory
    @Inject internal lateinit var scheduler: WorkScheduler
    @Inject internal lateinit var workDispatcher: ExecutorCoroutineDispatcher
    private lateinit var foreground: ForegroundActivator
    private lateinit var workCoroutineScope: CoroutineScope
    private var startId: Int? = null
    private var workerCount: Int by Delegates.observable(0){_, _, newValue ->
        if(newValue == 0) stopWorkService()
    }

    companion object{
        internal var state: ServiceState = ServiceState.STOPPED

        internal fun isStarted() = !state.equals(ServiceState.STOPPED)
    }

    private fun stopWorkService(){
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
        workCoroutineScope = CoroutineScope(workDispatcher)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        workCoroutineScope.launch { startAction(intent) }
        super.onStartCommand(intent, flags, startId)
        this.startId = startId
        return START_NOT_STICKY
    }

    private suspend fun startAction(intent: Intent?){
        val work = intent?.getParcelableExtra<Work>(Work.KEY_PARCEL) ?: return
        when(intent.action ?: return){
            Actions.ACTION_SCHEDULE_BEFORE_REBOOT.action -> {
                database.insert(work)
                scheduleWorkBeforeAfterReboot()
            }
            Actions.ACTION_SCHEDULE_AFTER_REBOOT.action -> scheduleWorkBeforeAfterReboot()
            Actions.ACTION_EXPIRED_WORK.action -> processExpiredWork(Worker.createFromWork(work))
            else -> return
        }
    }

    override fun onDestroy() {
        Log.d("WorkService", "onDestroy()")
        startId = 0
        workCoroutineScope.cancel()
        workDispatcher.cancel()
        workDispatcher.close()
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

    private suspend fun Flow<List<Work>>.scheduleBeforeAndAfterReboot() = supervisorScope{
        filterNotNull()
            .flatMapMerge(DEFAULT_CONCURRENCY) {
                flow {
                    emit(it.map { Worker.createFromWork(it) })
                }
            }
            .onEach {
                it.filterIsInstance<PersistentWorker>()
                    .forEach { worker ->
                        scheduler.use {
                            worker.scheduleAfterReboot()
                        }
                    }
            }.flowOn(currentCoroutineContext())
            .launchIn(this)
    }

    private suspend fun scheduleWorkBeforeAfterReboot() = database.getPersistentWork().scheduleBeforeAndAfterReboot()


    private suspend fun processExpiredWork(worker: Worker) = supervisorScope{
        if(workers.contains(worker)) return@supervisorScope
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            if(!state.equals(ServiceState.FOREGROUND))
                foreground.activate()
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
                NotificatonService.enqueue(this@WorkService, intent.apply { setAction(Actions.ACTION_FINISH.action) })
        }
        mutex.withLock { workerCount-- }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if(level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
            System.gc()
    }
}
