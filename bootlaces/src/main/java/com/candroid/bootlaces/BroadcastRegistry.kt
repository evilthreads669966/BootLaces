package com.candroid.bootlaces

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.runBlocking

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * Register and unregister all BroadcastReceivers.
 * */
internal object BroadcastRegistry{
    private val receiver: BroadcastReceiver
    private val shutdownReceiver: BroadcastReceiver

    init {
        receiver = object : BroadcastReceiver(){
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if(intent?.action?.equals(Actions.ACTION_UPDATE) ?: false)
                    BootNotificationFactory.getInstance(ctx!!).updateBootNotification()
            }
        }

        /*saves boot data to storage when the device begins to restart or shutdown*/
        shutdownReceiver = object : BroadcastReceiver() {
            val TAG = this::class.java.name
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if(intent?.action.equals(Intent.ACTION_SHUTDOWN))
                    runBlocking { BootRepository.getInstance(ctx!!).saveBoot(Boot.getInstance()) }
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