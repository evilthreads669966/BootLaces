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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
 **/data class Configuration(
    var service: KClass<*>? = null,
    var notificationTitle: String = "evil threads",
    var notificationContent: String = "boot laces",
    var notificationIcon: Int = -1,
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
    var notificationIcon: Int = -1
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
 **/fun bootService(ctx: Activity, payload: (suspend () -> Unit)? = null ,config: Configuration.() -> Unit){
    deferredPayload = payload
    configuration.run{
        this.ctx = ctx
        this.config()
    }
    requireNotNull(configuration.serviceName)
    ctx.save()
    ctx.load()
}

/*update title and/or content text and/or icon of boot service notification*/
fun bootNotification(ctx: Context, config: BootNotification.() -> Unit){
    val notifcation = BootNotification(configuration.notificationTitle, configuration.notificationContent, configuration.notificationIcon).apply{
        this.config()
    }
    val updateIntent = Intent().apply {
        action = Actions.ACTION_UPDATE
        putExtra(BootLacesRepository.Keys.KEY_TITLE, notifcation.notificationTitle)
        putExtra(BootLacesRepository.Keys.KEY_CONTENT, notifcation.notificationContent)
        putExtra(BootLacesRepository.Keys.KEY_SMALL_ICON, notifcation.notificationIcon)
    }
    LocalBroadcastManager.getInstance(ctx).sendBroadcast(updateIntent)
}

/*save boot service configuration properties for reboot*/
private fun Context.save(){
    val serviceClassName = AppContainer.getInstance(this).service.getServiceName()
    if(serviceClassName == null){
        AppContainer.getInstance(this).service.run {
            configuration.serviceName?.let { setServiceName(it) }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                setNotificationTitle(configuration.notificationTitle)
                setNotificationContent(configuration.notificationContent)
                setNotificationIcon(configuration.notificationIcon)
                configuration.takeUnless { it.noPress }?.let { setNotificationActivity(it.notificationClickActivity?.name ?: getContextClassName()!!) }
            }
        }
    }
}

/*start boot service*/
private fun Context.load(){
    if(BootServiceState.isStopped())
        with(Intent(this, Class.forName(configuration.serviceName ?: "null"))){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                this@load.startForegroundService(this)
            else
                this@load.startService(this)
        }
}

/*get class name for activity*/
private fun Context.getContextClassName(): String?{
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        if(!configuration.noPress) return javaClass.name
    return null
}