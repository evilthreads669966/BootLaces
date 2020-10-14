package com.candroid.bootlaces

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import com.candroid.bootlaces.BootNotificationManager.Configuration.FOREGROUND_ID
import javax.inject.Inject

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * Boot strapped lifecycle aware persistent foreground service.
 * LifecycleBootService has ability to pass a function to onCreate off the main threads at runtime.
 * This payload is unrelated to Boot
 * */
abstract class LifecycleBootService: LifecycleService() {
    private val tag = this::class.java.name
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    @Inject lateinit var mgr: BootNotificationManager
    @Inject lateinit var monitor: BroadcastMonitor

    @PublishedApi
    internal companion object{
        var payload: (suspend () -> Unit)? = null
    }

    init {
        if(payload != null)
            lifecycleScope.launchWhenCreated {
                payload!!.invoke()
            }
        lifecycleScope.launchWhenStarted {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startBootForeground()
        }
    }

    override fun getLifecycle() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        BootServiceState.setRunning()
        monitor.register(this)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        mDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        monitor.unregister(this)
        BootServiceState.setStopped()
        super.onDestroy()
    }

    @Throws(SecurityException::class)
   suspend fun startBootForeground(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Log.d("BOOTSERVICE", "${this.foregroundServiceType}")
            startForeground(FOREGROUND_ID, mgr.createNotification(), foregroundServiceType)
        }
        else
            this.startForeground(FOREGROUND_ID, mgr.createNotification())
    }
}