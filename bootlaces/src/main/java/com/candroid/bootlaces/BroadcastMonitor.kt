package com.candroid.bootlaces

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * Monitors system and local broadcasts.
 * */
@Singleton
class BroadcastMonitor @Inject constructor(val mgr: BootNotificationManager,val repo: BootRepository){
    private val receiver: BroadcastReceiver
    private val shutdownReceiver: BroadcastReceiver
    init {
        /*Receives broadcasts when Boot has changed its' state.
        * Responsible for updating Boot foreground notification*/
        receiver = object : BroadcastReceiver(){
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if(intent?.action?.equals(Actions.ACTION_UPDATE) ?: false)
                    mgr.updateBootNotification()
            }
        }

        /*Saves Boot to storage when the device is powering off*/
        shutdownReceiver = object : BroadcastReceiver() {
            val TAG = this::class.java.name
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if(intent?.action.equals(Intent.ACTION_SHUTDOWN))
                    runBlocking { repo.saveBoot(mgr.boot) }
            }
        }
    }

    fun register(ctx: Context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LocalBroadcastManager.getInstance(ctx).registerReceiver(receiver, IntentFilter(Actions.ACTION_UPDATE))
        ctx.registerReceiver(shutdownReceiver, IntentFilter(Intent.ACTION_SHUTDOWN))
    }

    fun unregister(ctx: Context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(receiver)
        ctx.unregisterReceiver(shutdownReceiver)
    }
}