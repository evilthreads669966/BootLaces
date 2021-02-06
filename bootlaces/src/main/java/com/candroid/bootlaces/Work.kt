package com.candroid.bootlaces

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
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
 * @email evilthreads669966@gmail.com~
 * @date 10/18/20
 **/
@Parcelize
@Entity
data class Work(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val workerName: String,
    val interval: Long? = null,
    val repeating: Boolean? = null,
    val allowWhileIdle: Boolean? = null,
    val precision: Boolean? = null
) :Parcelable{
    internal constructor(worker: Worker, interval: Long? = null, repeating: Boolean? = null, allowWhileIdle: Boolean? = null, precision: Boolean? = null):
            this(worker.id, worker.javaClass.name, interval, repeating, allowWhileIdle, precision)

    companion object{
        internal const val KEY_PARCEL = "KEY_PARCEL"
    }
}