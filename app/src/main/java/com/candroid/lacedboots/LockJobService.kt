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

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.candroid.bootlaces.BootServiceState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.ticker

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
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class LockJobService: JobService(){

    companion object Scheduler{
        val ID = 666

        fun schedule(ctx: Context){
            val job = JobInfo.Builder(ID, ComponentName(ctx, LockJobService::class.java)).setOverrideDeadline(0).build()
            val scheduler = ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            scheduler.schedule(job)
        }
    }

    override fun onStopJob(params: JobParameters?) = true

    @InternalCoroutinesApi
    override fun onStartJob(params: JobParameters?): Boolean {
        GlobalScope.launch {
            ticker(2000, 10000, this.coroutineContext + Dispatchers.Default).consumeEach {
                if (BootServiceState.isStopped()) {
                    with(applicationContext){
                        val intent = Intent(this, LockService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            startForegroundService(intent)
                        else
                            startService(intent)
                        delay(2000)
                    }
                }
                yield()
            }
        }
        return true
    }
}