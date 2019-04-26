package com.candroid.bootlaces

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat

abstract class BootService : Service() {
    companion object{
        var isRunning = false
        val KEY_NOTIFICATION_TITLE = "KEY_NOTIFICATION_TITLE"
        val KEY_NOTIFICATION_CONTENT = "KEY_NOTIFICATION_CONTENT"
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(666,createNotification(this))
        return START_STICKY;
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    private fun createNotification(context : Context): Notification {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(getString(R.string.channel_id), getString(R.string.channel_id), NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val title = preferences.getString(KEY_NOTIFICATION_TITLE, "candroid")
        val content = preferences.getString(KEY_NOTIFICATION_CONTENT, "boot laces")
        val builder = NotificationCompat.Builder(context)
        builder.setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(getString(R.string.channel_id))
            return builder.build()
    }
}
