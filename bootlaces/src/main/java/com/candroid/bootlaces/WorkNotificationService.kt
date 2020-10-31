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
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
 *
 **/
class WorkNotificationService: JobIntentService(){
    companion object{
        private var builder: NotificationCompat.Builder? = null
        private fun builder(ctx: Context): NotificationCompat.Builder{
            if(builder == null)
                builder = NotificationCompat.Builder(ctx)
            return builder!!
        }
        private var mgr: NotificationManagerCompat? = null
        private fun mgr(ctx: Context): NotificationManagerCompat{
            if(mgr == null)
                mgr = NotificationManagerCompat.from(ctx)
            return mgr!!
        }
        val ID_JOB = 666
        fun enqueue(ctx: Context, id: Int, intent: Intent) = enqueueWork(ctx, WorkNotificationService::class.java, id, intent)
        val KEY_DESCRIPTION = "KEY_DESCRIPTION"
        val KEY_TYPE = "KEY_TYPE"
        val KEY_ID = "KEY_ID"
        val TYPE_BACKGROUND_STARTED = "TYPE_BACKGROUND_STARTED"
        val TYPE_BACKGROUND_FINISHED = "TYPE_BACKGROUND_FINISHED"
        private val BACKGROUND_STARTED_DEFAULT_TITLE = "Background Service Running"
        private val BACKGROUND_STARTED_DEFAULT_CONTENT = "Working in the background"
        private val BACKGROUND_STARTED_DEFAULT_SMALL_ICON = android.R.drawable.stat_sys_download
        private val BACKGROUND_FINISHED_DEFAULT_TITLE = "Background Service Finished"
        private val BACKGROUND_FINISHED_DEFAULT_CONTENT = "Finished work"
        private val BACKGROUND_FINISHED_DEFAULT_SMALL_ICON = android.R.drawable.stat_sys_download_done
        private val BACKGROUND_CHANNEL_DESCRIPTION = "Displays notifications for events regarding background work."
        private val BACKGROUND_CHANNEL_GROUP_DESCRIPTION = "Displays notifications for events regarding background work being executed in the background of the device while the app is not open."
        private val BACKGROUND_CHANNEL_GROUP_NAME = "Background Service"
        private val BACKGROUND_CHANNEL_GROUP_ID = "999"
        private val BACKGROUND_NOTIFICATION_GROUP_ID = "888"
        private val BACKGROUND_ID = 66
        private val BACKGROUND_CHANNEL_ID = "background"
        private val BACKGROUND_CHANNEL_NAME = "Background Service"

        val NOTIFICATION_TEMPLATE_BACKGROUND = NotificationCompat.Extender() {
            it.run {
                setContentInfo("Processing Data in background")
                setCategory(NotificationCompat.CATEGORY_PROGRESS)
                setShowWhen(true)
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
                    setChannelId(NotificationUtils.FOREGROUND_CHANNEL_ID)
                }
                setGroup(NotificationUtils.FOREGROUND_CHANNEL_GROUP_ID)
                setGroupSummary(false)
                setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
                setDefaults(NotificationCompat.DEFAULT_ALL)
            }
        }
    }

    override fun onHandleWork(intent: Intent) {
        var description: String? = null
        var type: String? = null
        var id: Int? = null
        val extras = intent.extras
        extras?.run {
            if(containsKey(KEY_DESCRIPTION))
                description = getString(KEY_DESCRIPTION)
            if(containsKey(KEY_TYPE))
                type = getString(KEY_TYPE)
            if(containsKey(KEY_ID))
                id = getInt(KEY_ID)
        }
        val notification = createNotification(type!!, description)
        mgr(this).notify(BACKGROUND_CHANNEL_ID, id!!, notification)
    }

    fun createNotification(type: String,description: String? = "Doing work in the backround"): Notification {
        createBackgroundChannel(this)
        builder(this).apply {
            when (type) {
                TYPE_BACKGROUND_STARTED -> {
                    setProgress(100, 0, true)
                    setContentTitle(description ?: BACKGROUND_STARTED_DEFAULT_TITLE)
                    setContentText(description ?: BACKGROUND_STARTED_DEFAULT_CONTENT)
                    setSmallIcon(BACKGROUND_STARTED_DEFAULT_SMALL_ICON)
                    setProgress(100, 0, true)
                    this.extend(NOTIFICATION_TEMPLATE_BACKGROUND)
                }
                TYPE_BACKGROUND_FINISHED -> {
                    setProgress(100, 100, false)
                    setContentTitle(description ?: BACKGROUND_FINISHED_DEFAULT_TITLE)
                    setContentText("Finished")
                    setSmallIcon(BACKGROUND_FINISHED_DEFAULT_SMALL_ICON)
                    setTimeoutAfter(15000)
                    this.extend(NOTIFICATION_TEMPLATE_BACKGROUND)
                }
                else -> {
                }
            }
        }
        return builder(this).build()
    }

    fun createBackgroundChannel(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel: NotificationChannel? = mgr(this).getNotificationChannel(BACKGROUND_CHANNEL_ID)
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
                    setSound(RingtoneManager.getActualDefaultRingtoneUri(ctx, RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    createBackgroundChannelGroup(channel)
                mgr(this).createNotificationChannel(channel)
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createBackgroundChannelGroup(channel: NotificationChannel?) {
        val backgroundGroup = NotificationChannelGroup(BACKGROUND_CHANNEL_GROUP_ID, BACKGROUND_CHANNEL_GROUP_NAME)
        mgr(this).createNotificationChannelGroup(backgroundGroup)
        channel!!.group = backgroundGroup.id
        backgroundGroup.apply {
            channels.add(channel)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                description = BACKGROUND_CHANNEL_GROUP_DESCRIPTION
            }
        }
    }
}