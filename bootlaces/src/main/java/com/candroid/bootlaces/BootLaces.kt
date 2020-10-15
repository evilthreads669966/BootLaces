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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

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
 * Creates the first Boot and starts its' foreground service.
 * Modify your Boot with updateBoot to change the foreground notification
 **/
@ActivityScoped
class BootLaces @Inject constructor(val boot: IBoot){

    @ExperimentalCoroutinesApi
    @Throws(BootException::class)
    inline fun startBoot(ctx: Activity, noinline payload: ( suspend () -> Unit)? = null, crossinline init: IBoot.() -> Unit) = runBlocking{
        LifecycleBootService.payload = payload
        if(boot.service != null) return@runBlocking
        val service = boot.apply { init() }.service ?: throw BootException()
        val intent = Intent(ctx, Class.forName(service))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent)
        else
            ctx.startService(intent)
    }

    /*update the persistent foreground notification's data*/
    inline fun updateBoot(ctx: Context, crossinline config: IBoot.() -> Unit){
        boot.apply { config() }
        LocalBroadcastManager.getInstance(ctx).sendBroadcast(Intent(Actions.ACTION_UPDATE))
    }
}
