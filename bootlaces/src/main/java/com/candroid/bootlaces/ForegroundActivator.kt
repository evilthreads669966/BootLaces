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
import androidx.core.app.ServiceCompat
import com.candroid.bootlaces.api.IForegroundActivator
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
 * activates foreground in [BackgroundWorker]
 **/
class ForegroundActivator @Inject constructor(override val scope: CoroutineScope, val ctx: Service) : IForegroundActivator<Notification> {

     override fun update(type: ForegroundTypes, worker: FlowWorker?) {
          val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
          if(type == ForegroundTypes.FOREGROUND)
               mgr.notify(NotificationUtils.Configuration.FOREGROUND_ID, create(type, null))
          else
               if(worker != null)
                    mgr.notify(worker.id.toString(), NotificationUtils.Configuration.FOREGROUND_ID + worker.id, create(type, worker))
     }

     override fun create(type: ForegroundTypes, worker: FlowWorker?): Notification {
          NotificationUtils.Configuration.createForegroundChannel(ctx)
          val builder = NotificationCompat.Builder(ctx).apply {
               when(type){
                    ForegroundTypes.BACKGROUND -> {
                         setContentTitle("Worker ${worker?.id}")
                         setContentText(NotificationUtils.Configuration.DEFAULT_FOREGROUND_CONTENT)
                         setSmallIcon(NotificationUtils.Configuration.DEFAULT_FOREGROUND_ICON, 4)
                         setProgress(100, 0, true)
                         setContentInfo("Processing Data")
                         setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    }
                    ForegroundTypes.BACKGROUND_COMPLETE -> {
                         setContentTitle("Worker ${worker?.id}")
                         setContentText(NotificationUtils.Configuration.DEFAULT_FOREGROUND_COMPLETE_CONTENT)
                         setSmallIcon(NotificationUtils.Configuration.DEFAULT_FOREGROUND_COMPLETE_ICON, 4)
                         setContentInfo("Processing Data")
                         setTimeoutAfter(30000)
                         setAutoCancel(true)
                         setCategory(NotificationCompat.CATEGORY_STATUS)
                    }
                    ForegroundTypes.FOREGROUND -> {
                         setContentTitle(NotificationUtils.Configuration.DEFAULT_FOREGROUND_TITLE)
                         setContentText(NotificationUtils.Configuration.DEFAULT_FOREGROUND_CONTENT)
                         setSmallIcon(NotificationUtils.Configuration.DEFAULT_FOREGROUND_ICON, 4)
                         setPriority(NotificationCompat.PRIORITY_DEFAULT)
                         setOngoing(true)
                         setOnlyAlertOnce(true)
                         setNotificationSilent()
                         setShowWhen(false)
                         setCategory(NotificationCompat.CATEGORY_SERVICE)
                    }
                    else -> {}
               }
          }.extend(NotificationUtils.NOTIFICATION_TEMPLATE_BACKGROUND_WORK)
          return builder.build()
     }

     @Throws(SecurityException::class)
     override fun activate(){
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
               BootServiceState.setForeground()
               if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    ctx.startForeground(NotificationUtils.Configuration.FOREGROUND_ID, create(ForegroundTypes.FOREGROUND,null), ctx.foregroundServiceType) }
               else
                    ctx.startForeground(NotificationUtils.Configuration.FOREGROUND_ID, create(ForegroundTypes.FOREGROUND, null))
          }
     }

     override fun deactivate(){
          ServiceCompat.stopForeground(ctx,ServiceCompat.STOP_FOREGROUND_DETACH)
          /*val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              mgr.deleteNotificationChannel(NotificationUtils.Configuration.FOREGROUND_CHANNEL_ID)*/
          BootServiceState.setBackground()
     }
}

enum class ForegroundTypes{
     BACKGROUND, BACKGROUND_COMPLETE, FOREGROUND
}