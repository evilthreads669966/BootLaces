package com.candroid.bootlaces

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.PreferenceDataStoreFactory
import androidx.datastore.preferences.Preferences
import com.candroid.bootlaces.api.SimpleFactory
import dagger.Binds
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
import kotlinx.coroutines.SupervisorJob
import java.io.File
import javax.inject.Scope
import javax.inject.Singleton

@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
annotation class ForegroundScope

@Module
@InstallIn(SingletonComponent::class)
abstract class BootReceiverModule{
    @Singleton
    @Binds abstract fun bindFactory(factory: BootFactory): SimpleFactory<IBoot,DataStore<Preferences>>
}

@Module
@InstallIn(SingletonComponent::class)
object BootReceiverModuleImpl {
    @Singleton
    @Provides fun provideScope() = CoroutineScope(Dispatchers.IO + SupervisorJob())
    @Singleton
    @Provides fun provideBoot(factory: BootFactory): IBoot = factory.create()
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
}

@ForegroundScope
@EntryPoint
@InstallIn(ForegroundComponent::class)
interface ForegroundEntryPoint{
    @ForegroundScope
    fun getActivator(): ForegroundActivator
}

@DefineComponent(parent = ServiceComponent::class)
@ForegroundScope
interface ForegroundComponent{
    @DefineComponent.Builder
    interface Builder {
        fun build(): ForegroundComponent
    }
}