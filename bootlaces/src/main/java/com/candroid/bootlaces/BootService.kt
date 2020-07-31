/*

Copyright 2019 Chris Basinger

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
limitations under the License.
*/
package com.candroid.bootlaces

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * BootService implementation is to be set to the configuration's service property in bootLaces method.
 * The BootService implementation will be started automatically at device boot and upon first using the app.
 * This class has a dependency on BootPreferences and BootNotification
 * Example usage:
 *
 * ```
 *  class MyService : BootService() {
 *      override fun onCreate() {
 *          //do something here
 *      }
 *  }
 *
 *  class MyActivity: AppCompatActivity{
 *      override fun onCreate(){
 *          bootLaces{
 *              it.service = MyService::class
 *          }
 *      }
 *
 *  }
 * ```
 */
abstract class BootService : NotificationService() {

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
        BootServiceState.setRunning()
    }

    override fun onDestroy() {
        super.onDestroy()
        BootServiceState.setStopped()
    }
}

/*base class that handles the creation of the persistent background service notification requirement*/
sealed class NotificationService: LifecycleService(){
   private val mUpdateReceiver by lazy { UpdateReceiver() }
    private val mDispatcher = ServiceLifecycleDispatcher(this)

    override fun getLifecycle() = mDispatcher.lifecycle

    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LocalBroadcastManager.getInstance(applicationContext).registerReceiver(mUpdateReceiver, IntentFilter(Actions.ACTION_UPDATE))
    }

    override fun onStart(intent: Intent?, startId: Int) {
        mDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            bootNotification()
    }


    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mUpdateReceiver)
    }

    inner class UpdateReceiver(): BroadcastReceiver(){
        override fun onReceive(ctx: Context?, intent: Intent?) {
            var title: String? = null
            var content: String? = null
            var icon: Int? = null
            if(intent!!.hasExtra(BootLacesRepository.KEY_TITLE)){
                title = intent.getStringExtra(BootLacesRepository.KEY_TITLE)
            }
            if(intent.hasExtra(BootLacesRepository.KEY_CONTENT)){
                content = intent.getStringExtra(BootLacesRepository.KEY_CONTENT)
            }
            if(intent.hasExtra(BootLacesRepository.KEY_SMALL_ICON)){
                icon = intent.getIntExtra(BootLacesRepository.KEY_SMALL_ICON, -1)
            }
            AppContainer.getInstance(ctx!!).service.update(title, content, icon ?: -1)
        }
    }
    /*create boot service notification*/
    private fun bootNotification() {
        BootLacesServiceImpl.createChannel(this)
        startForeground(BootLacesServiceImpl.getId(this), AppContainer.getInstance(this).service.create())
    }
}