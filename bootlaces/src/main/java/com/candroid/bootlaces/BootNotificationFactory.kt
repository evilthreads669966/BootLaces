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
import kotlinx.coroutines.flow.firstOrNull

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
 * Not really the best factory but I was trying to pick a good name. More responsibilties than a simple factory pattern should have.
 * Creates [Notifications] and updates foreground [Notification]
 **/
internal class BootNotificationFactory(val ctx: Context){

    companion object{
        private var INSTANCE: BootNotificationFactory? = null
        fun getInstance(ctx: Context): BootNotificationFactory{
            if(INSTANCE == null)
                INSTANCE = BootNotificationFactory(ctx)
            return INSTANCE!!
        }
    }

    object Configuration{
        val CHANNEL_ID = "666"
        val CHANNEL_NAME = "evil"
        val FOREGROUND_ID = 6666
        val DEFAULT_TITLE = "EVIL THREADS"
        val DEFAULT_CONTENT = "BOOT LACES"

        fun createChannel(ctx: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                with(ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager){
                    if(getNotificationChannel(CHANNEL_ID) == null){
                        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                        createNotificationChannel(channel)
                        return true
                    }
                }
            }
            return false
        }
    }

    suspend fun createNotification(): Notification? {
        val boot = AppContainer.getInstance(ctx).repository.loadBoot().firstOrNull()
        if(boot != null){
            Configuration.createChannel(ctx)
            val builder = NotificationCompat.Builder(ctx, Configuration.CHANNEL_ID)
            builder.setContentTitle(boot.title ?: Configuration.DEFAULT_TITLE)
            builder.setContentText(boot.content ?: Configuration.DEFAULT_CONTENT)
            builder.setSmallIcon(boot.icon ?: android.R.drawable.sym_def_app_icon)
            if(boot.activity != null)
                builder.setContentIntent(boot.activity!!)
            builder.setShowWhen(false)
            builder.setAutoCancel(false)
            builder.setOngoing(true)
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(Configuration.CHANNEL_ID)
            return builder.build()
        }
        return null
    }

    suspend fun updateForegroundNotification(title: String?, content: String?, icon: Int?) {
        AppContainer.getInstance(ctx).repository.saveBoot(null, null, title, content, icon)
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Configuration.FOREGROUND_ID, this@BootNotificationFactory.createNotification())
    }


    private fun NotificationCompat.Builder.setContentIntent(activity: String) {
        val intent = Intent(ctx, Class.forName(activity)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_VIEW
        }
        setContentIntent(PendingIntent.getActivity(ctx, 0, intent, 0))
    }

}

