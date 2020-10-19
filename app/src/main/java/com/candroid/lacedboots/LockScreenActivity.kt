/*Copyright 2019 Chris Basinger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.candroid.lacedboots

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStateAtLeast
import com.candroid.bootlaces.BackgroundActivator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/*
            (   (                ) (             (     (
            )\ ))\ )    *   ) ( /( )\ )     (    )\ )  )\ )
 (   (   ( (()/(()/(  ` )  /( )\()|()/((    )\  (()/( (()/(
 )\  )\  )\ /(_))(_))  ( )(_)|(_)\ /(_))\((((_)( /(_)) /(_))
((_)((_)((_|_))(_))   (_(_()) _((_|_))((_))\ _ )(_))_ (_))
| __\ \ / /|_ _| |    |_   _|| || | _ \ __(_)_\(_)   \/ __|
| _| \ V /  | || |__    | |  | __ |   / _| / _ \ | |) \__ \
|___| \_/  |___|____|   |_|  |_||_|_|_\___/_/ \_\|___/|___/
....................../´¯/)
....................,/¯../
.................../..../
............./´¯/'...'/´¯¯`·¸
........../'/.../..../......./¨¯\
........('(...´...´.... ¯~/'...')
.........\.................'...../
..........''...\.......... _.·´
............\..............(
..............\.............\...
*/
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@AndroidEntryPoint
class LockScreenActivity: VisibilityActivity(){
    private val mOverlay_request_code = 666
    @InternalCoroutinesApi
    @Inject lateinit var mgr: BackgroundActivator

    init {
        lifecycleScope.launch {
            lifecycle.whenStateAtLeast(Lifecycle.State.STARTED) {
                mgr.activate(LockService::class.java.name)
            }
        }
        lifecycleScope.launchWhenResumed {
            mgr.scheduleWorker(1){
                Log.d("LOCK SERVICE", "WORKER FLOW WORKED")
                Log.d("FLOW WORKER", "${System.currentTimeMillis()}")
                delay(1000)
            }
            mgr.scheduleWorker(2){
                Log.d("LOCK SERVICE", "WORKER FLOW WORKED")
                Log.d("OTHER FLOW WORKER", "${System.currentTimeMillis()}")
                delay(5000)
                Log.d("OTHER FLOW WORKER", "${System.currentTimeMillis()}")
            }
            mgr.scheduleWorker(3){
                Log.d("LOCK SERVICE", "WORKER FLOW WORKED")
                Log.d("OTHER FLOW WORKER 3", "${System.currentTimeMillis()}")
                delay(5000)
                Log.d("OTHER FLOW WORKER 3", "${System.currentTimeMillis()}")
            }
            mgr.scheduleWorker(4){
                Log.d("LOCK SERVICE", "WORKER FLOW WORKED")
                Log.d("OTHER FLOW WORKER 4", "${System.currentTimeMillis()}")
                delay(5000)
            }
            mgr.scheduleWorker(5){
                Log.d("LOCK SERVICE", "WORKER FLOW WORKED")
                Log.d("WORKER 5", "${System.currentTimeMillis()}")
                delay(5000)
            }
        }
    }

/*    override fun onStart() {
        super.onStart()
        checkPermission()
    }*/

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${packageName}"))
                startActivityForResult(intent, mOverlay_request_code)
            }
        }
    }
}