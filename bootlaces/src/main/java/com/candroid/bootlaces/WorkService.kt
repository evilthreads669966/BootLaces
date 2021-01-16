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
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import androidx.core.app.ServiceCompat
import dagger.hilt.EntryPoints
import dagger.hilt.android.AndroidEntryPoint
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
@AndroidEntryPoint
class WorkService(): Service() {
    @Inject lateinit var provider: Provider<ForegroundComponent.Builder>
    @Inject lateinit var alarmMgr: AlarmManager
    @Inject lateinit var database: WorkDao
    @Inject lateinit var channel: Channel<Work>
    @Inject lateinit var supervisor: CompletableJob
    private lateinit var scope: CoroutineScope
    private lateinit var foreground: ForegroundActivator
    private val workers = Collections.synchronizedSet(mutableSetOf<Worker>())
    private var workerCount: Int by Delegates.observable(0){_, _, newValue ->
        if(newValue == 0){
            foreground.deactivate()
            stopSelf()
        }
    }

    companion object{
        var state: ServiceState = ServiceState.STOPPED
        fun isStarted() = !state.equals(ServiceState.STOPPED)
    }

    override fun onCreate() {
        super.onCreate()
        state = ServiceState.BACKGROUND
        foreground = EntryPoints.get(provider.get().build(),ForegroundEntryPoint::class.java).getActivator()
        scope = CoroutineScope(Dispatchers.Default + supervisor)
        scope.launch { handleWork() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.run {
            if(!hasExtra(Work.KEY_PARCEL)) return@run
            if(action == null) return@run
            val work = getParcelableExtra<Work>(Work.KEY_PARCEL) ?: return@run
            scope.launch {
                when(intent.action){
                    Actions.ACTION_WORK_PERSISTENT.action -> { withContext(Dispatchers.IO){ database.insert(work) } }
                    Actions.ACTION_WORK_ONE_TIME.action -> { channel.send(work) }
                    Actions.ACTION_WORK_PERIODIC.action -> { channel.send(work) }
                    Actions.ACTION_WORK_FUTURE.action -> {
                        channel.send(work)
                        withContext(Dispatchers.IO){ database.delete(work) }
                    }
                    else -> return@launch
                }
            }
        }
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        supervisor.also { it.cancelChildren() }.cancel()
        channel.close()
        workers.forEach { it.unregisterWorkReceiver() }
        workers.clear()
        if(state.equals(ServiceState.FOREGROUND))
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        state = ServiceState.STOPPED
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private suspend fun Flow<Work>.processWorkRequests(scope: CoroutineScope){
        this.map { work -> work.toWorker() }
            .onEach { worker -> this@WorkService.scope.assignWorker(worker) }
            .launchIn(scope)
    }

    private suspend fun CoroutineScope.handleWork(){
        val ioScope = CoroutineScope(this@handleWork.coroutineContext + Dispatchers.IO)
        with(database){
            getPersistentWork().filterNotNull().processWorkRequests(ioScope)

            getPeriodicWork().filterNotNull().onEach {
                preparePendingWork(it).run {
                    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + it.interval!!, it.interval!!, this)
                }
            }.launchIn(ioScope)

            getFutureWork().filterNotNull().onEach {
                preparePendingWork(it).run { alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, it.delay!!, this) }
            }.launchIn(ioScope)

            getSpecificPeriodicWork().filterNotNull().onEach { work ->
                preparePendingWork(work).run {
                    var interval = 0L
                    if(work.hourly == true)
                        interval = AlarmManager.INTERVAL_HOUR
                    else if(work.daily == true)
                        interval = AlarmManager.INTERVAL_DAY
                    else if(work.weekly == true)
                        interval = AlarmManager.INTERVAL_DAY * 7
                    else if(work.monthly == true)
                        interval = AlarmManager.INTERVAL_DAY * 31
                    else if(work.yearly == true)
                        interval = AlarmManager.INTERVAL_DAY * 365
                    else
                        return@run
                    alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 60000, interval, this)
                }
            }.launchIn(ioScope)
        }
        channel.consumeAsFlow().processWorkRequests(scope)
    }
    private fun preparePendingWork(work: Work): PendingIntent {
        val intent = Intent().apply {
            setClass(this@WorkService, BootReceiver::class.java)
            putExtra(Work.KEY_PARCEL, work)
        }
        return PendingIntent.getBroadcast(this@WorkService, work.id, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    @InternalCoroutinesApi
    private suspend fun CoroutineScope.assignWorker(worker: Worker){
        if(workers.contains(worker)) return
        if(!state.equals(ServiceState.FOREGROUND)) foreground.activate()
        launch{
            workers -= worker.apply {
                workerCount = workers.also { it += this }.size
                val intent = IntentFactory.createWorkNotificationIntent(this)
                if(withNotification == true)
                    NotificatonService.enqueue(this@WorkService, intent)
                registerWorkReceiver()
                doWork(this@WorkService)
                unregisterWorkReceiver()
                if(withNotification == true)
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