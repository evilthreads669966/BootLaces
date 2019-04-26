package com.candroid.bootlaces

import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat

class Laces{
    companion object{
        fun tie(context: Context, serviceName: String, notificationTitle: String = "candroid", notificationContent: String = "boot laces", notificationIcon: Int = -1, notificationClickActivity: Class<Any>? = null){
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val serviceClassName = preferences.getString(BootReceiver.KEY_SERVICE_CLASS_NAME, "null")
            if(serviceClassName.equals("null")){
                val editor = preferences.edit()
                preferences.getString(BootService.KEY_NOTIFICATION_TITLE, "candroid")
                preferences.getString(BootService.KEY_NOTIFICATION_CONTENT, "boot laces")
                preferences.getInt(BootService.KEY_NOTIFICATION_ICON, -1)
                preferences.getString(BootService.KEY_CLICKED_ACTIVITY_NAME, null)
                editor.putString(BootReceiver.KEY_SERVICE_CLASS_NAME, serviceName)
                editor.putString(BootService.KEY_NOTIFICATION_TITLE, notificationTitle)
                editor.putString(BootService.KEY_NOTIFICATION_CONTENT, notificationContent)
                editor.putInt(BootService.KEY_NOTIFICATION_ICON, notificationIcon)
                editor.putString(BootService.KEY_CLICKED_ACTIVITY_NAME, notificationClickActivity?.name)
                editor.apply()
            }
            if(!BootService.isRunning){
                val intent = Intent(context, Class.forName(serviceName))
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(intent)
                else
                    context.startService(intent)
            }
        }
    }
}