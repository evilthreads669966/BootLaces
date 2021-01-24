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

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.candroid.bootlaces.NotificationFactory.ForegroundNotification.NOTIFICATION_TEMPLATE_FOREGROUND
import com.candroid.bootlaces.NotificationFactory.WorkNotification.BACKGROUND_FINISHED_DEFAULT_TITLE
import com.candroid.bootlaces.NotificationFactory.WorkNotification.BACKGROUND_STARTED_DEFAULT_CONTENT
import com.candroid.bootlaces.NotificationFactory.WorkNotification.BACKGROUND_STARTED_DEFAULT_TITLE
import com.candroid.bootlaces.NotificationFactory.WorkNotification.TEMPLATE_FINISH
import com.candroid.bootlaces.NotificationFactory.WorkNotification.TEMPLATE_START
import com.candroid.bootlaces.NotificationFactory.WorkNotification.createBackgroundChannel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

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
 * @date 10/31/20
 * creates notifications for foreground and workers
 **/
internal class NotificationFactory @Inject constructor(@ApplicationContext private val ctx: Context, internal val mgr: NotificationManagerCompat, private val builder: NotificationCompat.Builder){
    internal fun createStartedNotification(description: String?): Notification{
        createBackgroundChannel(ctx, mgr)
        return builder.apply {
            extend(TEMPLATE_START)
            setContentTitle(description ?:  BACKGROUND_STARTED_DEFAULT_TITLE)
            setContentText(description ?: BACKGROUND_STARTED_DEFAULT_CONTENT)
        }.build()
    }

    internal fun createFinishedNotification(description: String?): Notification{
        createBackgroundChannel(ctx, mgr)
        return builder.apply {
            extend(TEMPLATE_FINISH)
            setContentTitle(description ?: BACKGROUND_FINISHED_DEFAULT_TITLE)
        }.build()
    }

    internal fun createForegroundNotification(): Notification{
        ForegroundNotification.createForegroundChannel(ctx, mgr)
        return builder.apply {
            extend(NOTIFICATION_TEMPLATE_FOREGROUND).build()
        }.build()
    }

    private fun NotificationCompat.Builder.setContentIntent(activity: String) {
        val intent = Intent(ctx, Class.forName(activity)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_VIEW
        }
        setContentIntent(PendingIntent.getActivity(ctx, 0, intent, 0))
    }

    internal object ForegroundNotification{
        const val FOREGROUND_CHANNEL_DESCRIPTION = "Foreground Work"
        const val FOREGROUND_CHANNEL_GROUP_DESCRIPTION = "For temporary tasks"
        const val FOREGROUND_CHANNEL_GROUP_NAME = "Foreground Processing"
        const val FOREGROUND_CHANNEL_GROUP_ID = "666"
        const val FOREGROUND_NOTIFICATION_GROUP_ID = "666"
        const val FOREGROUND_DEFAULT_SMALL_ICON = android.R.drawable.sym_def_app_icon
        const val FOREGROUND_ID = 666
        const val FOREGROUND_CHANNEL_ID = "foreground"
        const val FOREGROUND_CHANNEL_NAME = "Foreground Work"
        const val DEFAULT_FOREGROUND_TITLE = "Background Processing"
        const val DEFAULT_FOREGROUND_CONTENT = "Work in progress"
        const val DEFAULT_FOREGROUND_ICON = android.R.drawable.stat_sys_upload
        val NOTIFICATION_TEMPLATE_FOREGROUND = NotificationCompat.Extender() {
            it.run {
                setContentTitle(DEFAULT_FOREGROUND_TITLE)
                setContentText(DEFAULT_FOREGROUND_CONTENT)
                setSmallIcon(DEFAULT_FOREGROUND_ICON, 4)
                setPriority(NotificationCompat.PRIORITY_DEFAULT)
                setOngoing(true)
                setOnlyAlertOnce(true)
                setNotificationSilent()
                setShowWhen(false)
                setCategory(NotificationCompat.CATEGORY_SERVICE)
                setStyle(NotificationCompat.BigTextStyle())
                setAutoCancel(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setColorized(true)
                    setColor(Color.TRANSPARENT)
                }
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setChannelId(FOREGROUND_CHANNEL_ID)
                }
                setGroup(FOREGROUND_NOTIFICATION_GROUP_ID)
                setGroupSummary(false)
                setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
                setDefaults(NotificationCompat.DEFAULT_ALL)
            }
        }
        internal fun createForegroundChannel(ctx: Context, mgr: NotificationManagerCompat): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var channel: NotificationChannel? = mgr.getNotificationChannel(FOREGROUND_CHANNEL_ID)
                if (channel == null) {
                    channel = NotificationChannel(
                        FOREGROUND_CHANNEL_ID,
                        FOREGROUND_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    (channel).apply {
                        description = FOREGROUND_CHANNEL_DESCRIPTION
                        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                        setShowBadge(false)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                            setAllowBubbles(false)
                        enableLights(false)
                        enableVibration(false)
                        lightColor = ctx.getColor(android.R.color.holo_green_dark)
                        val audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build()
                        setSound(RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        createForegroundChannelGroup(mgr, channel)
                    mgr.createNotificationChannel(channel)
                    return true
                }
            }
            return false
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createForegroundChannelGroup(mgr: NotificationManagerCompat, channel: NotificationChannel?) {
            val foregroundGroup =
                NotificationChannelGroup(
                    FOREGROUND_CHANNEL_GROUP_ID,
                    FOREGROUND_CHANNEL_GROUP_NAME
                )
            mgr.createNotificationChannelGroup(foregroundGroup)
            channel!!.group = foregroundGroup.id
            foregroundGroup.apply {
                channels.add(channel)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    description = FOREGROUND_CHANNEL_GROUP_DESCRIPTION
                }
            }
        }
    }

