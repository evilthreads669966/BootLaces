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

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.security.InvalidParameterException

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
 * Starts your BootService and initalizes its' required configuration data for a foreground notification
 **/
@Throws(BootException::class)
inline fun Context.startBoot(noinline payload: ( suspend () -> Unit)? = null,  crossinline init: Boot.() -> Unit){
    LifecycleBootService.payload = payload
    val boot = Boot().apply { init() }
    if(boot.service == null)
        throw BootException()
    runBlocking {
        BootRepository.getInstance(this@startBoot).saveBoot(boot)
        val bootNotifConfig = BootRepository.getInstance(this@startBoot).loadBoot().firstOrNull()
        if(bootNotifConfig?.service != null){
            val intent = Intent(this@startBoot, Class.forName(bootNotifConfig.service!!))
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(intent)
            else
                startService(intent)
        }
    }
}

/* Change the Boot properties for the persistent foreground notificaiton*/
inline fun updateBoot(ctx: Context, crossinline config: Boot.() -> Unit){
    val boot = Boot().apply { config() }
    val updateIntent = Intent().apply {
        action = Actions.ACTION_UPDATE
        putExtra(BootRepository.KEY_TITLE, boot.title)
        putExtra(BootRepository.KEY_CONTENT, boot.content)
        putExtra(BootRepository.KEY_ICON, boot.icon)
        putExtra(BootRepository.KEY_ACTIVITY, boot.activity)
    }
    LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent)
}

