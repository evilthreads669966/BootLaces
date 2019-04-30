/*
 * Copyright 2019 Chris Basinger
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.candroid.bootlaces

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat

class NotificationFactory {
    companion object{
        /**
         * Reads in a map of key value pairs from a shared preferences file whose values are assigned to some of the propertiees of a [Notification]
         *
         * @return [Notification]
         */
        fun createForegroundNotification(ctx : Context): Notification {
            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(ctx)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(ctx.getString(R.string.channel_id), ctx.getString(R.string.channel_id), NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(notificationChannel)
                builder.setChannelId(ctx.getString(R.string.channel_id))
            }
            with(PreferenceManager.getDefaultSharedPreferences(BootStorage.getContext(ctx))) {
                var icon = getInt(BootService.KEY_NOTIFICATION_ICON, -1)
                if (icon == -1) icon = android.R.drawable.sym_def_app_icon
                builder.setContentTitle(getString(BootService.KEY_NOTIFICATION_TITLE, "candroidtb"))
                    .setContentText(getString(BootService.KEY_NOTIFICATION_CONTENT, "boot laces"))
                    .setSmallIcon(icon)
                    .setShowWhen(false)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) getString(BootService.KEY_CLICKED_ACTIVITY_NAME, null)?.let {
                    val intent = Intent(ctx, Class.forName(it))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.action = Intent.ACTION_VIEW
                    val pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0)
                    builder.setContentIntent(pendingIntent)
                }
                return builder.build()
            }
        }
    }
}