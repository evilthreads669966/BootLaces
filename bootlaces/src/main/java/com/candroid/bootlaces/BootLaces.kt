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
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

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
@ExperimentalCoroutinesApi
@Throws(BootException::class)
inline fun Context.startBoot(noinline payload: ( suspend () -> Unit)? = null,  crossinline init: Boot.() -> Unit) = runBlocking{
    LifecycleBootService.payload = payload
    val boot =  Scopes.BOOT_SCOPE.async { BootRepository.getInstance(this@startBoot).loadBoot().firstOrNull() ?: Boot() }
    if(boot.await().service != null)
        return@runBlocking
    boot.getCompleted().apply { init() }.let { boot ->
        Log.d(this::class.java.name, "REALLY BAD")
        if (boot.service == null)
            throw BootException()
        val job = Scopes.BOOT_SCOPE.launch {
            BootRepository.getInstance(this@startBoot).saveBoot(boot)
        }
        val intent = Intent(this@startBoot, Class.forName(boot.service!!))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent)
        else
            startService(intent)
        job.join()
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

