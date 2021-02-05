
package com.candroid.bootlaces
import android.app.AlarmManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
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

    suspend fun PersistentReceiver.scheduleReceiver(): Deferred<Boolean> = schedule(0L, true, false, true, true)

    suspend fun Worker.scheduleNow(persistent: Boolean = false): Deferred<Boolean> = schedule(0L, false, false, true, true)

    suspend fun Worker. scheduleFuture(delay: Long, persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(delay, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHour(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HOUR, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleQuarterDay(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY / 4, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHoursTwo(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HOUR * 2, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHoursThree(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HOUR * 3, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleDay(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleWeek(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY * 7, persistent,repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHalfWeek(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(((AlarmManager.INTERVAL_DAY * 3.5)).toLong() + AlarmManager.INTERVAL_HALF_DAY, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHalfDay(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HALF_DAY, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHalfHour(persistent: Boolean = false, repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HALF_HOUR, persistent, repeating, wakeupIfIdle, precision)

    suspend fun Worker.scheduleQuarterHour(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_FIFTEEN_MINUTES, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleMonth(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY * 31, persistent, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleYear(persistent: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY * 365, persistent, repeating, allowWhileIdle, precision)


    private suspend fun Worker.schedule(interval: Long, persistent: Boolean, repeating: Boolean, allowWhileIdle: Boolean, precision: Boolean): Deferred<Boolean> = coroutineScope{
        async {
            lateinit var work: Work
            if(persistent){
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