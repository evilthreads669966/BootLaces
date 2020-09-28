package com.candroid.lacedboots

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject

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
object AppModules{
    val lockerModule = module {
        single { LockManager() }
    }
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class App: Application(){
    val lockManager by inject<ILockManager>()

    companion object{
        val INSTANCE by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { App() }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(AppModules.lockerModule)
        }
        LockJobService.schedule(this)
    }
}

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class LockManager : ILockManager() {
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

sealed class ILockManager{
    abstract fun isLocked(): Boolean

    abstract fun isLockable(ctx: Context): Boolean

    abstract fun lockScreen(ctx: Context, intent: Intent?)
}