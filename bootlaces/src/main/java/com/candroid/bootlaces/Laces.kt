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

import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity

class Laces{
    companion object{
        fun tie(context: Context, serviceName: String, notificationTitle: String = "candroid", notificationContent: String = "boot laces", notificationIcon: Int = -1, notificationClickActivity: Class<Any>? = null, noClickMode: Boolean = false){
            persistService(context, serviceName, notificationTitle, notificationContent, notificationIcon, notificationClickActivity, noClickMode)
            startService(context, serviceName)
        }

        private fun persistService(context: Context, serviceName: String, notificationTitle: String, notificationContent: String, notificationIcon: Int, notificationClickActivity: Class<Any>?, noClickMode: Boolean){
            with(PreferenceManager.getDefaultSharedPreferences(context)){
                val serviceClassName = getString(BootReceiver.KEY_SERVICE_CLASS_NAME, "null")
                if(serviceClassName.equals("null")) edit()?.apply {
                    putString(BootReceiver.KEY_SERVICE_CLASS_NAME, serviceName)
                    putString(BootService.KEY_NOTIFICATION_TITLE, notificationTitle)
                    putString(BootService.KEY_NOTIFICATION_CONTENT, notificationContent)
                    putInt(BootService.KEY_NOTIFICATION_ICON, notificationIcon)
                    putString(BootService.KEY_CLICKED_ACTIVITY_NAME, notificationClickActivity?.name ?: getContextClassName(context, noClickMode))
                }?.apply()
            }
        }

        private fun startService(context: Context, serviceName: String){
            if(!BootService.isRunning()){
                val intent = Intent(context, Class.forName(serviceName))
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(intent)
                else
                    context.startService(intent)
            }
        }

        private fun getContextClassName(context : Context, noClickMode: Boolean): String?{
            if(context is AppCompatActivity && !noClickMode)
                return context.javaClass.name
            else
                return null
        }
    }
}