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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager

internal class BootReceiver : BroadcastReceiver() {
    companion object{
        val KEY_SERVICE_CLASS_NAME = "KEY_SERVICE_CLASS_NAME"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        /*TODO BUG service not starting on boot on alcatel prepaid 8.1 phone. This is working on a Pixel 2 emulator running 9.0*/
        if(intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)){
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val serviceClassName = preferences.getString(KEY_SERVICE_CLASS_NAME, "null")
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