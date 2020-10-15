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
import androidx.datastore.preferences.PreferenceDataStoreFactory
import androidx.datastore.preferences.Preferences
import com.candroid.bootlaces.DataStoreKeys.PREF_FILE_NAME
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_ACTIVITY
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_CONTENT
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_ICON
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_SERVICE
import com.candroid.bootlaces.DataStoreKeys.PREF_KEY_TITLE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
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
 **/
@Module
@InstallIn(ApplicationComponent::class)
object BootModule{
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences>{
        return  PreferenceDataStoreFactory.create(
            produceFile = { File(ctx.filesDir, PREF_FILE_NAME).apply { createNewFile() } },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }

    @Provides
    @Singleton
    fun provideAppCoroutineScope(): CoroutineScope
            = CoroutineScope(Dispatchers.Default + SupervisorJob() + CoroutineName("bootlaces"))

    @Provides
    @Singleton
    fun providesBoot(dataStore: DataStore<Preferences>): Boot = runBlocking {
        return@runBlocking dataStore.data.firstOrNull()?.let { prefs ->
            Boot(
                prefs[PREF_KEY_SERVICE],
                prefs[PREF_KEY_ACTIVITY],
                prefs[PREF_KEY_TITLE],
                prefs[PREF_KEY_CONTENT],
                prefs[PREF_KEY_ICON])
        } ?: Boot(null,null,null,null,null)
    }
}