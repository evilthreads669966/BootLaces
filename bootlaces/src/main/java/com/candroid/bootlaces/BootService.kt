package com.candroid.bootlaces

import android.content.Intent
import android.os.Build
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import com.candroid.bootlaces.NotificationUtils.Configuration.FOREGROUND_ID
import dagger.hilt.EntryPoints
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
    @Inject lateinit var dataStore: DataStore<Preferences>
    @Inject lateinit var boot: IBoot
    lateinit var foregroundService: ForegroundNotificationServiceImpl

    @PublishedApi
    internal companion object{
        var payload: (suspend () -> Unit)? = null
    }

    init {
        lifecycleScope.launchWhenCreated {
            if(payload != null) launch { payload?.invoke() }
            foregroundService.subscribe(boot){ flow ->
                flow.flowOn(Dispatchers.Default)
                    .flatMapMerge { flow{ emit(boot.mapPrefsToBoot(it)) } }
                    .collectLatest { b -> foregroundService.update(b) }
            }
        }
    }

    override fun getLifecycle() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        BootServiceState.setRunning()
        foregroundService = EntryPoints.get(provider.get().build(),ForegroundEntryPoint::class.java).getService()
    }

    override fun onStart(intent: Intent?, startId: Int) {
        mDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            lifecycleScope.launch { startBootForeground() }
        return START_STICKY
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        BootServiceState.setStopped()
        foregroundService.scope.also { it.coroutineContext.cancelChildren() }.cancel()
        super.onDestroy()
    }

    @Throws(SecurityException::class)
    fun startBootForeground(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            foregroundService.startForeground(boot)
        }
        else
            this.startForeground(FOREGROUND_ID, foregroundService.create(boot))
    }
}