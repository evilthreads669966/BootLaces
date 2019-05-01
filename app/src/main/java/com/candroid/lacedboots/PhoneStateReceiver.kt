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

package com.candroid.lacedboots

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.candroid.bootlaces.BootNotification

class PhoneStateReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val state : String?
        when(intent?.extras?.getString(TelephonyManager.EXTRA_STATE)){
            TelephonyManager.EXTRA_STATE_RINGING -> state = "Receiving a call"
            TelephonyManager.EXTRA_STATE_OFFHOOK -> state = "In a call"
            TelephonyManager.EXTRA_STATE_IDLE -> state = "Not in a call"
            else -> state = null
        }
        state?.let { BootNotification.update(context!!, "Phone State", it, android.R.drawable.sym_action_call) }
    }
}