
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
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

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
@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class BootReceiver : HiltBugReceiver(){
    @ExperimentalCoroutinesApi
    override fun onReceive(ctx: Context?, intent: Intent?){
        super.onReceive(ctx, intent)
        if(!WorkService.isStarted() && intent?.action?.contains("BOOT") ?: false) {
            runBlocking {
                intent?.setClass(ctx!!, WorkService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ctx!!.startForegroundService(intent)
                else
                    ctx!!.startService(intent)
            }
        }
        else{
            if(intent!!.hasExtra(Work.KEY_PARCEL)){
                val work = intent.getParcelableExtra<Work>(Work.KEY_PARCEL)
                GlobalScope.launch {
                    if(work!!.interval != null)
                        sendWorkRequest(ctx!!, work, Actions.ACTION_WORK_PERIODIC)
                    else
                        sendWorkRequest(ctx!!, work, Actions.ACTION_WORK_FUTURE)
                }
            }
        }
    }
}
/*fixes bug in Hilt*/
open class HiltBugReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {}
}