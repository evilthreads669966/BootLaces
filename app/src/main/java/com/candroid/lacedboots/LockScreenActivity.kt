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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import com.candroid.bootlaces.BootLaces
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
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
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@AndroidEntryPoint
class LockScreenActivity: VisibilityActivity(){
    private val mOverlay_request_code = 666
    @Inject lateinit var mObserver: LifecycleObserver
    @Inject lateinit var bootlaces: BootLaces
    override fun onStart() {
        super.onStart()
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        lifecycle.addObserver(mObserver)
        bootlaces.startBoot(this){
            service = LockService::class.qualifiedName
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                title = "I LOVE YOU"
                content = "Evil Threads love you one time!"
                activity = LockScreenActivity::class.qualifiedName
            }
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            bootlaces.updateBoot(this){
                content = "Evil Threads love you ${ScreenVisibility.count()} times!"
            }
    }

    override fun onPause() {
        super.onPause()
        lifecycle.removeObserver(mObserver)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${packageName}"))
                startActivityForResult(intent, mOverlay_request_code)
            }
        }
    }
}

