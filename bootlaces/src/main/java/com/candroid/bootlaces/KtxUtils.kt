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

import android.app.NotificationManager
import android.content.Context
import androidx.datastore.preferences.MutablePreferences
import androidx.datastore.preferences.Preferences
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_SERVICE
import com.candroid.bootlaces.NotificationUtils.Configuration.FOREGROUND_ID
import kotlinx.coroutines.CoroutineScope
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
 **/

suspend fun <T: IBoot, R: Preferences> T.mapPrefsToBoot(prefs: R): T = this.apply {
    service = prefs[DataStoreKeys.PREF_KEY_SERVICE]
    activity = prefs[DataStoreKeys.PREF_KEY_ACTIVITY]
    title = prefs[DataStoreKeys.PREF_KEY_TITLE]
    content = prefs[DataStoreKeys.PREF_KEY_CONTENT]
    icon = prefs[DataStoreKeys.PREF_KEY_ICON]
}

suspend fun <T: IBoot,R: MutablePreferences> T.mapBootToMutPrefs(prefs: R): T = this.apply {
    service?.let { prefs[PREF_KEY_SERVICE] = it }
    activity?.let { prefs[DataStoreKeys.PREF_KEY_ACTIVITY] = it }
    title?.let { prefs[DataStoreKeys.PREF_KEY_TITLE] = it }
    content?.let { prefs[DataStoreKeys.PREF_KEY_CONTENT] = it }
    icon?.let { prefs[DataStoreKeys.PREF_KEY_ICON] = it }
}
