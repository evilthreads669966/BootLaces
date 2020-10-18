package com.candroid.bootlaces.service

import android.content.Intent
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import com.candroid.bootlaces.ForegroundComponent
import com.candroid.bootlaces.ForegroundScope
import com.candroid.bootlaces.activators.ForegroundActivator
import com.candroid.bootlaces.activators.ForegroundActivator.Companion.startActivator
import com.candroid.bootlaces.service.notification.mapPrefsToBoot
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Provider

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * Boot strapped lifecycle aware persistent foreground service.
 * LifecycleBootService has ability to pass a function to onCreate off the main threads at runtime.
 * This payload is unrelated to Boot
 * */
@FlowPreview
@ForegroundScope
abstract class BootService: LifecycleService() {
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    @Inject lateinit var provider: Provider<ForegroundComponent.Builder>
    lateinit var activator: ForegroundActivator

    @PublishedApi
    internal companion object{
        var payload: (suspend () -> Unit)? = null
    }

    init {
        lifecycleScope.launchWhenCreated {
            if(payload != null) launch { payload?.invoke() }
            activator.activateCommunication{ flow ->
                flow.flowOn(Dispatchers.Default)
                    .flatMapMerge { flow{ emit(activator.info.mapPrefsToBoot(it)) } }
                    .collectLatest { b -> activator.updateForeground() }
            }
        }
    }

    override fun getLifecycle() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        BootServiceState.setRunning()
        activator = provider.startActivator()
    }

    override fun onStart(intent: Intent?, startId: Int) {
        mDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            lifecycleScope.launch { activator.activateForeground() }
        return START_STICKY
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        BootServiceState.setStopped()
        activator.scope.also { it.coroutineContext.cancelChildren() }.cancel()
        super.onDestroy()
    }
}