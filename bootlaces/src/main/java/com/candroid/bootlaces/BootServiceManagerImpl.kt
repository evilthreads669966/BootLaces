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
import android.os.Build
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.edit
import com.candroid.bootlaces.api.BootServiceManager
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.runBlocking
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
 **/
@ActivityScoped
class BootServiceManagerImpl @Inject constructor(@ActivityContext private val ctx: Context, val info: IBoot, val datastore: DataStore<Preferences>, ) : BootServiceManager<IBoot> {

    override suspend inline fun updateForegroundNotification(crossinline config: suspend IBoot.() -> Unit){
        datastore.edit { prefs ->
            info.apply { config() }.run {
                service?.let { prefs[DataStoreKeys.PREF_KEY_SERVICE] = it }
                activity?.let { prefs[DataStoreKeys.PREF_KEY_ACTIVITY] = it }
                title?.let { prefs[DataStoreKeys.PREF_KEY_TITLE] = it }
                content?.let { prefs[DataStoreKeys.PREF_KEY_CONTENT] = it }
                icon?.let { prefs[DataStoreKeys.PREF_KEY_ICON] = it }
            }
        }
    }

    override suspend fun initialize(payload: (suspend () -> Unit)?, init: suspend IBoot.() -> Unit) = runBlocking {
        //LifecycleBootService.payload = payload
        if (info.service != null && BootServiceState.isRunning()) return@runBlocking
        val service = info.apply { init() }.service ?: throw BootException()
        val intent = Intent(ctx, Class.forName(service))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent)
        else
            ctx.startService(intent)
    }
}