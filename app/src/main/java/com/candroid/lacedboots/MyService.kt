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
package com.candroid.lacedboots

import com.candroid.bootlaces.BootService
import com.candroid.bootlaces.BootNotification
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyService : BootService(){
    override fun onCreate() {
        super.onCreate()
        //waits ten seconds before updating notification with new information
        GlobalScope.launch {
            delay(10000)
            BootNotification.update(this@MyService, "new title", "new content", android.R.drawable.stat_sys_download)
        }
    }
}
