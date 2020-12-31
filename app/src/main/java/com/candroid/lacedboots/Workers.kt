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
package com.candroid.lacedboots

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.candroid.bootlaces.Worker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay

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
 * @date 10/31/20
 *
 **/
class PeriodicWorker: Worker(777, "Periodic Worker"){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Periodic worker", "working")
    }
}
class FutureWorker: Worker(999, "Future Worker"){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Future worker", "working")
    }
}
class OneTimeWorker: Worker(66,"One time work") {
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        for(i in 1..10)
            delay(1000)
    }
}
class SecondWorker: Worker(99,"Second worker") {
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        for(i in 1..10)
            delay(1000)
    }
}
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class ScreenLockerJob: Worker(666,"Locking the screen"){
    override val receiver: WorkReceiver?
        get() = object : WorkReceiver(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                LockManager.lockScreen(ctx!!, intent)
            }
        }

    override suspend fun doWork(ctx: Context) {
        val powerMgr = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        while(true){
            val intent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            delay(500)
            if (powerMgr.isInteractive)
                ctx.sendBroadcast(intent)
        }
    }
}