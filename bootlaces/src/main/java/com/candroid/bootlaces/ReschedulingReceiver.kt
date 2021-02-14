
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
 * Handles rescheduling for work that survives reboot
 **/

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
internal class ReschedulingReceiver : HiltBugReceiver(){
    @Inject lateinit var workRescheduling: WorkRescheduling
    @Inject lateinit var coroutineScope: CoroutineScope
    override fun onReceive(ctx: Context?, intent: Intent?){
        super.onReceive(ctx, intent)
        if(ctx != null && intent != null && intent.action != null)
            if(intent.action!!.contains("BOOT"))
                goAsync().apply {
                    runBlocking {
                        coroutineScope.launch(Dispatchers.IO) {
                            workRescheduling.reschedule(this)
                        }
                    }
                }.finish()
    }
}

/*fixes bug in Hilt*/
internal open class HiltBugReceiver : BroadcastReceiver(){
    override fun onReceive(ctx: Context?, intent: Intent?) {}
}