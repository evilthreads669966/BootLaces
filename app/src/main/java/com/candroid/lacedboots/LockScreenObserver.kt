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
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.candroid.bootlaces.startBoot
import com.candroid.bootlaces.updateBoot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

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
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class LockScreenObserver(val ctx: AppCompatActivity): LifecycleObserver {
    private val mOverlay_request_code = 666

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun startService(){
        ctx.startBoot{
            service = LockService::class.qualifiedName
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                title = "I LOVE YOU"
                content = "Evil Threads love you one time!"
                activity = LockScreenActivity::class.qualifiedName
            }
        }
        ctx.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun requestOverlay() = checkPermission()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun updateForegroundNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            updateBoot(ctx){
                content = "Evil Threads love you ${ScreenVisibility.count()} times!"
            }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun kill() = ctx.kill()

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            if (!Settings.canDrawOverlays(ctx)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))
                ctx.startActivityForResult(intent, mOverlay_request_code)
            }
        }
    }
}

private fun AppCompatActivity.kill(){
    var overlay = true
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
        overlay = Settings.canDrawOverlays(this)
    if(overlay)
        finishAndRemoveTask()
}