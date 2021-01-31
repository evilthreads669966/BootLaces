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

import android.app.AlarmManager
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.DefineComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

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
 * @date 10/31/20
 *
 **/
@Module
@InstallIn(SingletonComponent::class)
internal object GlobalModule {
    /*I'm getting a wierd error using a provider for Room database so this is a hack to get a singleton working*/
    @Provides
    fun provideWorkDao(@ApplicationContext ctx: Context): WorkDao = WorkDatabase.getInstance(ctx).workerDao()
    @Provides
    @Singleton
    fun provideAlarmMgr(@ApplicationContext ctx: Context) = ctx.getSystemService(Service.ALARM_SERVICE) as AlarmManager
    @Provides
    @Singleton
    fun provideNotificationBuilder(@ApplicationContext ctx: Context) = NotificationCompat.Builder(ctx)
    @Provides
    @Singleton
    fun provideNotificationMgr(@ApplicationContext ctx: Context) = NotificationManagerCompat.from(ctx)
}

@EntryPoint
@InstallIn(ForegroundComponent::class)
internal interface ForegroundEntryPoint{
    fun getForeground(): ForegroundActivator
}

@ObsoleteCoroutinesApi
@InstallIn(ServiceComponent::class)
@Module
internal object BackgroundModule{
    @Provides
    fun providesMutex() = Mutex()
    @Provides
    fun provideCoroutineScope() = CoroutineScope( EmptyCoroutineContext + Dispatchers.Default + SupervisorJob())
}

@DefineComponent(parent = ServiceComponent::class)
internal interface ForegroundComponent{
    @DefineComponent.Builder
    interface Builder {
        fun build(): ForegroundComponent
    }
}