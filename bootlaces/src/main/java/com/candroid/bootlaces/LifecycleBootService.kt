package com.candroid.bootlaces

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

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
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    private lateinit var job: Job

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
        super.onCreate()
        BootServiceState.setRunning()
        BroadcastRegistry.register(this)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        mDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        job = GlobalScope.launch(Dispatchers.Default) {
            val notif = async { BootNotificationFactory.getInstance(this@LifecycleBootService).createNotification() }
            startForeground(BootNotificationFactory.Configuration.FOREGROUND_ID, notif.await())
        }
        return START_STICKY
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        BroadcastRegistry.unregister(this)
        BootServiceState.setStopped()
        job.cancel()
        super.onDestroy()
    }
}