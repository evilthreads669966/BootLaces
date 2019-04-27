package com.candroid.bootlaces

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat

abstract class BootService : Service() {
    internal companion object {
        private var isRunning = false
        val KEY_NOTIFICATION_TITLE = "KEY_NOTIFICATION_TITLE"
        val KEY_NOTIFICATION_CONTENT = "KEY_NOTIFICATION_CONTENT"
        val KEY_NOTIFICATION_ICON = "KEY_NOTIFICATION_ICON"
        val KEY_CLICKED_ACTIVITY_NAME = "KEY_CLICKED_ACTIVITY_NAME"
        fun isRunning(): Boolean = isRunning
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(666, createNotification(this))
        return START_STICKY;
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    private fun createNotification(context: Context): Notification {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.channel_id),
                getString(R.string.channel_id),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context)
        with(PreferenceManager.getDefaultSharedPreferences(context)) {
            val title = getString(KEY_NOTIFICATION_TITLE, "candroidtb")
            val content = getString(KEY_NOTIFICATION_CONTENT, "boot laces")
            var icon = getInt(KEY_NOTIFICATION_ICON, -1)
            if (icon == -1) icon = android.R.drawable.sym_def_app_icon
            builder.setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(icon)
            getString(KEY_CLICKED_ACTIVITY_NAME, null)?.let {
                val intent = Intent(context, Class.forName(it))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.action = Intent.ACTION_VIEW
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                builder.setContentIntent(pendingIntent)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(getString(R.string.channel_id))
            return builder.build()
        }
    }
}
