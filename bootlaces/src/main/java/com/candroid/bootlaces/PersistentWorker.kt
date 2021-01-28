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
    val wakeIfIdle: Boolean = false,
    val precisionTiming: Boolean = false,
    open val repeating: Boolean,
): Worker(id, description, withNotification)

abstract class WorkerDaily : PersistentWorker{
    constructor(id: Int, description: String, withNotification: Boolean = false)
            : super(id, withNotification, description, AlarmManager.INTERVAL_HOUR*24, true, true, true)
}

abstract class WorkerHourly(id: Int, description: String, withNotification: Boolean = false)
    : PersistentWorker(id, true, description, AlarmManager.INTERVAL_HOUR, true, true, true)

abstract class WorkerHalfDay(id: Int, description: String, withNotification: Boolean = false)
    : PersistentWorker(id, true, description, AlarmManager.INTERVAL_DAY/2, true, true, true)

abstract class WorkerQuarterHourly(id: Int, description: String, withNotification: Boolean = false)
    : PersistentWorker(id, true, description, AlarmManager.INTERVAL_HOUR/4, true, true, true)