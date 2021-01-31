
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
class WorkScheduler @Inject constructor(@ApplicationContext private val ctx: Context,private val alarmMgr: AlarmManager, private val factory: IntentFactory, private val dao: WorkDao) {
    /*use this scoping function to schedule workers
    * ie: scheduler.use { MyWorker().scheduleHour() }
    * */
    fun use(init: WorkScheduler.() -> Unit){
        init()
    }

    suspend fun PersistentWorker.schedulePersistent(): Deferred<Boolean> = coroutineScope{
        val result = async(Dispatchers.IO) {
            val work = Work(this@schedulePersistent)
            if (dao.insert(work).toInt() != work.id)
                return@async false
            if(!BootReceiver.isRebootEnabled(ctx))
                BootReceiver.enableReboot(ctx)
            return@async this@schedulePersistent.scheduleFuture()
        }
        return@coroutineScope result
    }

    internal fun PersistentWorker.scheduleFuture() = scheduleFuture(interval, repeating, allowWhileIdle, precisionTiming)

    fun Worker.scheduleNow(): Boolean = scheduleFuture(0L, false, false, false)

    fun Worker. scheduleFuture(delay: Long, repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(delay, repeating, allowWhileIdle, precision)

    fun Worker.scheduleHour(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HOUR, repeating, allowWhileIdle, precision)

    fun Worker.scheduleQuarterDay(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HOUR * 6, repeating, allowWhileIdle, precision)

    fun Worker.scheduleHoursTwo(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HOUR * 2, repeating, allowWhileIdle, precision)

    fun Worker.scheduleHoursThree(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HOUR * 3, repeating, allowWhileIdle, precision)

    fun Worker.scheduleDay(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_DAY, repeating, allowWhileIdle, precision)

    fun Worker.scheduleWeek(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_DAY * 7, repeating, allowWhileIdle, precision)

    fun Worker.scheduleHalfWeek(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule((AlarmManager.INTERVAL_DAY * 3) + AlarmManager.INTERVAL_HALF_DAY, repeating, allowWhileIdle, precision)

    fun Worker.scheduleHalfDay(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HALF_DAY, repeating, allowWhileIdle, precision)

    fun Worker.scheduleHalfHour(repeating: Boolean = false, wakeupIfIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_HALF_HOUR, repeating, wakeupIfIdle, precision)

    fun Worker.scheduleQuarterHour(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_FIFTEEN_MINUTES, repeating, allowWhileIdle, precision)

    // TODO: 1/22/21 need to handle months with 28 days..don't remember what month(s) that is but this could cause a bug
    fun Worker.scheduleMonth(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_DAY * 31, repeating, allowWhileIdle, precision)

    // TODO: 1/22/21 perhaps calculate remaining days left in the year...not really sure which to choose from as it would be a year from the day scheduled
    fun Worker.scheduleYearly(repeating: Boolean = false, allowWhileIdle: Boolean = false, precision: Boolean = false): Boolean =
        schedule(AlarmManager.INTERVAL_DAY * 365, repeating, allowWhileIdle, precision)


    private fun Worker.schedule(interval: Long, repeating: Boolean, allowWhileIdle: Boolean, precision: Boolean): Boolean{
        val work = Work(this)
        val pendingIntent = factory.createPendingIntent(work) ?: return false
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
        return true
    }
}