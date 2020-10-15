package com.candroid.bootlaces

import android.content.Intent
import android.os.Build
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_ACTIVITY
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_CONTENT
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_ICON
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_SERVICE
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_TITLE
import com.candroid.bootlaces.NotificationUtils.Configuration.FOREGROUND_ID
import com.candroid.bootlaces.NotificationUtils.createNotification
import com.candroid.bootlaces.NotificationUtils.updateBootNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
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
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    @Inject lateinit var dataStore: DataStore<Preferences>
    @Inject lateinit var boot: Boot

    @PublishedApi
    internal companion object{
        var payload: (suspend () -> Unit)? = null
    }

    init {
        lifecycleScope.launchWhenCreated {
            launch { payload?.invoke() }
            dataStore.data.flowOn(Dispatchers.Default).collect { prefs ->
                boot.apply {
                    service = prefs[PREF_KEY_SERVICE]
                    activity = prefs[PREF_KEY_ACTIVITY]
                    title = prefs[PREF_KEY_TITLE]
                    content = prefs[PREF_KEY_CONTENT]
                    icon = prefs[PREF_KEY_ICON]
                }
                launch { updateBootNotification(this@LifecycleBootService, boot) }
            }
        }
    }

    override fun getLifecycle() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        BootServiceState.setRunning()
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
        super.onDestroy()
    }

    @Throws(SecurityException::class)
    fun startBootForeground(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            startForeground(FOREGROUND_ID, createNotification(this, boot), foregroundServiceType)
        }
        else
            this.startForeground(FOREGROUND_ID, createNotification(this, boot))
    }
}