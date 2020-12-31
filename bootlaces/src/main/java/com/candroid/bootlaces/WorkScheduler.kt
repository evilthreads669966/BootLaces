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

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
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
 * @date 10/16/20
 *
 * activates [WorkService]
 **/
@FlowPreview
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@Singleton
class WorkScheduler @Inject constructor(@ApplicationContext val ctx: Context) {
    suspend fun schedulePersistent(worker: Worker){
        val work = Work( worker.id, worker::class.java.name)
        sendWorkRequest(ctx, work, Actions.ACTION_WORK_PERSISTENT)
        persistWorkService()
    }

    private fun persistWorkService(){
        val componentName = ComponentName(ctx, BootReceiver::class.java)
        val state = ctx.packageManager.getComponentEnabledSetting(componentName)
        if(state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            ctx.packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    suspend fun scheduleOneTime(worker: Worker){
        val work = Work( worker.id, worker::class.java.name)
        sendWorkRequest(ctx, work, Actions.ACTION_WORK_ONE_TIME)
    }

    suspend fun schedulePeriodic(interval: Long, worker: Worker){
        val work = Work( worker.id, worker::class.java.name, interval = interval)
        sendWorkRequest(ctx, work, Actions.ACTION_WORK_PERSISTENT)
        persistWorkService()
    }

    suspend fun scheduleFuture(delay: Long, worker: Worker){
        val work = Work(worker.id, worker::class.java.name, delay = delay)
        sendWorkRequest(ctx, work, Actions.ACTION_WORK_PERSISTENT)
        persistWorkService()
    }
}

@InternalCoroutinesApi
internal suspend fun sendWorkRequest(ctx: Context, work: Work, action: Actions){
    val intent = IntentFactory.createWorkServiceIntent(ctx, work, action)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        ctx.startForegroundService(intent)
    else
        ctx.startService(intent)
}