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
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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
 **/
internal class BootNotificationFactory(val ctx: Context){

    internal companion object{
        private var INSTANCE: BootNotificationFactory? = null
        fun getInstance(ctx: Context): BootNotificationFactory{
            if(INSTANCE == null)
                INSTANCE = BootNotificationFactory(ctx)
            return INSTANCE!!
        }
    }

    internal object Configuration{
        val CHANNEL_ID = "666"
        val CHANNEL_NAME = "evil"
        val FOREGROUND_ID = 6666
        val DEFAULT_TITLE = "EVIL THREADS"
        val DEFAULT_CONTENT = "BOOT LACES"

        fun createChannel(ctx: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if(mgr.getNotificationChannel(CHANNEL_ID) == null){
                    val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                    mgr.createNotificationChannel(channel)
                    return true
                }
            }
            return false
        }
    }

    suspend fun createNotification(): Notification? {
        val boot = BootRepository.getInstance(ctx).loadBoot().firstOrNull()
        if(boot != null){
            Configuration.createChannel(ctx)
            val builder = NotificationCompat.Builder(ctx, Configuration.CHANNEL_ID).apply {
                setContentTitle(boot.title ?: Configuration.DEFAULT_TITLE)
                setContentText(boot.content ?: Configuration.DEFAULT_CONTENT)
                setSmallIcon(boot.icon ?: android.R.drawable.sym_def_app_icon)
                if(boot.activity != null)
                    setContentIntent(boot.activity!!)
                setShowWhen(false)
                setAutoCancel(false)
                setOngoing(true)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    setChannelId(Configuration.CHANNEL_ID)
            }
            return builder.build()
        }
        return null
    }

    suspend fun updateBootNotification(boot: Boot) {
        Scopes.BOOT_SCOPE.launch { BootRepository.getInstance(ctx).saveBoot(boot) }
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Configuration.FOREGROUND_ID, createNotification())
    }

    private fun NotificationCompat.Builder.setContentIntent(activity: String) {
        val intent = Intent(ctx, Class.forName(activity)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_VIEW
        }
        setContentIntent(PendingIntent.getActivity(ctx, 0, intent, 0))
    }
}