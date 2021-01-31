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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

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
 * creates intents
 **/
@Singleton
class IntentFactory @Inject constructor(@ApplicationContext private val ctx: Context){

    internal fun createWorkNotificationIntent(worker: Worker) = Intent().apply {
        setAction(Actions.ACTION_START.action)
        putExtra(NotificatonService.KEY_ID, worker.id)
        putExtra(NotificatonService.KEY_DESCRIPTION, worker.description)
    }

    internal fun createWorkIntent(work: Work, actions: Actions) = Intent().apply {
        setClass(ctx, WorkService::class.java)
        setAction(actions.action)
        putExtra(Work.KEY_PARCEL, work)
    }

    private fun createAlarmIntent(work: Work): Intent?{
        val intent = createWorkIntent(work, Actions.ACTION_EXECUTE_WORKER)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(PendingIntent.getForegroundService(ctx, work.id, intent, PendingIntent.FLAG_NO_CREATE) != null)
                return null
        }
        else{
            if(PendingIntent.getService(ctx, work.id, intent, PendingIntent.FLAG_NO_CREATE) != null)
                return null
        }
        return intent
    }
    
    internal fun createPendingIntent(work: Work): PendingIntent? {
        val intent = createAlarmIntent(work) ?: return null
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return PendingIntent.getForegroundService(ctx, work.id, intent, PendingIntent.FLAG_IMMUTABLE)
        else
            return PendingIntent.getService(ctx, work.id, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}
