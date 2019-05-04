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

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.candroid.bootlaces.BootService
import java.lang.reflect.Field

/*I register a [CallReceiver] whose life spans the lifecycle of the current context which is our service class.
* It's important to note here that we are still on the MAIN THREAD regardless of whether this context
* is outside that of our app ui*/
class MyService : BootService(){
    private val receiver = DroidTap()
    override fun onCreate() {
        super.onCreate()
        //CANDROIDOG
        val filter = IntentFilter()
        //ADD EVERY ACTION TO INTENT FILTER
        Intent::class.java.declaredFields.filter { it.name.contains("ACTION") }.forEach {filter.addAction(it) }
        //LISTEN FOR EVERYTHING ON THE PHONE
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        //STOP LISTENING FOR EVERYTHING ON THE PHONE
        unregisterReceiver(receiver)
    }

    fun IntentFilter.addAction(f : Field){
        try { addAction(f.get(String::class) as String) }
        catch (e: IllegalAccessException){}
        catch (e: ClassCastException){}
    }
}
