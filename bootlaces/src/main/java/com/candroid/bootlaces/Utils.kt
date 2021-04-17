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
    if(state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED){
        ctx.packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        return true
    }
    return false
}

internal object Utils{
    fun isRebootEnabled(ctx: Context) = ReschedulingReceiver::class.java.isComponentEnabled(ctx)
    fun enableReboot(ctx: Context) = ReschedulingReceiver::class.java.enableComponent(ctx)
}
