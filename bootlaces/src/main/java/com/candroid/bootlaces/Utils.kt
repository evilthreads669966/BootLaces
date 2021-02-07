package com.candroid.bootlaces

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
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
 * @date 02/05/20
 *
 **/
private fun Class<*>.isComponentEnabled(ctx: Context): Boolean{
    val componentName = ComponentName(ctx, this)
    val state = ctx.packageManager.getComponentEnabledSetting(componentName)
    return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
}

private fun Class<*>.enableComponent(ctx: Context): Boolean{
    val componentName = ComponentName(ctx, this)
    val state = ctx.packageManager.getComponentEnabledSetting(componentName)
    if(state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
        ctx.packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        return true
    }
    return false
}

internal object Utils{
    fun isRebootEnabled(ctx: Context) = BootReceiver::class.java.isComponentEnabled(ctx)
    fun enableReboot(ctx: Context) = BootReceiver::class.java.enableComponent(ctx)
}
