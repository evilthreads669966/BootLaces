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

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
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
        internal fun <T : Service> create(ctx : T){
            createChannel(ctx)
            ctx?.startForeground(getId(ctx), createNotification(ctx, BootPreferences.getInstance(ctx)))
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
            with(BootPreferences.getInstance(ctx)){ edit().apply{
                title?.let { if(!getString(KEY_TITLE, ctx.getString(R.string.author)).equals(it)) putString(KEY_TITLE, it) }
                content?.let { if(!getString(KEY_CONTENT, ctx.getString(R.string.author)).equals(it)) putString(KEY_CONTENT, it) }
                if(icon != -1) if(getInt(KEY_SMALL_ICON, -1) != icon) putInt(KEY_SMALL_ICON, icon)
            }.apply()
                val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(getId(ctx), createNotification(ctx, this))
            }
        }

        /**
         * Creates a notification whose properties are set from a map of key value pairs read in from a shared preferences file
         *
         * @param ctx
         * @param [prefs] [SharedPreferences] provided by [BootPreferences]
         * @return [Notification]
         */
        private fun createNotification(ctx : Context, prefs : SharedPreferences ): Notification{
            with(NotificationCompat.Builder(ctx, getChannelId(ctx))){
                setContentTitle(prefs.getString(KEY_TITLE, ctx.getString(R.string.author)))
                setContentText(prefs.getString(KEY_CONTENT, ctx.getString(R.string.author)))
                setSmallIcon(prefs)
                setContentIntent(ctx, prefs)
                setShowWhen(false)
                setAutoCancel(false)
                setOngoing(true)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    setChannelId(getChannelId(ctx))
                return build()
            }
        }

        /**
         * Creates the notification channel used by [BootService]'s persistent foreground notification
         *
         * @param ctx
         */
        private fun createChannel(ctx : Context){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                with(ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager){
                    getNotificationChannel(getChannelId(ctx))?.let { return }
                    val notificationChannel = NotificationChannel(getChannelId(ctx), getChannelName(ctx), NotificationManager.IMPORTANCE_HIGH)
                    createNotificationChannel(notificationChannel)
                }
            }
        }

        /**
         * Creates a pending intent that starts an activity.
         * The pending intent gets fired off when the notification is pressed.
         *
         * @param ctx
         * @param [prefs] [SharedPreferences] provided by [BootPreferences]
         */
        private fun NotificationCompat.Builder.setContentIntent(ctx : Context, prefs : SharedPreferences){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) prefs.getString(KEY_ACTIVITY_NAME, null)?.let {
                with(Intent(ctx, Class.forName(it))){
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    action = Intent.ACTION_VIEW
                    setContentIntent(PendingIntent.getActivity(ctx, 0, this, 0))
                }

            }
        }

        private fun NotificationCompat.Builder.setSmallIcon(prefs : SharedPreferences){
            var icon = prefs.getInt(KEY_SMALL_ICON, -1)
            if (icon == -1) icon = android.R.drawable.sym_def_app_icon
            setSmallIcon(icon)
        }

        internal fun getId(ctx: Context) = ctx.resources.getInteger(R.integer.notification_id)

        internal fun getChannelId(ctx: Context) = ctx.resources.getString(R.string.channel_id)

        internal fun getChannelName(ctx: Context) = ctx.resources.getString(R.string.channel_name)
    }
}