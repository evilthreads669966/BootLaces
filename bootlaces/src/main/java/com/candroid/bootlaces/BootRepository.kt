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
import android.util.Log
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException
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
 * @date 10/09/20
 *
 * Load Boot and save Boot. Persists Boot's configuration data to a file.
 **/
class BootRepository @Inject constructor(@ApplicationContext val ctx: Context) {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { File(ctx.applicationContext.filesDir, PREF_FILE_NAME).apply { createNewFile() } },
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    )

    companion object{
        val TAG = this::class.java.simpleName
        val KEY_TITLE = "KEY_TITLE"
        val KEY_CONTENT = "KEY_CONTENT"
        val KEY_ICON = "KEY_ICON"
        val KEY_ACTIVITY = "KEY_ACTIVITY"
        val KEY_SERVICE = "KEY_SERVICE"
        private val PREF_FILE_NAME = "bootlaces.preferences_pb"
        private val PREF_KEY_TITLE = preferencesKey<String>(KEY_TITLE)
        private val PREF_KEY_CONTENT = preferencesKey<String>(KEY_CONTENT)
        private val PREF_KEY_ICON = preferencesKey<Int>(KEY_ICON)
        private val PREF_KEY_SERVICE = preferencesKey<String>(KEY_SERVICE)
        private val PREF_KEY_ACTIVITY = preferencesKey<String>(KEY_ACTIVITY)
    }

    fun loadBoot(): Flow<IBoot> = dataStore.data.catch { e ->
        when(e){
            is IOException -> emit(emptyPreferences())
            else -> Log.e(TAG, e.message!!)
        }
    }.map { prefs ->
        Boot(null,null,null,null, null).apply {
            service = prefs[PREF_KEY_SERVICE]
            activity = prefs[PREF_KEY_ACTIVITY]
            title = prefs[PREF_KEY_TITLE]
            content = prefs[PREF_KEY_CONTENT]
            icon = prefs[PREF_KEY_ICON]
        }
    }

    suspend fun <T: IBoot>saveBoot(boot: T) = dataStore.edit { prefs ->
        boot.run {
            service?.let { prefs[PREF_KEY_SERVICE] = it }
            activity?.let { prefs[PREF_KEY_ACTIVITY] = it }
            title?.let { prefs[PREF_KEY_TITLE] = it }
            content?.let { prefs[PREF_KEY_CONTENT] = it }
            icon?.let { prefs[PREF_KEY_ICON] = it }
        }
    }
}