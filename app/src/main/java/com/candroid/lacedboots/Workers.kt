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

class YearlyWorker: Worker(444, "Yearly Worker"){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Yearly Worker", "working for 2 hours")
        delay(AlarmManager.INTERVAL_HOUR * 2)
    }
}

class MonthlyWorker: Worker(333, "Monthly Worker", withNotification = true){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Monthly Worker", "working for 2 minutes")
        delay(120000)
    }
}

class WeeklyWorker: Worker(888, "Weekly Worker", withNotification = true){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Weekly Worker", "working for 15 minutes")
        delay(AlarmManager.INTERVAL_FIFTEEN_MINUTES)
    }
}

class DailyWorker: Worker(222, "Daily Worker", withNotification = true){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Daily Worker", "working for 1 minute")
        delay(60000)
    }
}

class HourlyWorker: Worker(111, "Hourly Worker", withNotification = true){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Hourly Worker", "working for 5 minutes")
        delay(60000 * 5)
    }
}

class HalfHourWorker: Worker(8899, "Half Hour Worker", withNotification = true){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Half Hour Worker", "working for 45 seconds")
        delay(45000)
    }
}

class QuarterHourWorker: Worker(445, "Quarter Hour Worker", withNotification = true){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Quarter Hour Worker", "working for 1 minute")
        delay(60000)
    }
}


class ThirdFutureWorker: Worker(667, "Third Future Worker", withNotification = true){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Third Future Worker", "working for 20 seconds")
        delay(20000)
    }
}



class SecondFutureWorker: Worker(6666, "Second Future Worker", withNotification = true){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("Second Future Worker", "working for 30 seconds")
        delay(30000)
    }
}



class FirstFutureWorker: Worker(999, "First Future Worker", withNotification = true){
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("First Future Worker", "working for 5 seconds")
        delay(5000)
    }
}
class OneTimeWorker: Worker(66,"One time work", withNotification = true) {
    override val receiver: WorkReceiver?
        get() = null

    override suspend fun doWork(ctx: Context) {
        Log.d("OneTimeWorker", "working for 10 seconds")
        for(i in 1..10)
            delay(1000)
    }
}

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class PersistentWorker: Worker(666,"Locking the screen", withNotification = true){
    override val receiver: WorkReceiver?
        get() = object : WorkReceiver(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                //LockManager.lockScreen(ctx!!, intent)
            }
        }

    override suspend fun doWork(ctx: Context) {
        val powerMgr = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        val intent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        while(true){
            Log.d("Persistent Worker", "Working for 25 seconds")
            delay(25000)
    /*        if (powerMgr.isInteractive)
                ctx.sendBroadcast(intent)*/
        }
    }
}