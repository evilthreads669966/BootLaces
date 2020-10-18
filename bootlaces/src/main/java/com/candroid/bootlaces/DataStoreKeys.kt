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

import androidx.datastore.preferences.preferencesKey
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
 * @date 10/09/20
 *
 * Load Boot and save Boot. Persists Boot's configuration data to a file.
 **/
object DataStoreKeys {
        private val KEY_TITLE = "KEY_TITLE"
        private val KEY_CONTENT = "KEY_CONTENT"
        private val KEY_ICON = "KEY_ICON"
        private val KEY_ACTIVITY = "KEY_ACTIVITY"
        private val KEY_SERVICE = "KEY_SERVICE"
        val PREF_FILE_NAME = "boot.preferences_pb"
        val PREF_KEY_TITLE = preferencesKey<String>(KEY_TITLE)
        val PREF_KEY_CONTENT = preferencesKey<String>(KEY_CONTENT)
        val PREF_KEY_ICON = preferencesKey<Int>(KEY_ICON)
        val PREF_KEY_SERVICE = preferencesKey<String>(KEY_SERVICE)
        val PREF_KEY_ACTIVITY = preferencesKey<String>(KEY_ACTIVITY)
}