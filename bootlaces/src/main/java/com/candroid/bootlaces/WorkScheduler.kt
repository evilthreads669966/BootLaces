
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

    suspend fun Worker.scheduleNow(): Deferred<Boolean> = schedule(0L, false, false, true, true)

    suspend fun Worker. scheduleFuture(delay: Long, surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(delay, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHour(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HOUR, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleQuarterDay(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY / 4, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHoursTwo(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HOUR * 2, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHoursThree(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HOUR * 3, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleDay(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleWeek(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY * 7, surviveReboot,repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHalfWeek(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(((AlarmManager.INTERVAL_DAY * 3.5)).toLong() + AlarmManager.INTERVAL_HALF_DAY, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHalfDay(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HALF_DAY, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleHalfHour(surviveReboot: Boolean = false, repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_HALF_HOUR, surviveReboot, repeating, wakeupIfIdle, precision)

    suspend fun Worker.scheduleQuarterHour(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_FIFTEEN_MINUTES, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleMonth(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY * 31, surviveReboot, repeating, allowWhileIdle, precision)

    suspend fun Worker.scheduleYear(surviveReboot: Boolean = false, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Deferred<Boolean> =
        schedule(AlarmManager.INTERVAL_DAY * 365, surviveReboot, repeating, allowWhileIdle, precision)


    private suspend fun Worker.schedule(interval: Long, surviveReboot: Boolean, repeating: Boolean, allowWhileIdle: Boolean, precision: Boolean): Deferred<Boolean> = coroutineScope{
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