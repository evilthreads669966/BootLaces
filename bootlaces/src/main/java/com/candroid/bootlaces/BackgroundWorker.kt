package com.candroid.bootlaces

import android.content.Intent
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import dagger.hilt.EntryPoints
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Provider

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * activates [FlowWorker]
 * */
@InternalCoroutinesApi
@FlowPreview
@ForegroundScope
abstract class BackgroundWorker: LifecycleService() {
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    private var startId: Int = 0
    @Inject lateinit var provider: Provider<ForegroundComponent.Builder>
    @InternalCoroutinesApi
    @Inject lateinit var channel: Channel<FlowWorker>
    lateinit var component: ForegroundComponent
    @Inject lateinit var foreground: ForegroundActivator
    private val workers = mutableSetOf<FlowWorker>()

    init {
        lifecycle.addObserver(BootServiceState)
        lifecycleScope.launchWhenCreated {
            foreground.activate()
            withContext(Dispatchers.Default){
                channel.consumeAsFlow()
                    .onEach {
                        foreground.update(ForegroundTypes.BACKGROUND, it)
                        it.doWork(this@BackgroundWorker)
                    }.filter { it.complete }
                    .collect {
                        foreground.update(ForegroundTypes.BACKGROUND_COMPLETE, it)
                        workers.add(it)
                    }
            }
            foreground.deactivate()
        }
    }

    override fun getLifecycle() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mDispatcher.onServicePreSuperOnStart()
        super.onStartCommand(intent, flags, startId)
        this.startId = startId
        return START_STICKY
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        if(BootServiceState.isForeground())
            ServiceCompat.stopForeground(this,ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelfResult(startId)
        foreground.scope.also { it.coroutineContext.cancelChildren() }.cancel()
        lifecycle.removeObserver(BootServiceState)
        super.onDestroy()
    }
}