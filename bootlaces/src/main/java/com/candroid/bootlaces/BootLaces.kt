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

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
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

/*holds the configuration properties for the boot service*/
data class Configuration(
    var service: KClass<*>? = null,
    var notificationTitle: String = "evil threads",
    var notificationContent: String = "boot laces",
    var notificationIcon: Int = -1,
    var notificationClickActivity: Class<AppCompatActivity>? = null,
    var noPress: Boolean = false) {
    lateinit var ctx: AppCompatActivity
    val serviceName: String?
        get() =  service?.java?.name
}

data class BootNotification(
    var notificationTitle: String = "evil threads",
    var notificationContent: String = "boot laces",
    var notificationIcon: Int = -1
)


/*initialize bootservice with configuration properties*/
fun bootService(ctx: AppCompatActivity, payload: (suspend () -> Unit)? = null ,config: Configuration.() -> Unit){
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