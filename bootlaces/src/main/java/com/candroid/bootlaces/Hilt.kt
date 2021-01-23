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
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.DefineComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex

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
@ExperimentalCoroutinesApi
@FlowPreview
@InternalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
internal object GlobalModule {
    @Provides
    fun provideWorkDao(@ApplicationContext ctx: Context): WorkDao = Room.databaseBuilder(ctx, WorkDatabase::class.java, "worker_database").build().workerDao()
    @Provides
    fun provideAlarmMgr(@ApplicationContext ctx: Context) = ctx.getSystemService(Service.ALARM_SERVICE) as AlarmManager
}

@FlowPreview
@InternalCoroutinesApi
@EntryPoint
@InstallIn(ForegroundComponent::class)
internal interface ForegroundEntryPoint{
    @ExperimentalCoroutinesApi
    fun getForeground(): ForegroundActivator
}

@InstallIn(ServiceComponent::class)
@Module
internal object BackgroundModule{
    @Provides
    fun provideChannel() = Channel<Work>()
    @Provides
    fun provideSupervisor(): CompletableJob = SupervisorJob()
    @Provides
    fun providesMutex() = Mutex()
    @Provides
    fun provideWorkers(): MutableCollection<Worker> = mutableSetOf()
}

@InstallIn(ServiceComponent::class)
@Module
internal object ForegroundModule{
    @Provides
    fun provideNotificationBuilder(@ApplicationContext ctx: Context) = NotificationCompat.Builder(ctx)
    @Provides
    fun provideNotificationMgr(@ApplicationContext ctx: Context) = NotificationManagerCompat.from(ctx)
}

@DefineComponent(parent = ServiceComponent::class)
internal interface ForegroundComponent{
    @DefineComponent.Builder
    interface Builder {
        fun build(): ForegroundComponent
    }
}