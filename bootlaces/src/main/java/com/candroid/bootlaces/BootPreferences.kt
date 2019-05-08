/*
 * Copyright 2019 Chris Basinger
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.candroid.bootlaces

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import com.candroid.bootlaces.BootPreferences.Companion.getInstance

/**
 * BootPreferences provides [getInstance] which returns the [SharedPreferences] file located in device protected storage
 * used for when Direct Boot is activated and the application needs to access it before the user enters
 * their device credentials
 *
 */
class BootPreferences{
    companion object{
        /**
         * This provides you with a [SharedPreferences] located in the device protected storage
         *
         * @param [context] the context of your service
         * @return [Context] the context where your device protected storage exists
         */
        fun getInstance(ctx: Context): SharedPreferences {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                with(ctx.createDeviceProtectedStorageContext()){
                    moveSharedPreferencesFrom(ctx, PreferenceManager.getDefaultSharedPreferencesName(ctx))
                    return PreferenceManager.getDefaultSharedPreferences(this)
                }
            else return PreferenceManager.getDefaultSharedPreferences(ctx)
        }
    }
}