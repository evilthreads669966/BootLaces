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
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Inject
import javax.inject.Singleton

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
 * @date 10/16/20
 *
 * activates [WorkService]
 **/
@FlowPreview
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@Singleton
class WorkScheduler @Inject constructor(@ApplicationContext val ctx: Context, val alarmMgr: AlarmManager, val intentFactory: IntentFactory) {
    fun schedulePersistent(worker: Worker){
        val work = Work( worker.id, worker::class.java.name)
        sendWorkPersistent(work)
    }

    fun scheduleNow(worker: Worker){
        sendWorkAlarm(worker.toWork(), 0L, false, false)
    }

    fun scheduleFuture(delay: Long, worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), delay, repeating, wakeupIfIdle)
    }

    fun scheduleHour(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_HOUR, repeating, wakeupIfIdle)
    }

    fun scheduleQuarterDay(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_HOUR * 6, repeating, wakeupIfIdle)
    }

    fun scheduleHoursTwo(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_HOUR * 2, repeating, wakeupIfIdle)
    }

    fun scheduleHoursThree(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_HOUR * 3, repeating, wakeupIfIdle)
    }

    fun scheduleDay(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_DAY, repeating, wakeupIfIdle)
    }

    fun scheduleWeek(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_DAY * 7, repeating, wakeupIfIdle)
    }

    fun scheduleHalfWeek(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), (AlarmManager.INTERVAL_DAY * 3) + AlarmManager.INTERVAL_HALF_DAY, repeating, wakeupIfIdle)
    }

    fun scheduleHalfDay(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_HALF_DAY, repeating, wakeupIfIdle)
    }

    fun scheduleHalfHour(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_HALF_HOUR, repeating, wakeupIfIdle)
    }

    fun scheduleQuarterHour(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeating, wakeupIfIdle)
    }

    fun scheduleMonth(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        // TODO: 1/22/21 need to handle months with 28 days..don't remember what month(s) that is but this could cause a bug
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_DAY * 31, repeating, wakeupIfIdle)
    }

    fun scheduleYearly(worker: Worker, repeating: Boolean = false, wakeupIfIdle: Boolean = false){
        // TODO: 1/22/21 perhaps calculate remaining days left in the year...not really sure which to choose from as it would be a year from the day scheduled
        sendWorkAlarm(worker.toWork(), AlarmManager.INTERVAL_DAY * 365, repeating, wakeupIfIdle)

    }



    @FlowPreview
    @InternalCoroutinesApi
    private fun sendWorkAlarm(work: Work, interval: Long, repeating: Boolean, wakeupIfIdle: Boolean): Boolean{
        val pendingIntent = intentFactory.createPendingIntent(work) ?: return false
        var alarmTimeType: Int = AlarmManager.RTC
        val triggerTime = System.currentTimeMillis() + interval
        if(wakeupIfIdle)
            alarmTimeType = AlarmManager.RTC_WAKEUP
        if(repeating)
            alarmMgr.setRepeating(alarmTimeType, triggerTime, interval, pendingIntent)
        else
            if(wakeupIfIdle)
                alarmMgr.setExactAndAllowWhileIdle(alarmTimeType, triggerTime, pendingIntent)
            else
                alarmMgr.setExact(alarmTimeType, triggerTime, pendingIntent)
        return true
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @InternalCoroutinesApi
    internal fun sendWorkPersistent(work: Work){
        val intent = intentFactory.createWorkIntent(work, Actions.ACTION_WORK_PERSISTENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent)
        else
            ctx.startService(intent)
        WorkService.persist(ctx)
    }
}