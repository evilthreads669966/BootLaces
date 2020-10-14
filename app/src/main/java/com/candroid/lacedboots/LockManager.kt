package com.candroid.lacedboots

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@ServiceScoped
class LockManager @Inject constructor(): ILockManager {
    override fun isLocked() = ScreenVisibility.isVisible()

    override fun isLockable(ctx: Context): Boolean{
        var overlay = true
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            overlay = Settings.canDrawOverlays(ctx)
        }
        return overlay && !isLocked()
                && !(ctx.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardLocked
                && (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
    }

    override fun lockScreen(ctx: Context, intent: Intent?) {
        val activityIntent = (intent ?: Intent()).apply {
            setClass(ctx, LockScreenActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ctx.startActivity(activityIntent)
    }
}

@ServiceScoped
interface ILockManager {
    fun isLocked(): Boolean

    fun isLockable(ctx: Context): Boolean

    fun lockScreen(ctx: Context, intent: Intent?)
}
