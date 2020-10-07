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
package com.candroid.bootlaces

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
/**
 * BootService implementation is to be set to the configuration's service property in bootLaces method.
 * The BootService implementation will be started automatically at device boot and upon first using the app.
 * This class has a dependency on BootPreferences and BootNotification
 * Example usage:
 *
 * ```
 *  class MyService : BootService() {
 *      override fun onCreate() {
 *          //do something here
 *      }
 *  }
 *
 *  class MyActivity: AppCompatActivity{
 *      override fun onCreate(){
 *          bootLaces{
 *              it.service = MyService::class
 *          }
 *      }
 *
 *  }
 * ```
 */
/*a non-lifecycle aware BootService component. It does not require any libraries*/
abstract class BootService : Service() {
    private val notifProxy = NotificationProxy()

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        BootServiceState.setRunning()
        notifProxy.onStart(this)
    }

    override fun onCreate() {
        super.onCreate()
        notifProxy.onCreate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        BootServiceState.setStopped()
        notifProxy.onDestroy(this)
    }
}

internal var deferredPayload: (suspend () -> Unit)? = null

/*a lifecycle aware BootService component that allows for registering an observable. It requires the androidx lifecycle-services library*/
abstract class LifecycleBootService: LifecycleService() {
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    private val notifProxy = NotificationProxy()

    init {
        lifecycleScope.launchWhenCreated {
            deferredPayload?.invoke()
        }
    }

    override fun getLifecycle() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        notifProxy.onCreate(this)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        mDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
        notifProxy.onStart(this)
    }

    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
        notifProxy.onDestroy(this)
    }
}

/*everything related to handling the persistent foreground notification is delegated to this object*/
internal class NotificationProxy{
    private val mUpdateReceiver by lazy { UpdateReceiver() }

    fun onStart(ctx: Service){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            bootNotification(ctx)
    }

    fun onCreate(ctx: Service){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LocalBroadcastManager.getInstance(ctx).registerReceiver(mUpdateReceiver, IntentFilter(Actions.ACTION_UPDATE))
    }

    fun onDestroy(ctx: Service){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(mUpdateReceiver)
    }

    inner class UpdateReceiver(): BroadcastReceiver(){
        override fun onReceive(ctx: Context?, intent: Intent?) {
            var title: String? = null
            var content: String? = null
            var icon: Int? = null
            if(intent!!.hasExtra(BootLacesRepository.KEY_TITLE)){
                title = intent.getStringExtra(BootLacesRepository.KEY_TITLE)
            }
            if(intent.hasExtra(BootLacesRepository.KEY_CONTENT)){
                content = intent.getStringExtra(BootLacesRepository.KEY_CONTENT)
            }
            if(intent.hasExtra(BootLacesRepository.KEY_SMALL_ICON)){
                icon = intent.getIntExtra(BootLacesRepository.KEY_SMALL_ICON, -1)
            }
            AppContainer.getInstance(ctx!!).service.update(title, content, icon ?: -1)
        }
    }

    /*create boot service notification*/
    internal fun bootNotification(ctx: Service) {
        BootLacesServiceImpl.createChannel(ctx)
        ctx.startForeground(BootLacesServiceImpl.getId(ctx), AppContainer.getInstance(ctx).service.create())
    }
}