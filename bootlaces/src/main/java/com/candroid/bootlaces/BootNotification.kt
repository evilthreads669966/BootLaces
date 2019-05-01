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

class BootNotification {
    companion object{
        val KEY_TITLE = "KEY_TITLE"
        val KEY_CONTENT = "KEY_CONTENT"
        val KEY_SMALL_ICON = "KEY_SMALL_ICON"
        val KEY_ACTIVITY_NAME = "KEY_ACTIVITY_NAME"
        val ID = 666
        /**
         * Reads in a map of key value pairs from a shared preferences file whose values are assigned to some of the propertiees of a [Notification]
         *
         * @return [Notification]
         */
        internal fun create(ctx : Context): Notification {
            createChannel(ctx)
            return createNotification(ctx, PreferenceManager.getDefaultSharedPreferences(BootStorage.getContext(ctx)))

        }

        fun update(ctx : Context, title : String? = null, content : String? = null, icon : Int = -1){
            with(PreferenceManager.getDefaultSharedPreferences(BootStorage.getContext(ctx))){
                edit().apply{
                    title?.let { putString(KEY_TITLE, it) }
                    content?.let { putString(KEY_CONTENT, it) }
                    if(icon != -1) putInt(KEY_SMALL_ICON, icon)
                }.apply()
                val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(ID, createNotification(ctx, this))
            }
        }

        private fun createNotification(ctx : Context, prefs : SharedPreferences ): Notification{
            val builder = NotificationCompat.Builder(ctx, ctx.getString(R.string.channel_id))
            var icon = prefs.getInt(KEY_SMALL_ICON, -1)
            if (icon == -1) icon = android.R.drawable.sym_def_app_icon
            builder.setContentTitle(prefs.getString(KEY_TITLE, "candroidtb"))
                .setContentText(prefs.getString(KEY_CONTENT, "boot laces"))
                .setSmallIcon(icon)
                .setShowWhen(false)
                .setActivityIntent(ctx, prefs)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(ctx.getString(R.string.channel_id))
            return builder.build()
        }

        private fun createChannel(ctx : Context){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if(notificationManager.getNotificationChannel(ctx.getString(R.string.channel_id)) == null){
                    val notificationChannel = NotificationChannel(ctx.getString(R.string.channel_id), ctx.getString(R.string.channel_id), NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(notificationChannel)
                }
            }
        }

        private fun NotificationCompat.Builder.setActivityIntent(ctx : Context, prefs : SharedPreferences){
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