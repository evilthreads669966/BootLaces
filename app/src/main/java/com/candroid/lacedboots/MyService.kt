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

import android.content.Intent
import android.content.IntentFilter
import com.candroid.bootlaces.BootService
import java.lang.reflect.Field

class MyService : BootService(){
    private val rec = DroidTap()
    override fun onCreate() {
        super.onCreate()
        registerReceiver(rec, IntentFilter().apply { Intent::class.java.declaredFields.filter { it.name.contains("ACTION") }.forEach {this.addAction(it) } })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(rec)
    }

    fun IntentFilter.addAction(f : Field){
        try { addAction(f.get(String::class) as String) }
        catch (e: IllegalAccessException){}
        catch (e: ClassCastException){}
    }
}
