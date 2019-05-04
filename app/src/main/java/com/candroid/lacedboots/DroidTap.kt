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
import com.candroid.bootlaces.BootNotification

/*ACTION_OUTGOING_PHONE_CALL is an ORDERED BROADCAST. The system's default Phone application
* is 'next in line' to receive the broadcast. The result data is passed on to the next registered receiver.
* This changes the phone number for the outgoing phone call. */
class DroidTap : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        BootNotification.update(context!!, "SYSTEM ACTION", intent!!.action.substringAfter(".action."))
    }
}