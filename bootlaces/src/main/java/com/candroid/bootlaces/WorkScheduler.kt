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
@Singleton
class WorkScheduler @Inject constructor(@ApplicationContext private val ctx: Context,private val alarmMgr: AlarmManager, private val factory: IntentFactory) {
    /*use this scoping function to schedule workers
    * ie: scheduler.use { MyWorker().scheduleHour() }
    * */
    fun use(init: WorkScheduler.() -> Unit){
        init()
    }

    fun PersistentWorker.scheduleBeforeAfterReboot() = scheduleBeforeReboot()

    internal fun PersistentWorker.schedule() = scheduleFuture(interval, repeating, wakeIfIdle, precisionTiming)

    fun Worker.scheduleNow(): Boolean = scheduleFuture(0L, false, false, false)

    fun Worker.scheduleFuture(delay: Long, repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(delay, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleHour(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HOUR, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleQuarterDay(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HOUR * 6, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleHoursTwo(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HOUR * 2, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleHoursThree(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HOUR * 3, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleDay(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_DAY, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleWeek(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_DAY * 7, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleHalfWeek(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule((AlarmManager.INTERVAL_DAY * 3) + AlarmManager.INTERVAL_HALF_DAY, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleHalfDay(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HALF_DAY, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleHalfHour(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HALF_HOUR, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleQuarterHour(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeating, wakeupIfIdle, precision)

    // TODO: 1/22/21 need to handle months with 28 days..don't remember what month(s) that is but this could cause a bug
    fun Worker.scheduleMonth(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_DAY * 31, repeating, wakeupIfIdle, precision)

    // TODO: 1/22/21 perhaps calculate remaining days left in the year...not really sure which to choose from as it would be a year from the day scheduled
    fun Worker.scheduleYearly(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_DAY * 365, repeating, wakeupIfIdle, precision)

    private fun Worker.schedule(interval: Long, repeating: Boolean, wakeupIfIdle: Boolean, precision: Boolean): Boolean{
        val work = Work(this)
        val pendingIntent = factory.createPendingIntent(work) ?: return false
        var alarmTimeType: Int = AlarmManager.RTC
        val triggerTime = System.currentTimeMillis() + interval
        if(wakeupIfIdle)
            alarmTimeType = AlarmManager.RTC_WAKEUP
        if(repeating)
            alarmMgr.setRepeating(alarmTimeType, triggerTime, interval, pendingIntent)
        else if(wakeupIfIdle && precision)
            alarmMgr.setExactAndAllowWhileIdle(alarmTimeType, triggerTime, pendingIntent)
        else if(!wakeupIfIdle && precision)
            alarmMgr.setExact(alarmTimeType, triggerTime, pendingIntent)
        else if(wakeupIfIdle && !precision)
            alarmMgr.setAndAllowWhileIdle(alarmTimeType, triggerTime, pendingIntent)
        else
            alarmMgr.set(alarmTimeType, triggerTime, pendingIntent)
        return true
    }

    private fun PersistentWorker.scheduleBeforeReboot(){

        val work = Work(this)
        val intent = factory.createWorkIntent(work, Actions.ACTION_SCHEDULE_BEFORE_REBOOT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent)
        else
            ctx.startService(intent)
        if(!BootReceiver.isRebootEnabled(ctx)) BootReceiver.enableReboot(ctx)
    }
}