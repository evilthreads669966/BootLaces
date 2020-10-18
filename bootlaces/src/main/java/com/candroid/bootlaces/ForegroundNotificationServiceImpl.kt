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

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import com.candroid.bootlaces.NotificationUtils.setContentIntent
import com.candroid.bootlaces.api.ForegroundNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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
/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/16/20
 *
 **/
class ForegroundNotificationServiceImpl @Inject constructor(override val scope: CoroutineScope, val ctx: Service, dataStore: DataStore<Preferences>) : ForegroundNotificationService<Notification,IBoot> {

    override val events: Flow<Preferences> = dataStore.data

    inline suspend fun subscribe(info: IBoot, crossinline subscribe: suspend (Flow<Preferences>) -> Unit){ scope.launch { subscribe(events) } }

    override fun startForeground(info: IBoot) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ctx.startForeground(NotificationUtils.Configuration.FOREGROUND_ID, create(info), ctx.foregroundServiceType) }
        else
            ctx.startForeground(NotificationUtils.Configuration.FOREGROUND_ID, create(info))
    }

    override fun update(info: IBoot) {
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(NotificationUtils.Configuration.FOREGROUND_ID, create(info))
    }

    override fun create(info: IBoot): Notification {
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
}