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
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.candroid.bootlaces.NotificationUtils.Companion.FOREGROUND_ID
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import kotlin.properties.Delegates

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
 * activates foreground in [BackgroundWorkService]
 **/
@ForegroundScope
class ForegroundActivator @Inject constructor(val ctx: Service, val scope: CoroutineScope, val notificationMgr: NotificationManagerCompat, val notificationUtils: NotificationUtils, val database: WorkerDao){
     @Inject lateinit var builder: NotificationCompat.Builder

     fun notifyForeground() {
          notificationUtils.createForegroundChannel(ctx)
          val notification = builder.extend(notificationUtils.NOTIFICATION_TEMPLATE_FOREGROUND).build()
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               ctx.startForeground(FOREGROUND_ID, notification, ctx.foregroundServiceType)
          } else
               ctx.startForeground(FOREGROUND_ID,notification)
     }

     @Throws(SecurityException::class)
     fun activate() {
          if(BootServiceState.isForeground())
               return
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
               BootServiceState.setForeground()
               notifyForeground()
          }
     }

     fun deactivate() {
          ServiceCompat.stopForeground(ctx, ServiceCompat.STOP_FOREGROUND_REMOVE)
          BootServiceState.setBackground()
     }
}