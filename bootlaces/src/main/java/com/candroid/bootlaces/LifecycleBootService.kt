package com.candroid.bootlaces

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * Lifecycle aware persistent foreground service that is started by the startBoot method and BootReceiver.
 * */
abstract class LifecycleBootService: LifecycleService() {
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    private lateinit var notifProxy: NotificationProxy

    @PublishedApi
    internal companion object{
        var payload: (suspend () -> Unit)? = null
    }

    init {
        if(payload != null)
            lifecycleScope.launchWhenCreated {
                payload!!.invoke()
            }
    }

    override fun getLifecycle() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        BootServiceState.setRunning()
        super.onCreate()
        notifProxy = NotificationProxy()
        notifProxy.onCreate(this)
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
        notifProxy.onDestroy(this)
        super.onDestroy()
        BootServiceState.setStopped()
    }
}