package com.candroid.lacedboots

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.candroid.bootlaces.Worker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay

class OneTimeWorker: Worker(66,"One time work") {
    override suspend fun doWork(ctx: Context) {
        for(i in 1..10)
            delay(1000)
    }
}
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class ScreenLockerJob: Worker(666,"Locking the screen", action = Intent.ACTION_CLOSE_SYSTEM_DIALOGS){
    override suspend fun doWork(ctx: Context) {
        val powerMgr = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        while(true){
            val intent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            delay(500)
            if (powerMgr.isInteractive)
                ctx.sendBroadcast(intent)
        }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        LockManager.takeIf { !it.isLocked() && it.isLockable(ctx) }?.lockScreen(ctx, intent)
    }
}