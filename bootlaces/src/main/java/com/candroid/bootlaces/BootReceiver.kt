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

/**
 * Listens for various BOOT action system broadcasts and when received starts a service
 * whose name is a value mapped to a key in a shared preferences file.The service's name
 * value is initialized to a parameter passed in to the [Laces.tie] method
 *
 */
internal class BootReceiver : BroadcastReceiver() {
    companion object{
        val KEY_SERVICE_CLASS_NAME = "KEY_SERVICE_CLASS_NAME"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if(!BootService.isRunning()){
            with(BootPreferences.getInstance(context!!)){
                getString(KEY_SERVICE_CLASS_NAME, "null")?.let {
                    if(!it.equals("null")){
                        intent?.setClassName(context!!, it)
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            context?.startForegroundService(intent)
                        else
                            context?.startService(intent)
                    }
                }
            }
        }
    }
}