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
        /**
         * Responsible for ensuring that your implementation of [BootService] starts not only the first time the application is run but also
         * everytime the device powers on from this point forward.
         *
         * @param [context] instance of your current [Context]
         * @param [serviceName] name of service subclassing [BootService]
         * @param [notificationTitle] title of your [BootService]'s notification
         * @param [notificationContent] content of your [BootService]'s notification
         * @param [notificationIcon] location of drawable resource for the small icon in your [BootService]'s notification
         * @param [notificationClickActivity] the activity to start when your [BootService]'s notification is pressed
         * @param [noClickMode] do nothing when your [BootService]'s notification is pressed
         */
        fun tie(context: Context, serviceName: String, notificationTitle: String = "candroid", notificationContent: String = "boot laces", notificationIcon: Int = -1, notificationClickActivity: Class<Any>? = null, noClickMode: Boolean = false){
            persistService(context, serviceName, notificationTitle, notificationContent, notificationIcon, notificationClickActivity, noClickMode)
            startService(context, serviceName)
        }

        /**
         * Creates a map of key value pairs required by [BootReceiver] to start [BootService] at device boot time and writes them to a file.
         * [BootService] also uses these values to create a customized persistent notification.
         *
         * @param [context] instance of your current [Context]
         * @param [serviceName] name of service subclassing [BootService]
         * @param [notificationTitle] title of your [BootService]'s notification
         * @param [notificationContent] content of your [BootService]'s notification
         * @param [notificationIcon] location of drawable resource for the small icon in your [BootService]'s notification
         * @param [notificationClickActivity] the activity to start when your [BootService]'s notification is pressed
         * @param [noClickMode] do nothing when your [BootService]'s notification is pressed
         */
        private fun persistService(context: Context, serviceName: String, notificationTitle: String, notificationContent: String, notificationIcon: Int, notificationClickActivity: Class<Any>?, noClickMode: Boolean){
            with(PreferenceManager.getDefaultSharedPreferences(BootStorage.getContext(context))){
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

        /**
         * Starts a service that subclasses [BootService] in the foreground if the device is running Oreo or greater.
         * Otherwise it gets started in the background.
         *
         * @param [context] instance of your current [Context]
         * @param [serviceName] name of service subclassing [BootService]
         */
        private fun startService(context: Context, serviceName: String){
            if(!BootService.isRunning()){
                val intent = Intent(context, Class.forName(serviceName))
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(intent)
                else
                    context.startService(intent)
            }
        }

        /**
         * Returns the name of the class file that contains the current scope of the instance of [Context] that was passed in.
         *
         * @param [context] instance of your current [Context]
         * @param [noClickMode] do nothing when your [BootService]'s notification is pressed
         * @return [String] the name of the java class containing the current context instance's scope
         */
        private fun getContextClassName(context : Context, noClickMode: Boolean): String?{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                if(context is AppCompatActivity && !noClickMode)
                    return context.javaClass.name
            }
            return null
        }
    }
}