    internal object WorkNotification {
        const val BACKGROUND_STARTED_DEFAULT_TITLE = "Background Service Running"
        const val BACKGROUND_STARTED_DEFAULT_CONTENT = "Working in the background"
        const val BACKGROUND_STARTED_DEFAULT_SMALL_ICON = android.R.drawable.stat_sys_download
        const val BACKGROUND_FINISHED_DEFAULT_TITLE = "Background Service Finished"
        const val BACKGROUND_FINISHED_DEFAULT_CONTENT = "Finished"
        const val BACKGROUND_FINISHED_DEFAULT_SMALL_ICON = android.R.drawable.stat_sys_download_done
        const val BACKGROUND_CHANNEL_DESCRIPTION = "Displays notifications for events regarding background work."
        const val BACKGROUND_CHANNEL_GROUP_DESCRIPTION = "Displays notifications for events regarding background work being executed in the background of the device while the app is not open."
        const val BACKGROUND_CHANNEL_GROUP_NAME = "Background Service"
        const val BACKGROUND_CHANNEL_GROUP_ID = "999"
        const val BACKGROUND_CHANNEL_ID = "background"
        const val BACKGROUND_CHANNEL_NAME = "Background Service"
        val TEMPLATE_BACKGROUND = NotificationCompat.Extender() {
            it.setContentInfo("Processing Data in background")
            it.setCategory(NotificationCompat.CATEGORY_PROGRESS)
            it.setShowWhen(true)
            it.setPriority(NotificationCompat.PRIORITY_HIGH)
            it.setAllowSystemGeneratedContextualActions(true)
            it.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            it.setAutoCancel(false)
            it.setOnlyAlertOnce(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.setColorized(true)
                it.setColor(Color.TRANSPARENT)
            }
            it.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.setChannelId(BACKGROUND_CHANNEL_ID)
            }
            it.setGroup(BACKGROUND_CHANNEL_GROUP_ID)
            it.setGroupSummary(false)
            it.setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            it.setDefaults(NotificationCompat.DEFAULT_ALL)
        }
        val TEMPLATE_START = NotificationCompat.Extender {
            it.extend(TEMPLATE_BACKGROUND)
            it.setProgress(100, 0, true)
            it.setSmallIcon(BACKGROUND_STARTED_DEFAULT_SMALL_ICON)
        }
        val TEMPLATE_FINISH = NotificationCompat.Extender {
            it.extend(TEMPLATE_BACKGROUND)
            it.setContentText(BACKGROUND_FINISHED_DEFAULT_CONTENT)
            it.setProgress(100, 100, false)
            it.setSmallIcon(BACKGROUND_FINISHED_DEFAULT_SMALL_ICON)
            it.setTimeoutAfter(15000)
        }

        internal fun createBackgroundChannel(ctx: Context, mgr: NotificationManagerCompat): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var channel: NotificationChannel? =
                    mgr.getNotificationChannel(BACKGROUND_CHANNEL_ID)
                if (channel == null) {
                    channel = NotificationChannel(BACKGROUND_CHANNEL_ID,BACKGROUND_CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT)
                    (channel).apply {
                        description = BACKGROUND_CHANNEL_DESCRIPTION
                        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                        setShowBadge(false)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                            setAllowBubbles(false)
                        enableLights(false)
                        enableVibration(false)
                        lightColor = ctx.getColor(android.R.color.holo_green_dark)
                        val audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build()
                        setSound(RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        createBackgroundChannelGroup(mgr, channel)
                    mgr.createNotificationChannel(channel)
                    return true
                }
            }
            return false
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createBackgroundChannelGroup(mgr: NotificationManagerCompat, channel: NotificationChannel?) {
            val backgroundGroup = NotificationChannelGroup(BACKGROUND_CHANNEL_GROUP_ID, BACKGROUND_CHANNEL_GROUP_NAME)
            mgr.createNotificationChannelGroup(backgroundGroup)
            channel!!.group = backgroundGroup.id
            backgroundGroup.apply {
                channels.add(channel)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    description = BACKGROUND_CHANNEL_GROUP_DESCRIPTION
                }
            }
        }
    }
}