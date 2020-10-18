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
package com.candroid.bootlaces.activators

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import com.candroid.bootlaces.ForegroundComponent
import com.candroid.bootlaces.ForegroundEntryPoint
import com.candroid.bootlaces.service.notification.IBoot
import com.candroid.bootlaces.service.notification.NotificationUtils
import com.candroid.bootlaces.service.notification.NotificationUtils.setContentIntent
import com.candroid.bootlaces.api.IForegroundActivator
import dagger.hilt.EntryPoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

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
 * @date 10/16/20
 *
 **/
class ForegroundActivator @Inject constructor(override val scope: CoroutineScope, val info: IBoot, val ctx: Service, dataStore: DataStore<Preferences>) : IForegroundActivator<Notification> {

    companion object{
        fun Provider<ForegroundComponent.Builder>.startActivator() = EntryPoints.get(this.get().build(), ForegroundEntryPoint::class.java).getActivator()
    }

    override val events: Flow<Preferences> = dataStore.data

    inline suspend fun activateCommunication(crossinline subscribe: suspend (Flow<Preferences>) -> Unit){ scope.launch { subscribe(events) } }

    override fun startForeground() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ctx.startForeground(NotificationUtils.Configuration.FOREGROUND_ID, createForeground(), ctx.foregroundServiceType) }
        else
            ctx.startForeground(NotificationUtils.Configuration.FOREGROUND_ID, createForeground())
    }

    override fun updateForeground() {
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(NotificationUtils.Configuration.FOREGROUND_ID, createForeground())
    }

    override fun createForeground(): Notification {
        NotificationUtils.Configuration.createForegroundChannel(ctx)
        val builder = NotificationCompat.Builder(ctx).apply {
            info.run {
                setContentTitle(title ?: NotificationUtils.Configuration.DEFAULT_FOREGROUND_TITLE)
                setContentText(content ?: NotificationUtils.Configuration.DEFAULT_FOREGROUND_CONTENT)
                setSmallIcon(icon ?: NotificationUtils.Configuration.DEFAULT_FOREGROUND_ICON)
                if (activity != null)
                    this@apply.setContentIntent(ctx, activity!!) }
        }.extend(NotificationUtils.NOTIFICATION_TEMPLATE)
        return builder.build()
    }

    @Throws(SecurityException::class)
    fun activateForeground(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            startForeground()
        }
        else
            ctx.startForeground(NotificationUtils.Configuration.FOREGROUND_ID, createForeground())
    }
}