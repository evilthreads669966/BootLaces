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
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * [BootService] is an android service component that is automatically started when the device is turned on or rebooted.
 * [BootService] automatically handles configuring and creating a persistent foreground [Notification] in the notification drawer for you application.
 * A [Notification] that cannot be swiped away is required for all background services running on Android Oreo 8.0 and greater.
 * Subclass [BootService] and declaring the name of your derived service in the AndroidManifest.xml file with a service tag is all that is required.
 * The configuration data required by [BootService] and its' foreground notification are set inside of your MainActivity using the [bootService] function.
 * The only required configuration data for [BootService] is the name of your subclass that you create. [BootService] is not smart of enough to be able to find this out on its' own.
 * The name of your [BootService] subclass is used for starting your service when the device turns on or reboots. All other configuration data is optional besides the name of your subclass.
 **/
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
/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * [LifecycleBootService] only differs from [BootService] by subclassing [LifecycleService] instead of [Service].
 * This allows for two different features not available in [BootService]
 *   1. You can register a [LifecycleObserver] which you can read about in AndroidX jetpack documentation
 *   2. You can use lifecycleScope to launch coroutines in the init block of [LifecycleBootService]. These coroutines don't require you to worry about cleaning up as [LifecycleService] automatically stops any coroutine jobs in [Service.onStop] and [Service.onDestroy] callbacks
 * [bootService] takes an optional function as a named argument. It is named payload. You can pass a function to be ran in a coroutine that is launched in [LifecycleBootService.init] using [LifecycleScope.launchWhenCreated].
 * The named payload argument of type function will run in the background without you having to worry about stopping it or cleaning up resources. This allows for you to pass work to be done in [LifecycleBootService] from your activity.
 * This work will perform everytime the device turns on or reboots. So this will be an effective way of scheduling a periodic job to be ran.
 * [LifecycleBootService] requires you to add an additional dependency in your app's build.gradle file. The additional dependency is a jetpack library called [androidx.lifecycle:lifecycle-runtime-ktx]
 **/
abstract class LifecycleBootService: LifecycleService() {
    private val mDispatcher = ServiceLifecycleDispatcher(this)
    private val notifProxy = NotificationProxy()

    init {
        if(deferredPayload != null)
            lifecycleScope.launchWhenCreated {
                deferredPayload!!.invoke()
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

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * [NotificationProxy] is provides functions that shadow the lifecycle callbacks of the Android [Service] component.
 * [BootService] and [LifecycleBootService] have a [NotificationProxy] object member variable.
 * [NotificationProxy.onStart], [NotificationProxy.onDestroy], and [NotificationProxy.onDestroy] needs to called within [Service.onStart], [Service.onCreate], and [Service.onDestroy]
 * The callbacks are all you need to be aware of.
 * The persistent foreground notification is created inside of [onStart].
 * [NotificationProxy] also contains a [BroadcastReceiver] implementation named [UpdateReceiver]. This receiver is responsible for subscribing to the [Actions.ACTION_UPDATE] broadcast which is local to the app.
 * [Actions.ACTION_UPDATE] is sent from [bootNotification] everytime the foreground notification's content is updated. [bootNotification] takes in notification configuration data and persists it and then sends a broadcast to [UpdateReceiver] with this information.
 * [UpdateReceiver] is responsible for updating foreground notification title, body, and icon at runtime after it has already been created.
 * [NotificationProxy.bootNotification] is used internally to create a notification. It uses [BootLacesServiceImpl.create] function to create the foreground notification in [onStart]
 **/internal class NotificationProxy{
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