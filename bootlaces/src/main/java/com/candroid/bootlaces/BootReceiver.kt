
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

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint

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
 * @date 10/09/20
 *
 * Activates [WorkService]
 **/

@AndroidEntryPoint
class BootReceiver : HiltBugReceiver(){

    companion object{
        internal fun isRebootEnabled(ctx: Context): Boolean{
            val componentName = ComponentName(ctx, BootReceiver::class.java)
            val state = ctx.packageManager.getComponentEnabledSetting(componentName)
            return (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
        }

        internal fun enableReboot(ctx: Context){
            val componentName = ComponentName(ctx, BootReceiver::class.java)
            val state = ctx.packageManager.getComponentEnabledSetting(componentName)
            if(state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
                ctx.packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    override fun onReceive(ctx: Context?, intent: Intent?){
        super.onReceive(ctx, intent)
        if(!WorkService.isStarted() && intent?.action?.contains("BOOT") ?: false) {
            intent?.setClass(ctx ?: return, WorkService::class.java)
            intent?.setAction(null)?.setAction(Actions.ACTION_SCHEDULE_AFTER_REBOOT.action)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ctx?.startForegroundService(intent)
            else
                ctx?.startService(intent)
        }
    }
}
/*fixes bug in Hilt*/
open class HiltBugReceiver : BroadcastReceiver(){
    override fun onReceive(ctx: Context?, intent: Intent?) {}
}