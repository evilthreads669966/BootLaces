/*

Copyright 2019 Chris Basinger

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
*/
package com.candroid.bootlaces

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat

/**
 * Create a subclass and override onCreate to do any initializations such as registering receivers.
 * Anything you do in onStartCommand will delay the notification being posted.
 * However feel free to override any method you wish for customization.
 * [BootService] will be started whenever the device reboots.
 *
 * Example usage:
 *
 * ```
 *  class MyService : BootService() {
 *      override fun onCreate() {
 *          //do something here
 *      }
 *  }
 * ```
 */
abstract class BootService : Service() {
    internal companion object {
        val KEY_NOTIFICATION_TITLE = "KEY_NOTIFICATION_TITLE"
        val KEY_NOTIFICATION_CONTENT = "KEY_NOTIFICATION_CONTENT"
        val KEY_NOTIFICATION_ICON = "KEY_NOTIFICATION_ICON"
        val KEY_CLICKED_ACTIVITY_NAME = "KEY_CLICKED_ACTIVITY_NAME"
        private var isRunning = false
        fun isRunning(): Boolean = isRunning
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(666, createNotification())
        return START_STICKY;
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    /**
     * Reads in a map of key value pairs from a shared preferences file whose values are assigned to some of the propertiees of a [Notification]
     *
     * @return [Notification]
     */
    private fun createNotification(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(getString(R.string.channel_id), getString(R.string.channel_id), NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
            builder.setChannelId(getString(R.string.channel_id))
        }
        with(PreferenceManager.getDefaultSharedPreferences(this)) {
            var icon = getInt(KEY_NOTIFICATION_ICON, -1)
            if (icon == -1) icon = android.R.drawable.sym_def_app_icon
            builder.setContentTitle(getString(KEY_NOTIFICATION_TITLE, "candroidtb"))
                .setContentText(getString(KEY_NOTIFICATION_CONTENT, "boot laces"))
                .setSmallIcon(icon)
                .setShowWhen(false)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) getString(KEY_CLICKED_ACTIVITY_NAME, null)?.let {
                val intent = Intent(this@BootService, Class.forName(it))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.action = Intent.ACTION_VIEW
                val pendingIntent = PendingIntent.getActivity(this@BootService, 0, intent, 0)
                builder.setContentIntent(pendingIntent)
            }
            return builder.build()
        }
    }
}
