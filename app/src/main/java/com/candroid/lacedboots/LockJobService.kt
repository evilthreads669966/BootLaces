package com.candroid.lacedboots

import android.app.job.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.candroid.bootlaces.BootServiceState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.ticker

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