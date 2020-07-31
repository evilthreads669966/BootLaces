package com.candroid.lacedboots

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Environment

class FileJobService: JobService(){
    override fun onStopJob(params: JobParameters?): Boolean {
        TODO("Not yet implemented")
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        TODO("Not yet implemented")
    }
}

fun files(ctx: Context){
    val fileNames = Environment.getExternalStorageDirectory()
}