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
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.candroid.bootlaces.Laces
import com.kotlinpermissions.KotlinPermissions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            KotlinPermissions.with(this) // where this is an FragmentActivity instance
                .permissions(Manifest.permission.PROCESS_OUTGOING_CALLS)
                .onAccepted { permissions -> Laces.tie(this, MyService::class.java.name) }
                .onDenied { permissions -> recreate()}
                .onForeverDenied { permissions -> finish()}
                .ask()
        }else
            Laces.tie(this, MyService::class.java.name)
    }
}
