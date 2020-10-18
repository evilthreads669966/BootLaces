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
package com.candroid.bootlaces.api

import android.app.Activity
import android.app.Notification
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.lifecycle.LifecycleCoroutineScope
import com.candroid.bootlaces.IBoot
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

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
interface BootServiceManager<out T: IBoot>{
    suspend fun initialize(payload: (suspend () -> Unit)? = null, init: suspend T.() -> Unit)

    suspend  fun updateForegroundNotification(config: suspend T.() -> Unit)
}


interface ForegroundNotificationService<out T: Notification,in S: IBoot> {
    val events: Flow<Preferences>

    val scope: CoroutineScope

    fun startForeground(info: S)

    fun  create(info: S): T

    fun update(info: S)
}

inline suspend fun <T : Notification, S : IBoot> ForegroundNotificationService<T, S>.subscribe(info: IBoot, crossinline subscribe: suspend (Flow<Preferences>) -> Unit) {
    subscribe(events)
}

interface EventBus<out T,in S>{
    val bus: T
    suspend fun emit(info: S)
    interface EventBusConsumer{
        suspend fun LifecycleCoroutineScope.subscribe()
    }
}


interface IBootLaces<out T: IBoot,out R: DataStore<Preferences>> {
    val scope: CoroutineScope
    val boot: T
    val dataStore: R

    suspend fun updateBoot(config: T.() -> Unit): Unit

    suspend fun startBoot(ctx: Activity, payload: (suspend () -> Unit)?, init: T.() -> Unit): Boolean
}

interface SimpleFactory<out T,in R>{
    fun new(): T
    fun create(): T
}