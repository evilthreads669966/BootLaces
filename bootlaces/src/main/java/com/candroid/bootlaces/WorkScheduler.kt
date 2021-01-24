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
 **/
@FlowPreview
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@Singleton
class WorkScheduler @Inject constructor(@ApplicationContext private val ctx: Context,private val alarmMgr: AlarmManager, private val factory: IntentFactory) {
    /*use this scoping function to schedule workers
    * ie: scheduler.use { MyWorker().scheduleHour() }
    * */
    fun use(init: WorkScheduler.() -> Unit){
        init()
    }

    fun Worker.schedulePersistent() = schedule(ctx, factory)

    fun Worker.scheduleNow(): Boolean = schedule(factory, alarmMgr, 0L, false, false)

    fun Worker.scheduleFuture(delay: Long, repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, delay, repeating, wakeupIfIdle)

    fun Worker.scheduleHour(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_HOUR, repeating, wakeupIfIdle)

    fun Worker.scheduleQuarterDay(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_HOUR * 6, repeating, wakeupIfIdle)

    fun Worker.scheduleHoursTwo(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_HOUR * 2, repeating, wakeupIfIdle)

    fun Worker.scheduleHoursThree(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_HOUR * 3, repeating, wakeupIfIdle)

    fun Worker.scheduleDay(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_DAY, repeating, wakeupIfIdle)

    fun Worker.scheduleWeek(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_DAY * 7, repeating, wakeupIfIdle)

    fun Worker.scheduleHalfWeek(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, (AlarmManager.INTERVAL_DAY * 3) + AlarmManager.INTERVAL_HALF_DAY, repeating, wakeupIfIdle)

    fun Worker.scheduleHalfDay(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_HALF_DAY, repeating, wakeupIfIdle)

    fun Worker.scheduleHalfHour(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_HALF_HOUR, repeating, wakeupIfIdle)

    fun Worker.scheduleQuarterHour(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeating, wakeupIfIdle)

    // TODO: 1/22/21 need to handle months with 28 days..don't remember what month(s) that is but this could cause a bug
    fun Worker.scheduleMonth(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_DAY * 31, repeating, wakeupIfIdle)

    // TODO: 1/22/21 perhaps calculate remaining days left in the year...not really sure which to choose from as it would be a year from the day scheduled
    fun Worker.scheduleYearly(repeating: Boolean = false, wakeupIfIdle: Boolean = false): Boolean =
        schedule(factory, alarmMgr, AlarmManager.INTERVAL_DAY * 365, repeating, wakeupIfIdle)

    private fun Worker.schedule(factory: IntentFactory, alarmMgr: AlarmManager, interval: Long, repeating: Boolean, wakeupIfIdle: Boolean): Boolean{
        val pendingIntent = factory.createPendingIntent(this.toWork()) ?: return false
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

    private fun Worker.schedule(ctx: Context, factory: IntentFactory){
        val intent = factory.createWorkIntent(this.toWork(), Actions.ACTION_WORK_PERSISTENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent)
        else
            ctx.startService(intent)
        WorkService.persist(ctx)
    }
}