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
package com.candroid.bootlaces.service.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.candroid.bootlaces.service.notification.NotificationUtils.Configuration.FOREGROUND_CHANNEL_ID
import com.candroid.bootlaces.service.notification.NotificationUtils.Configuration.FOREGROUND_GROUP_ID

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
object NotificationUtils{

    internal val NOTIFICATION_TEMPLATE = NotificationCompat.Extender {
        it.run {
            it.setShowWhen(false)
            setAutoCancel(false)
            setOngoing(true)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                setColorized(true)
                setColor(Color.TRANSPARENT)
            }
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setChannelId(FOREGROUND_CHANNEL_ID)
            }
            setGroup(FOREGROUND_GROUP_ID)
        }
    }

    internal object Configuration{
        val DEFAULT_FOREGROUND_ICON = android.R.drawable.sym_def_app_icon
        val FOREGROUND_CHANNEL_DESCRIPTION = "EVIL CHANNEL 666"
        val FOREGROUND_GROUP_DESCRIPTION = "EVIL GROUP 666"
        val FOREGROUND_GROUP_NAME = "EVIL GROUP"
        val FOREGROUND_GROUP_ID = "666"
        val FOREGROUND_ID = 666
        val FOREGROUND_CHANNEL_ID = "EVIL SERVICE"
        val FOREGROUND_CHANNEL_NAME= "EVIL CHANNEL"
        val DEFAULT_FOREGROUND_TITLE = "EVIL TITLE"
        val DEFAULT_FOREGROUND_CONTENT = "EVIL CONTENT"

        fun createForegroundChannel(ctx: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mgr = NotificationManagerCompat.from(ctx)
                if(mgr.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mgr.createNotificationChannelGroup(NotificationChannelGroup(FOREGROUND_GROUP_ID, FOREGROUND_GROUP_NAME))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            mgr.getNotificationChannelGroup(FOREGROUND_GROUP_ID)
                        }
                    }
                    val channel = NotificationChannel(FOREGROUND_CHANNEL_ID, FOREGROUND_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                        group = FOREGROUND_GROUP_ID
                        description = FOREGROUND_CHANNEL_DESCRIPTION
                        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                        setShowBadge(false)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            setAllowBubbles(false)
                    }
                    mgr.createNotificationChannel(channel)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mgr.getNotificationChannelGroup(FOREGROUND_GROUP_ID)?.apply {
                            channels.add(mgr.getNotificationChannel(FOREGROUND_CHANNEL_ID))
                            description = FOREGROUND_GROUP_DESCRIPTION
                        }
                    }
                    return true
                }
            }
            return false
        }
    }

    internal fun NotificationCompat.Builder.setContentIntent(ctx: Context, activity: String) {
        val intent = Intent(ctx, Class.forName(activity)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_VIEW
        }
        setContentIntent(PendingIntent.getActivity(ctx, 0, intent, 0))
    }
}