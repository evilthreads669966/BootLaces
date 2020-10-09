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
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
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
 * [BootLacesRepositoryImpl] implements the [BootLacesRepository] interface. [BootLacesServiceImpl] wraps the persistent storage and hides its' implementation from the user so no mistakes can happen.
 * The persistent storage being used is [SharedPreferences] which is just a file with key-value pairs.
 * [BootLacesRepositoryImpl] is where all [Configuration] and [BootNotification] properties are persisted after calling the [bootService] and [bootNotification] functions.
 * You may change or retreive this configuration data through this implementation of [BootLacesRepository].
 * You do not directly use any [BootLacesRepositoryImpl] getters or setters for the configuration data.
 * Instead you use [BootLacesServiceImpl] which provides shadowing functions for this repository.
 **/
class BootLacesRepositoryImpl(ctx: Context): BootLacesRepository(ctx){
    override fun getPreferences(): SharedPreferences {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val dpCtx = ctx.createDeviceProtectedStorageContext()
            dpCtx.moveSharedPreferencesFrom(ctx, PreferenceManager.getDefaultSharedPreferencesName(ctx))
            return PreferenceManager.getDefaultSharedPreferences(dpCtx)
        }
        return mPrefs
    }

    override fun putBootService(serviceName: String) = mPrefs.edit().putString(KEY_SERVICE_CLASS_NAME, serviceName).apply()

    override fun fetchBootService() = mPrefs.getString(KEY_SERVICE_CLASS_NAME, null)

    override fun putContent(content: String) = mPrefs.edit().putString(KEY_CONTENT, content).apply()

    override fun fetchContent() = mPrefs.getString(KEY_CONTENT, null)

    override fun putTitle(title: String) = mPrefs.edit().putString(KEY_TITLE, title).apply()

    override fun fetchTitle() = mPrefs.getString(KEY_TITLE, null)

    override fun putIcon(icon: Int) = mPrefs.edit().putInt(KEY_SMALL_ICON, icon).apply()

    override fun fetchIcon() = mPrefs.getInt(KEY_SMALL_ICON, -1)

    override fun putActivity(activityName: String) = mPrefs.edit().putString(KEY_ACTIVITY_NAME, activityName).apply()

    override fun fetchActivity() = mPrefs.getString(KEY_ACTIVITY_NAME, null)
}

/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 10/09/20
 *
 * [BootLacesRepository] provides an interface with getters and setters for [Configuration] and [BootNotification] properties.
 * [Configuration] and [BootNotification] properties are only accessed by the user from within the [bootService] [bootNotification] functions argument's receiver.
 * Instead you use [BootLacesServiceImpl] which provides shadowing functions for this repository.
 **/
sealed class BootLacesRepository(val ctx: Context){
    companion object Keys{
        val KEY_TITLE = "KEY_TITLE"
        val KEY_CONTENT = "KEY_CONTENT"
        val KEY_SMALL_ICON = "KEY_SMALL_ICON"
        val KEY_ACTIVITY_NAME = "KEY_ACTIVITY_NAME"
    }
    internal val mPrefs by lazy { PreferenceManager.getDefaultSharedPreferences(ctx) }

    internal abstract fun getPreferences(): SharedPreferences

    internal abstract fun putBootService(serviceName: String)

    internal abstract fun fetchBootService(): String?

    internal abstract fun putContent(content: String)

    internal abstract fun fetchContent(): String?

    internal abstract fun putTitle(title: String)

    internal abstract fun fetchTitle(): String?

    internal abstract fun putIcon(icon: Int)

    internal abstract fun fetchIcon(): Int?

    internal abstract fun putActivity(activityName: String)

    internal abstract fun fetchActivity(): String?
}