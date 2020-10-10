package com.candroid.bootlaces

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * Responsible for the persistent foreground notification of BootService
 * */
internal class NotificationProxy{
    private lateinit var receiver: UpdateReceiver
    lateinit var job: Job

    companion object{
        var updateReceiverJob: Job? = null
    }
    fun onCreate(ctx: Service){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val filter = IntentFilter(Actions.ACTION_UPDATE)
            receiver = UpdateReceiver()
            LocalBroadcastManager.getInstance(ctx).registerReceiver(receiver, filter)
            job = Scopes.BOOT_SCOPE.launch { startBootNotification(ctx) }
        }
    }

    fun onDestroy(ctx: Service){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(receiver)
        job.cancel()
        updateReceiverJob?.cancel()
        Scopes.BOOT_SCOPE.coroutineContext.also { it.cancelChildren() }.cancel()
    }

    /*create boot service notification*/
    private suspend fun startBootNotification(ctx: Service): Boolean {
        BootNotificationFactory.Configuration.createChannel(ctx)
        val notification = BootNotificationFactory.getInstance(ctx).createNotification()
        notification?.let { notif ->
            ctx.startForeground(BootNotificationFactory.Configuration.FOREGROUND_ID, notif)
            return true
        }
        return false
    }

    inner class UpdateReceiver: BroadcastReceiver(){

        override fun onReceive(ctx: Context?, intent: Intent?) {
            if(intent?.action?.equals(Actions.ACTION_UPDATE) ?: false){
                val boot = Boot()

                if(intent!!.hasExtra(BootRepository.KEY_ACTIVITY))
                    boot.activity = intent.getStringExtra(BootRepository.KEY_ACTIVITY)

                if(intent.hasExtra(BootRepository.KEY_TITLE))
                    boot.title = intent.getStringExtra(BootRepository.KEY_TITLE)

                if(intent.hasExtra(BootRepository.KEY_CONTENT))
                    boot.content = intent.getStringExtra(BootRepository.KEY_CONTENT)

                if(intent.hasExtra(BootRepository.KEY_ICON))
                    boot.icon = intent.getIntExtra(BootRepository.KEY_ICON, -1).takeIf { ic -> ic != -1 }

               updateReceiverJob = Scopes.BOOT_SCOPE.launch { BootNotificationFactory.getInstance(ctx!!).updateBootNotification(boot) }
            }
        }
    }
}

@PublishedApi
internal object Scopes{
    val BOOT_SCOPE by lazy(LazyThreadSafetyMode.NONE) { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
}