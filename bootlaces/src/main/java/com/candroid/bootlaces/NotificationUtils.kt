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

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
 * @date 10/09/20
 *
 **/
@ForegroundScope
class NotificationUtils @Inject constructor( val mgr: NotificationManagerCompat) {

    companion object{
        val FOREGROUND_CHANNEL_DESCRIPTION = "Foreground Work"
        val FOREGROUND_CHANNEL_GROUP_DESCRIPTION = "For temporary tasks"
        val FOREGROUND_CHANNEL_GROUP_NAME = "Foreground Processing"
        val FOREGROUND_CHANNEL_GROUP_ID = "666"
        val FOREGROUND_NOTIFICATION_GROUP_ID = "666"
        val FOREGROUND_DEFAULT_SMALL_ICON = android.R.drawable.sym_def_app_icon
        val FOREGROUND_ID = 666
        val FOREGROUND_CHANNEL_ID = "foreground"
        val FOREGROUND_CHANNEL_NAME = "Foreground Work"
        val DEFAULT_FOREGROUND_TITLE = "Background Processing"
        val DEFAULT_FOREGROUND_CONTENT = "Work in progress"
        val DEFAULT_FOREGROUND_ICON = android.R.drawable.stat_sys_download
        val BACKGROUND_STARTED_DEFAULT_TITLE = "Background Service Running"
        val BACKGROUND_STARTED_DEFAULT_CONTENT = "Working in the background"
        val BACKGROUND_STARTED_DEFAULT_SMALL_ICON = android.R.drawable.stat_sys_download
        val BACKGROUND_FINISHED_DEFAULT_TITLE = "Background Service Finished"
        val BACKGROUND_FINISHED_DEFAULT_CONTENT = "Finished work"
        val BACKGROUND_FINISHED_DEFAULT_SMALL_ICON = android.R.drawable.stat_sys_download_done
        val BACKGROUND_CHANNEL_DESCRIPTION =
            "Displays notifications for events regarding background work."
        val BACKGROUND_CHANNEL_GROUP_DESCRIPTION =
            "Displays notifications for events regarding background work being executed in the background of the device while the app is not open."
        val BACKGROUND_CHANNEL_GROUP_NAME = "Background Service"
        val BACKGROUND_CHANNEL_GROUP_ID = "999"
        val BACKGROUND_NOTIFICATION_GROUP_ID = "888"
        val BACKGROUND_ID = 66
        val BACKGROUND_CHANNEL_ID = "background"
        val BACKGROUND_CHANNEL_NAME = "Background Service"


    }
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

    val NOTIFICATION_TEMPLATE_BACKGROUND = NotificationCompat.Extender() {
        it.run {
            setContentInfo("Processing Data in background")
            setCategory(NotificationCompat.CATEGORY_PROGRESS)
            it.setShowWhen(true)
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setAllowSystemGeneratedContextualActions(true)
            setStyle(NotificationCompat.DecoratedCustomViewStyle())
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setColorized(true)
                setColor(Color.TRANSPARENT)
            }
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setChannelId(BACKGROUND_CHANNEL_ID)
            }
            setGroup(BACKGROUND_NOTIFICATION_GROUP_ID)
            setGroupSummary(false)
            setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            setDefaults(NotificationCompat.DEFAULT_ALL)
        }
    }

    fun createForegroundChannel(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel: NotificationChannel? = mgr.getNotificationChannel(FOREGROUND_CHANNEL_ID)
            if (channel == null) {
                channel = NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    FOREGROUND_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                (channel as NotificationChannel).apply {
                    description = FOREGROUND_CHANNEL_DESCRIPTION
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setShowBadge(false)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                        setAllowBubbles(false)
                    enableLights(false)
                    enableVibration(false)
                    lightColor = ctx.resources.getColor(android.R.color.holo_green_dark)
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build()
                    setSound(
                        RingtoneManager.getActualDefaultRingtoneUri(
                            ctx,
                            RingtoneManager.TYPE_NOTIFICATION
                        ), audioAttributes
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    createForegroundChannelGroup(channel)
                this.mgr.createNotificationChannel(channel!!)
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createForegroundChannelGroup(channel: NotificationChannel?) {
        val foregroundGroup =
            NotificationChannelGroup(FOREGROUND_CHANNEL_GROUP_ID, FOREGROUND_CHANNEL_GROUP_NAME)
        mgr.createNotificationChannelGroup(foregroundGroup)
        channel!!.group = foregroundGroup.id
        foregroundGroup?.apply {
            channels.add(channel)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                description = FOREGROUND_CHANNEL_GROUP_DESCRIPTION
            }
        }
    }


    fun createBackgroundChannel(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel: NotificationChannel? = mgr.getNotificationChannel(BACKGROUND_CHANNEL_ID)
            if (channel == null) {
                channel = NotificationChannel(BACKGROUND_CHANNEL_ID, BACKGROUND_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                (channel as NotificationChannel).apply {
                    description = BACKGROUND_CHANNEL_DESCRIPTION
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setShowBadge(false)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                        setAllowBubbles(false)
                    enableLights(false)
                    enableVibration(false)
                    lightColor = ctx.resources.getColor(android.R.color.holo_green_dark)
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build()
                    setSound(
                        RingtoneManager.getActualDefaultRingtoneUri(
                            ctx,
                            RingtoneManager.TYPE_NOTIFICATION
                        ), audioAttributes
                    )
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    createBackgroundChannelGroup(channel)
                mgr.createNotificationChannel(channel!!)
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBackgroundChannelGroup(channel: NotificationChannel?) {
        val backgroundGroup =
            NotificationChannelGroup(BACKGROUND_CHANNEL_GROUP_ID, BACKGROUND_CHANNEL_GROUP_NAME)
        mgr.createNotificationChannelGroup(backgroundGroup)
        channel!!.group = backgroundGroup.id
        backgroundGroup?.apply {
            channels.add(channel)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                description = BACKGROUND_CHANNEL_GROUP_DESCRIPTION
            }
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