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
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat

/**
 * Provides a persistent foreground notification and the ability to update it.
 *
 */
class BootNotification {
    companion object{
        val KEY_TITLE = "KEY_TITLE"
        val KEY_CONTENT = "KEY_CONTENT"
        val KEY_SMALL_ICON = "KEY_SMALL_ICON"
        val KEY_ACTIVITY_NAME = "KEY_ACTIVITY_NAME"
        /**
         * Reads in a map of key value pairs from a shared preferences file whose values are assigned to some of the propertiees of a [Notification]
         *
         * @return [Notification]
         */
        internal fun create(ctx : Context): Notification {
            createChannel(ctx)
            return createNotification(ctx, BootPreferences.getInstance(ctx))

        }

        /**
         * Update one or more of [BootService]'s notification's properties.
         *
         * @param ctx
         * @param [title] notification's title text field
         * @param [content] notification's body text field
         * @param [icon] notification's small icon field
         */
        fun update(ctx : Context, title : String? = null, content : String? = null, icon : Int = -1){
            with(BootPreferences.getInstance(ctx)){
                edit().apply{
                    title?.let { if(!getString(KEY_TITLE, ctx.getString(R.string.author)).equals(it)) putString(KEY_TITLE, it) }
                    content?.let { if(!getString(KEY_CONTENT, ctx.getString(R.string.author)).equals(it)) putString(KEY_CONTENT, it) }
                    if(icon != -1) if(getInt(KEY_SMALL_ICON, -1) != icon) putInt(KEY_SMALL_ICON, icon)
                }.apply()
                val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(ctx.resources.getInteger(R.integer.notification_id), createNotification(ctx, this))
            }
        }

        /**
         * Creates a notification whose properties are set from a map of key value pairs read in from a shared preferences file
         *
         * @param ctx
         * @param [prefs] this should be instantiated with a context provided by [BootPreferences]
         * @return [Notification]
         */
        private fun createNotification(ctx : Context, prefs : SharedPreferences ): Notification{
            val builder = NotificationCompat.Builder(ctx, ctx.getString(R.string.channel_id))
            var icon = prefs.getInt(KEY_SMALL_ICON, -1)
            if (icon == -1) icon = android.R.drawable.sym_def_app_icon
            builder.setContentTitle(prefs.getString(KEY_TITLE, ctx.getString(R.string.author)))
                .setContentText(prefs.getString(KEY_CONTENT, ctx.getString(R.string.author)))
                .setSmallIcon(icon)
                .setShowWhen(false)
                .setContentIntent(ctx, prefs)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(ctx.getString(R.string.channel_id))
            return builder.build()
        }

        /**
         * Creates the notification channel used by [BootService]'s persistent foreground notification
         *
         * @param ctx
         */
        private fun createChannel(ctx : Context){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if(notificationManager.getNotificationChannel(ctx.getString(R.string.channel_id)) == null){
                    val notificationChannel = NotificationChannel(ctx.getString(R.string.channel_id), ctx.getString(R.string.channel_id), NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(notificationChannel)
                }
            }
        }

        /**
         * Creates a pending intent that starts an activity.
         * The pending intent gets fired off when the notification is pressed.
         *
         * @param ctx
         * @param [prefs] this should be instantiated with a context provided by [BootPreferences]
         */
        private fun NotificationCompat.Builder.setContentIntent(ctx : Context, prefs : SharedPreferences){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) prefs.getString(KEY_ACTIVITY_NAME, null)?.let {
                val intent = Intent(ctx, Class.forName(it))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.action = Intent.ACTION_VIEW
                val pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0)
                setContentIntent(pendingIntent)
            }
        }
    }
}