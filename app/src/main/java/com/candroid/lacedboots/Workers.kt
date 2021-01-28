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

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.text.format.DateUtils
import android.util.Log
import com.candroid.bootlaces.Worker
import com.candroid.bootlaces.WorkerHourly
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

class WorkerEight: Worker(8, "working for 2 hours", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(AlarmManager.INTERVAL_HOUR * 2)
    }
}

class WorkerOne: Worker(1, "working for 2 minutes", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(120000)
    }
}

class WorkerTwo: Worker(2, "working for 15 minutes", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(AlarmManager.INTERVAL_FIFTEEN_MINUTES)
    }
}

class WorkerThree: Worker(3, "working for 1 minute", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(60000)
    }
}

class WorkerFour: Worker(4, "working for 5 minutes", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(60000 * 5)
    }
}

class WorkerFive: Worker(5, "working for 45 seconds", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(45000)
    }
}

class WorkerSix: Worker(6, "working for 1 minute", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(60000)
    }
}

class WorkerSeven: Worker(7, "working for a minute and a half", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(90000L)
    }
}

class WorkerThirteen: Worker(13, "working for 20 seconds", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(20000)
    }
}

class WorkerTwelve: Worker(12, "working for 30 seconds", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(30000)
    }
}

class WorkerEleven: Worker(11, "working for 5 seconds", true){
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, description)
        delay(5000)
    }
}

class WorkerTen: Worker(10,"Worker Ten", true) {
    override suspend fun doWork(ctx: Context) {
        Log.d(tag, "working for 10 seconds")
        for(i in 1..10)
            delay(1000)
    }
}

class WorkerFourteen: WorkerHourly(14, "survives reboot and performs every hour", true){

    override val receiver: WorkReceiver?
        get() = object : WorkReceiver(Intent.ACTION_TIME_TICK) {
            
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if(intent?.action?.equals(action) ?: false){
                    val date = DateUtils.formatDateTime(ctx, System.currentTimeMillis(),0)
                    Log.d(this.tag, date ?: "null")
                }
            }
        }

    override suspend fun doWork(ctx: Context) {
        while(true){
            Log.d(tag, "working for three minutes")
            delay(60000L * 3)
        }
    }
}