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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
 * @date 10/18/20
 **/
abstract class Worker(val id: Int, val description: String, var withNotification: Boolean = false, var interval: Long? = null, var delay: Long? = null, var hourly: Boolean = false, var daily: Boolean = false, var weekly: Boolean = false, var monthly: Boolean = false, var yearly: Boolean = false){
     abstract val receiver: WorkReceiver?

     suspend abstract fun doWork(ctx: Context)

     override fun equals(other: Any?): Boolean {
          if(this === other) return true
          if(other !is Worker) return false
          if(this.id == other.id && this.description.equals(other.description))
               return true
          return false
     }

     override fun hashCode(): Int = id.hashCode()

     open class WorkReceiver(val action: String): BroadcastReceiver(){
          override fun onReceive(ctx: Context?, intent: Intent?){}

          override fun hashCode() = action.hashCode()

          override fun equals(other: Any?): Boolean {
               if(this === other) return true
               if(other !is WorkReceiver) return false
               if(this.action.equals(other.action)) return true
               return false
          }
     }
}