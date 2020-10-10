/*Copyright 2019 Chris Basinger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.candroid.bootlaces

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.runBlocking

/*
            (   (                ) (             (     (
            )\ ))\ )    *   ) ( /( )\ )     (    )\ )  )\ )
 (   (   ( (()/(()/(  ` )  /( )\()|()/((    )\  (()/( (()/(
 )\  )\  )\ /(_))(_))  ( )(_)|(_)\ /(_))\((((_)( /(_)) /(_))
((_)((_)((_|_))(_))   (_(_()) _((_|_))((_))\ _ )(_))_ (_))
| __\ \ / /|_ _| |    |_   _|| || | _ \ __(_)_\(_)   \/ __|
| _| \ V /  | || |__    | |  | __ |   / _| / _ \ | |) \__ \
|___| \_/  |___|____|   |_|  |_||_|_|_\___/_/ \_\|___/|___/
....................../´¯/)
....................,/¯../
.................../..../
............./´¯/'...'/´¯¯`·¸
........../'/.../..../......./¨¯\
........('(...´...´.... ¯~/'...')
.........\.................'...../
..........''...\.......... _.·´
............\..............(
..............\.............\...
*/
/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 **/
abstract class BootService : Service() {
    private lateinit var notifProxy: NotificationProxy

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        BootServiceState.setRunning()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        notifProxy = NotificationProxy()
        notifProxy.onCreate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        BootServiceState.setStopped()
        notifProxy.onDestroy(this)
    }
}


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

internal class NotificationProxy{
    private lateinit var receiver: UpdateReceiver

    fun onCreate(ctx: Service){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val filter = IntentFilter(Actions.ACTION_UPDATE)
            receiver = UpdateReceiver()
            LocalBroadcastManager.getInstance(ctx).registerReceiver(receiver, filter)
            runBlocking {
                startBootNotification(ctx)
            }
        }
    }

    fun onDestroy(ctx: Service){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(receiver)
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

                runBlocking {
                    BootNotificationFactory.getInstance(ctx!!).updateBootNotification(boot)
                }
            }
        }
    }
}