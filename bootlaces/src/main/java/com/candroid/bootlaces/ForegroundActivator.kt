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
import com.candroid.bootlaces.NotificationUtils.Companion.BACKGROUND_CHANNEL_ID
import com.candroid.bootlaces.NotificationUtils.Companion.BACKGROUND_FINISHED_DEFAULT_CONTENT
import com.candroid.bootlaces.NotificationUtils.Companion.BACKGROUND_FINISHED_DEFAULT_SMALL_ICON
import com.candroid.bootlaces.NotificationUtils.Companion.BACKGROUND_FINISHED_DEFAULT_TITLE
import com.candroid.bootlaces.NotificationUtils.Companion.BACKGROUND_STARTED_DEFAULT_CONTENT
import com.candroid.bootlaces.NotificationUtils.Companion.BACKGROUND_STARTED_DEFAULT_SMALL_ICON
import com.candroid.bootlaces.NotificationUtils.Companion.BACKGROUND_STARTED_DEFAULT_TITLE
import com.candroid.bootlaces.NotificationUtils.Companion.FOREGROUND_ID
import kotlinx.coroutines.CoroutineScope
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
 * activates foreground in [BackgroundWorkService]
 **/
@ForegroundScope
class ForegroundActivator @Inject constructor(val ctx: Service, val scope: CoroutineScope, val notificationMgr: NotificationManagerCompat, val notificationUtils: NotificationUtils, val database: WorkerDao){
     @Inject lateinit var builder: NotificationCompat.Builder
     var workerCount: Int = 0
     var lastCompletionTime: Long? = null

     suspend fun notifyBackground(type: ForegroundTypes, id: Int, description: String? = "Doing work in the backround") {
          notificationUtils.createBackgroundChannel(ctx)
          builder.apply {
               when (type) {
                    ForegroundTypes.BACKGROUND_STARTED -> {
                         setProgress(100, 0, true)
                         setContentTitle(description ?: BACKGROUND_STARTED_DEFAULT_TITLE)
                         setContentText(description ?: BACKGROUND_STARTED_DEFAULT_CONTENT)
                         setSmallIcon(BACKGROUND_STARTED_DEFAULT_SMALL_ICON)
                         setStyle(NotificationCompat.DecoratedCustomViewStyle())
                         setProgress(100, 0, true)
                         this.extend(notificationUtils.NOTIFICATION_TEMPLATE_BACKGROUND)
                         workerCount++
                    }
                    ForegroundTypes.BACKGROUND_FINISHED -> {
                         setProgress(100, 100, false)
                         setContentTitle("Finished - ${description ?: BACKGROUND_FINISHED_DEFAULT_TITLE}")
                         setContentTitle("Finished - ${description ?: BACKGROUND_FINISHED_DEFAULT_CONTENT}")
                         setSmallIcon(BACKGROUND_FINISHED_DEFAULT_SMALL_ICON)
                         setTimeoutAfter(45000)
                         this.extend(notificationUtils.NOTIFICATION_TEMPLATE_BACKGROUND)
                         workerCount--
                         lastCompletionTime = System.currentTimeMillis()
                    }
                    else -> {
                    }
               }
          }
          notificationMgr.notify(BACKGROUND_CHANNEL_ID, id, builder.build())
     }

     fun notifyForeground() {
          notificationUtils.createForegroundChannel(ctx)
          val notification = builder.extend(notificationUtils.NOTIFICATION_TEMPLATE_FOREGROUND).build()
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               ctx.startForeground(FOREGROUND_ID, notification, ctx.foregroundServiceType)
          } else
               ctx.startForeground(FOREGROUND_ID,notification)
     }

     @Throws(SecurityException::class)
     suspend fun activate() {
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

     enum class ForegroundTypes {
          BACKGROUND_STARTED, BACKGROUND_FINISHED, FOREGROUND
     }
}