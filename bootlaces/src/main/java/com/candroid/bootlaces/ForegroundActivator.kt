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
import androidx.core.app.ServiceCompat
import com.candroid.bootlaces.NotificationFactory.ForegroundNotification.FOREGROUND_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.Channel
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
 * activates foreground in [WorkService]
 **/
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class ForegroundActivator @Inject constructor(val ctx: Service){
     @Inject lateinit var factory: NotificationFactory
     private fun notifyForeground() {
          val notification = factory.createForegroundNotification()
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
               ctx.startForeground(FOREGROUND_ID, notification, ctx.foregroundServiceType)
          } else
               ctx.startForeground(FOREGROUND_ID,notification)
     }

     @FlowPreview
     @Throws(SecurityException::class)
     internal fun activate() {
          if(WorkService.state.equals(ServiceState.FOREGROUND)) return
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
               WorkService.state = ServiceState.FOREGROUND
               notifyForeground()
          }
     }

     @FlowPreview
     internal fun deactivate() {
          ServiceCompat.stopForeground(ctx, ServiceCompat.STOP_FOREGROUND_REMOVE)
          WorkService.state = ServiceState.BACKGROUND
     }
}