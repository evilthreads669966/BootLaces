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
internal class BootNotificationService(val ctx: Context){
    fun setContentIntent(builder: NotificationCompat.Builder, activity: String) {
        with(Intent(ctx, Class.forName(activity))) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_VIEW
            builder.setContentIntent(PendingIntent.getActivity(ctx, 0, this, 0))
        }
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            with(ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager){
                getNotificationChannel(getChannelId())?.let { return }
                val notificationChannel = NotificationChannel(getChannelId(), getChannelName(), NotificationManager.IMPORTANCE_HIGH)
                createNotificationChannel(notificationChannel)
            }
        }
    }

    fun getId() = ctx.resources.getInteger(R.integer.notification_id)

    fun getChannelId(): String = ctx.resources.getString(R.string.channel_id)

    fun getChannelName(): String = ctx.resources.getString(R.string.channel_name)

    suspend fun create(): Notification? {
        val bootNotifConfig = AppContainer.getInstance(ctx).repository.getBootNotificationConfig().firstOrNull()
        if(bootNotifConfig != null){
            val builder = NotificationCompat.Builder(ctx, getChannelId())
            builder.setContentTitle(bootNotifConfig.title)
            builder.setContentText(bootNotifConfig.content)
            builder.setSmallIcon(bootNotifConfig.icon ?: android.R.drawable.sym_def_app_icon)
            if(bootNotifConfig.activity != null)
                setContentIntent(builder, bootNotifConfig.activity!!)
            builder.setShowWhen(false)
            builder.setAutoCancel(false)
            builder.setOngoing(true)
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(getChannelId())
            return builder.build()
        }
        return null
    }

    suspend fun update(title: String?, content: String?, icon: Int) {
        AppContainer.getInstance(ctx).repository.setNotification(null, null, title, content, icon)
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(getId(), this@BootNotificationService.create())
    }
}