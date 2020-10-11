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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
inline fun Context.startBoot(noinline payload: ( suspend () -> Unit)? = null,  crossinline init: BootConfig.() -> Unit){
    LifecycleBootService.payload = payload
    val bootConfig = runBlocking { return@runBlocking BootRepository.getInstance(this@startBoot).loadBoot<BootConfig>().firstOrNull() }
    if(bootConfig!!.service != null)
        return
    bootConfig.init()
    if(bootConfig.service == null)
        return
    lateinit var intent: Intent
    Boot.getInstance().apply { edit(bootConfig) }.run {
        Scopes.BOOT_SCOPE.launch { BootRepository.getInstance(this@startBoot).saveBoot(this@run) }
        intent = Intent(this@startBoot, Class.forName(this.service!!))
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        startForegroundService(intent)
    else
        startService(intent)
}

/* Change the Boot properties for the persistent foreground notificaiton*/
inline fun updateBoot(ctx: Context, crossinline config: BootConfig.() -> Unit){
    val boot = BootConfig()
    boot.config()
    Boot.getInstance().run {  edit(boot) }
    LocalBroadcastManager.getInstance(ctx).sendBroadcast(Intent(Actions.ACTION_UPDATE))
}