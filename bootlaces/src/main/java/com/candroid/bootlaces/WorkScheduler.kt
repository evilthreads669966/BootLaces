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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*
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
class WorkScheduler @Inject constructor(@ApplicationContext private val ctx: Context, private val alarmMgr: AlarmManager, private val factory: IntentFactory, private val dao: WorkDao) {
    /*use this scoping function to schedule workers
    * ie: scheduler.use { MyWorker().scheduleHour() }
    * */
    fun use(init: WorkScheduler.() -> Unit) = init()

    suspend fun BackgroundReceiver.scheduleNow(surviveReboot: Boolean = false): Deferred<Boolean> = schedule(0L, null, surviveReboot, false, true, true)

    suspend fun Worker.scheduleNow(surviveReboot: Boolean = false): Deferred<Boolean> = schedule(0L, null, surviveReboot, false, true, true)

    suspend fun Worker. scheduleFuture(delay: Long, surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(delay, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHour(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HOUR, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleQuarterDay(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY / 4, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHoursTwo(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HOUR * 2, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHoursThree(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HOUR * 3, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleDay(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleWeek(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY * 7, null, surviveReboot,repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHalfWeek(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(((AlarmManager.INTERVAL_DAY * 3.5)).toLong() + AlarmManager.INTERVAL_HALF_DAY, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHalfDay(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HALF_DAY, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHalfHour(surviveReboot: Boolean = false, repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HALF_HOUR, null, surviveReboot, repeating, wakeupIfIdle, precision)

    suspend fun Worker.scheduleQuarterHour(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_FIFTEEN_MINUTES, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleMonth(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY * 31, null, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleYear(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY * 365, null, surviveReboot, repeating, allowWhileIdle, precision)

 /*   suspend fun Worker.scheduleByHourOfToday(hourOfDay: Int, minuteOfHour: Int , allowWhileIdle: Boolean, precision: Boolean): Deferred<Boolean>{
        val millis = Calendar.getInstance(TimeZone.getDefault()).run {
            set(get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DATE), hourOfDay, minuteOfHour)
            timeInMillis
        }
        return schedule(millis, surviveReboot = true, repeating = false, allowWhileIdle = allowWhileIdle, precision = precision)
    }

    suspend fun Worker.scheduleByDayOfWeek( weekOfYear: Int? = null, dayOfWeek: Int, hourOfDay: Int, minuteOfHour: Int, allowWhileIdle: Boolean, precision: Boolean): Deferred<Boolean>{
        val millis = Calendar.getInstance(TimeZone.getDefault()).run {
            setWeekDate(get(Calendar.YEAR), weekOfYear ?: get(Calendar.WEEK_OF_YEAR), dayOfWeek)
            timeInMillis + (hourOfDay * 60 * 1000 + minuteOfHour * 1000 )
        }
        return schedule(millis, surviveReboot = true, repeating = false, allowWhileIdle = allowWhileIdle, precision = precision)
    }

    suspend fun Worker.scheduleByDate(year: Int? = null, month: Int? = null, dayOfMonth: Int, hourOfDay: Int? = null, minuteOfHour: Int? = null, allowWhileIdle: Boolean, precision: Boolean): Deferred<Boolean>{
        val millis = Calendar.getInstance(TimeZone.getDefault()).run {
            set(year ?: get(Calendar.YEAR), month ?: get(Calendar.MONTH), dayOfMonth, hourOfDay ?: 0, minuteOfHour ?: 0)
            timeInMillis
        }
        return schedule(millis, dayOfMonth, surviveReboot = true, repeating = false, allowWhileIdle = allowWhileIdle, precision = precision)
    }*/

    private suspend fun Worker.schedule(interval: Long, dayOfMonth: Int? = null, surviveReboot: Boolean, repeating: Boolean, allowWhileIdle: Boolean, precision: Boolean): Deferred<Boolean> = coroutineScope{
        async {
            lateinit var work: Work
            if(surviveReboot){
                work = Work(this@schedule, interval, repeating, allowWhileIdle, precision)
                if (dao.insert(work).toInt() != work.id)
                    return@async false
                if(!Utils.isRebootEnabled(ctx))
                    Utils.enableReboot(ctx)
            }
            else
                work = Work(this@schedule)
            val pendingIntent = factory.createPendingIntent(work) ?: return@async false
            var alarmTimeType: Int = AlarmManager.RTC
            val triggerTime = System.currentTimeMillis() + interval
            if(allowWhileIdle)
                alarmTimeType = AlarmManager.RTC_WAKEUP
            if(repeating)
                alarmMgr.setRepeating(alarmTimeType, triggerTime, interval, pendingIntent)
            else if(allowWhileIdle && precision)
                alarmMgr.setExactAndAllowWhileIdle(alarmTimeType, triggerTime, pendingIntent)
            else if(!allowWhileIdle && precision)
                alarmMgr.setExact(alarmTimeType, triggerTime, pendingIntent)
            else if(allowWhileIdle && !precision)
                alarmMgr.setAndAllowWhileIdle(alarmTimeType, triggerTime, pendingIntent)
            else
                alarmMgr.set(alarmTimeType, triggerTime, pendingIntent)
            return@async true
        }
    }
}