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
import android.widget.RemoteViews
import androidx.core.app.JobIntentService
import com.candroid.bootlaces.NotificationFactory.WorkNotification.BACKGROUND_CHANNEL_ID
import dagger.hilt.android.AndroidEntryPoint
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
 * @date 10/31/20
 *
 **/
@AndroidEntryPoint
@ForegroundScope
class NotificatonService: JobIntentService(){
    @Inject lateinit var factory: NotificationFactory
    companion object{
        fun enqueue(ctx: Context, intent: Intent) = enqueueWork(ctx, NotificatonService::class.java, ID_JOB, intent)
        const val ID_JOB = 666
        const val KEY_DESCRIPTION = "KEY_DESCRIPTION"
        const val KEY_ID = "KEY_ID"
    }

    override fun onHandleWork(intent: Intent) {
        var description: String? = null
        var action: Actions? = null
        var id: Int? = null
        val extras = intent.extras
        extras?.run {
            if(containsKey(KEY_DESCRIPTION))
                description = getString(KEY_DESCRIPTION)
            if(containsKey(KEY_ID))
                id = getInt(KEY_ID)
        }
        action = Actions.valueOf(intent.action?: throw IllegalArgumentException("No action provided for notification service"))
        val notification = when(action) {
            Actions.ACTION_START -> { factory.createStartedNotification(description) }
            Actions.ACTION_FINISH -> { factory.createFinishedNotification(description) }
            else -> { throw RemoteViews.ActionException("Invalid action for notification service") }
        }
        factory.mgr.notify(BACKGROUND_CHANNEL_ID, id!!, notification)
    }
}