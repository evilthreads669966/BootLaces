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
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

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
internal class BootRepository(ctx: Context) {
    private val dataStore: DataStore<Preferences> = ctx.applicationContext.createDataStore(name = NAME)

    companion object{
        val KEY_TITLE = "KEY_TITLE"
        val KEY_CONTENT = "KEY_CONTENT"
        val KEY_ICON = "KEY_ICON"
        val KEY_ACTIVITY = "KEY_ACTIVITY"
        val KEY_SERVICE = "KEY_SERVICE"
        val NAME = "myrepository"
        private val PREF_KEY_TITLE = preferencesKey<String>(KEY_TITLE)
        private val PREF_KEY_CONTENT = preferencesKey<String>(KEY_CONTENT)
        private val PREF_KEY_ICON = preferencesKey<Int>(KEY_ICON)
        private val PREF_KEY_SERVICE = preferencesKey<String>(KEY_SERVICE)
        private val PREF_KEY_ACTIVITY = preferencesKey<String>(KEY_ACTIVITY)
    }

    fun getBootNotificationConfig() = dataStore.data.catch {
        if (it is IOException)
            emit(emptyPreferences())
    }.map { prefs ->
        BootServiceConfig().apply {
            service = prefs[PREF_KEY_SERVICE]
            activity = prefs[PREF_KEY_ACTIVITY]
            title = prefs[PREF_KEY_TITLE]
            content = prefs[PREF_KEY_CONTENT]
            icon = prefs[PREF_KEY_ICON] ?: android.R.drawable.sym_def_app_icon
        }
    }

    suspend fun setNotification(service: String?, activity: String?, title: String?, content: String?, icon: Int?) = dataStore.edit { prefs ->
        if (service != null)
            prefs[PREF_KEY_SERVICE] = service
        if (activity != null)
            prefs[PREF_KEY_ACTIVITY] = activity
        if (title != null)
            prefs[PREF_KEY_TITLE] = title
        if (content != null)
            prefs[PREF_KEY_CONTENT] = content
        if (icon != null)
            prefs[PREF_KEY_ICON] = icon
    }
}

internal data class BootServiceConfig(var service: String? = null, var activity: String? = null, var title: String? = null, var content: String? = null, var icon: Int?  = null)