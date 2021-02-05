package com.candroid.bootlaces

import android.content.Context

open class PersistentReceiver(id: Int) : PersistentWorker(id, false, "", 0L, true, true, false) {
    override suspend fun doWork(ctx: Context) {
        while(true){ }
    }
}