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

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import javax.inject.Inject

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
 *
 * Persistent foreground service
 **/
abstract class BootService : Service() {
    @Inject lateinit var mgr: BootNotificationManager
    @Inject lateinit var monitor: BroadcastMonitor
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        BootServiceState.setRunning()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startBootForeground()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        monitor.register(this)
    }

    override fun onDestroy() {
        BootServiceState.setStopped()
        monitor.unregister(this)
        super.onDestroy()
    }

    @Throws(SecurityException::class)
    fun startBootForeground(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            Log.d("BOOTSERVICE", "${this.foregroundServiceType}")
            startForeground(BootNotificationManager.Configuration.FOREGROUND_ID, mgr.createNotification(), foregroundServiceType)
        }
        else
            startForeground(BootNotificationManager.Configuration.FOREGROUND_ID, mgr.createNotification())
    }
}