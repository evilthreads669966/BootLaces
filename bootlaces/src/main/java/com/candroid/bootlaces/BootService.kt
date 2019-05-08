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

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Create a subclass and override onCreate to do any initializations such as registering receivers.
 * Anything you do in onStartCommand will delay the notification being posted.
 * However feel free to override any method you wish for customization.
 * [BootService] will be started whenever the device reboots.
 *
 * This service is direct boot aware. So any [SharedPreferences] you'd like to keep in this context please
 * use [BootPreferences].
 *
 * Example usage:
 *
 * ```
 *  class MyService : BootService() {
 *      override fun onCreate() {
 *          //do something here
 *      }
 *  }
 * ```
 */
abstract class BootService : Service() {
    internal companion object : State {
        private var state = States.STOPPED
        override fun getState(): States = state
        fun isRunning() = getState() == States.STARTED
    }

    override fun onCreate() {
        super.onCreate()
        state = States.STARTED
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        BootNotification.create(this)
        return START_STICKY;
    }

    override fun onBind(intent: Intent?): IBinder? { return null }

    override fun onDestroy() {
        super.onDestroy()
        state = States.STOPPED
    }
}
