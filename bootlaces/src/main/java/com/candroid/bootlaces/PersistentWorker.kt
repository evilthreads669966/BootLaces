package com.candroid.bootlaces

import android.app.AlarmManager

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
 * @date 1/28/20
 *
 * */
abstract class PersistentWorker(
    id: Int,
    withNotification: Boolean = false,
    description: String,
    open val interval: Long,
    val allowWhileIdle: Boolean = false,
    val precisionTiming: Boolean = false,
    open val repeating: Boolean,
): Worker(id, description, withNotification)

abstract class PersistentWorkerDaily : PersistentWorker{
    constructor(id: Int, description: String, withNotification: Boolean = false)
            : super(id, withNotification, description, AlarmManager.INTERVAL_HOUR*24, true, true, true)
}

abstract class PersistentWorkerHourly(id: Int, description: String, withNotification: Boolean = false)
    : PersistentWorker(id, true, description, AlarmManager.INTERVAL_HOUR, true, true, true)

abstract class PersistentWorkerHalfDay(id: Int, description: String, withNotification: Boolean = false)
    : PersistentWorker(id, true, description, AlarmManager.INTERVAL_DAY/2, true, true, true)

abstract class PersistentWorkerQuarterHourly(id: Int, description: String, withNotification: Boolean = false)
    : PersistentWorker(id, true, description, AlarmManager.INTERVAL_HOUR/4, true, true, true)

abstract class PersistentWorkerHalfHourly(id: Int, description: String, withNotification: Boolean = false)
    : PersistentWorker(id, true, description, AlarmManager.INTERVAL_HOUR/2, true, true, true)

abstract class PersistentWorkerWeekly(id: Int, description: String, withNotification: Boolean = false)
    : PersistentWorker(id, true, description, AlarmManager.INTERVAL_DAY * 7, true, true, true)

abstract class PersistentWorkerYearly(id: Int, description: String, withNotification: Boolean = false)
    : PersistentWorker(id, true, description, AlarmManager.INTERVAL_DAY * 365, true, true, true)