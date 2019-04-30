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
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log

/**
 * BootStorage provides [getContext] which returns the context where device storage that is only accessible before the user enters there pin.
 *
 */
class BootStorage{
    companion object{
        /**
         * This provides you with the context to use when persisting data to the device that you would like to use inside of your service.
         * So any thing you want to read from in your service needs to be written with using the [Context] returned from this method.
         *
         * @param [context] the context of your service
         * @return [Context] the context where your device protected storage exists
         */
        fun getContext(context: Context): Context {
            val ctx : Context
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                ctx = context.createDeviceProtectedStorageContext()
                if(ctx.moveSharedPreferencesFrom(context, PreferenceManager.getDefaultSharedPreferencesName(context)))
                    Log.d("BootStorage", "preference migration successful")
            }else{
                return context
            }
            return ctx
        }
    }
}