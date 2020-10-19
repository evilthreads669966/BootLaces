package com.candroid.bootlaces

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.PreferenceDataStoreFactory
import androidx.datastore.preferences.Preferences
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.hilt.DefineComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.AliasOf
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import javax.inject.Scope
import javax.inject.Singleton

@Scope
@AliasOf(ServiceScoped::class)
annotation class ForegroundScope

@Module
@InstallIn(SingletonComponent::class)
object BroadcastReceiverModule {
    @Singleton
    @Provides fun provideScope() = CoroutineScope(Dispatchers.Default + SupervisorJob())
    @Singleton
    @Provides fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = {
                File(
                    ctx.filesDir,
                    DataStoreKeys.PREF_FILE_NAME
                ).apply { createNewFile() }
            },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    }
    @Singleton
    @Provides fun provideChannel() = Channel<FlowWorker>(2)
}
@ForegroundScope
@EntryPoint
@InstallIn(ForegroundComponent::class)
interface ForegroundEntryPoint{
    @ForegroundScope
    fun getForeground(): ForegroundActivator
}
@Subcomponent(modules = [BackgroundServiceModule::class])
interface ForegroundComponent{
    @DefineComponent.Builder
    interface Builder {
        fun service(@BindsInstance service: BackgroundWorker): Builder
        fun build(): ForegroundComponent
    }
}

@Module(subcomponents = [ForegroundComponent::class])
abstract class BackgroundServiceModule{
    fun get
}