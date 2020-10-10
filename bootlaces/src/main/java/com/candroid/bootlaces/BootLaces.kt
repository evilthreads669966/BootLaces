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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

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
private val configuration = Configuration()

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * [Configuration] is used to temporarily hold the configuration data that is persisted in [BootLacesRepository].
 * [bootService] takes a named function as an argument with a receiver object of type [Configuration].
 * It is only within the [bootService] function that you would ever access this object. You never instatiate [Configuration] on your own.
 * [Configuration] contains all [BootService] and foreground [Notification] data besides the notification channel and name.
 **/
data class Configuration(
    var service: KClass<*>? = null,
    var notificationTitle: String = "evil threads",
    var notificationContent: String = "boot laces",
    var notificationIcon: Int? = null,
    var notificationClickActivity: Class<Activity>? = null,
    var noPress: Boolean = false) {
    lateinit var ctx: Activity
    val serviceName: String?
        get() =  service?.java?.name
}

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * [BootNotification] is a data holder for [BootService] persistent foreground [Notification].
 * It holds the [Notification] title, body, and icon.
 * You only access [BootNotification] indirectly through the [bootNotification] function with a functional argument with a receiver of type [BootNotification].
 * Never instatiate [BootNotification]. You only use the accompanying [bootNotification] function to access its' properties.
 **/
data class BootNotification(
    var notificationTitle: String = "evil threads",
    var notificationContent: String = "boot laces",
    var notificationIcon: Int? = null
)


/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * [bootService] is a function that allows you to initialize [BootService] configuartion properties through a functional argument with a receiver of type [Configuration]
 * [bootService] should be called in your launcher [Activity.onCreate] or [Activity.onResume] lifecycle callbacks.
 * [bootService] is responsible for starting your [BootService] for the first time the application is used after install.
 * Once the device has been restarted, the [BootReceiver] will handle starting [BootService]
 **/
fun bootService(ctx: Activity, payload: (suspend () -> Unit)? = null ,config: Configuration.() -> Unit){
    deferredPayload = payload
    configuration.run{
        this.ctx = ctx
        this.config()
    }
    runBlocking {
        AppContainer.getInstance(ctx).repository.setNotification(configuration.serviceName, configuration.notificationClickActivity?.name, configuration.notificationTitle, configuration.notificationContent, configuration.notificationIcon)
        val bootNotifConfig = AppContainer.getInstance(ctx).repository.getBootNotificationConfig().firstOrNull()
        if(bootNotifConfig?.service != null){
            val intent = Intent(ctx, Class.forName(bootNotifConfig.service!!))
            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                ctx.startForegroundService(intent)
            else
                ctx.startService(intent)
        }
    }
}

/*update title and/or content text and/or icon of boot service notification*/
fun bootNotification(ctx: Context, config: BootNotification.() -> Unit){
    val notifcation = BootNotification(configuration.notificationTitle, configuration.notificationContent, configuration.notificationIcon).apply{
        this.config()
    }
    val updateIntent = Intent().apply {
        action = Actions.ACTION_UPDATE
        putExtra(BootRepository.KEY_TITLE, notifcation.notificationTitle)
        putExtra(BootRepository.KEY_CONTENT, notifcation.notificationContent)
        putExtra(BootRepository.KEY_ICON, notifcation.notificationIcon)
    }
    LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent)
}