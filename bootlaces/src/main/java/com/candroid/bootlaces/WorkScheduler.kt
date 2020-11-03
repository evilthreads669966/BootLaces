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
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
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
 * activates [WorkService]
 **/
@FlowPreview
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@ActivityScoped
class WorkScheduler @Inject constructor(@ApplicationContext val ctx: Context, val dataStore: DataStore<Preferences>) {
    @Throws(SchedulerActivationException::class)
    suspend fun schedulePersistent(worker: Worker){
        val work = Work( worker.id, worker::class.java.name)
        sendWorkRequest(work, Actions.ACTION_WORK_PERSISTENT)
        persistWorkService()
    }

    private fun persistWorkService(){
        val componentName = ComponentName(ctx, BootReceiver::class.java)
        val state = ctx.packageManager.getComponentEnabledSetting(componentName)
        if(state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            ctx.packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    @Throws(SchedulerActivationException::class)
    suspend fun scheduleOneTime(worker: Worker){
        val work = Work( worker.id, worker::class.java.name)
        sendWorkRequest(work, Actions.ACTION_WORK_ONE_TIME)
    }

    suspend fun activate(serviceName: String){
        if (WorkService.isStarted()) return
        dataStore.edit {
            if(it[StoreKeys.PREF_KEY] == null)
                it[StoreKeys.PREF_KEY] = serviceName
        }
    }

    @Throws(SchedulerActivationException::class)
    private suspend fun sendWorkRequest(work: Work, action: Actions){
        val serviceName = dataStore.data.firstOrNull()?.get(StoreKeys.PREF_KEY) ?: throw SchedulerActivationException()
        val intent = IntentFactory.createWorkServiceIntent(ctx, work, action, serviceName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent)
        else
            ctx.startService(intent)
    }
}