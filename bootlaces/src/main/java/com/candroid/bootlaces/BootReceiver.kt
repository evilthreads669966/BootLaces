package com.candroid.bootlaces

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager

class BootReceiver : BroadcastReceiver() {
    companion object{
        val SERVICE_CLASS_NAME_KEY = "SERVICE_CLASS_NAME_KEY"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)){
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val serviceClassName = preferences.getString(SERVICE_CLASS_NAME_KEY, "null")
            if(!serviceClassName.equals("null")){
                intent?.setClassName(context!!, serviceClassName)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    context?.startForegroundService(intent)
                }else
                    context?.startService(intent)
            }
         }
    }
}