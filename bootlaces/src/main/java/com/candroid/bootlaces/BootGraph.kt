package com.candroid.bootlaces

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object Core{
    @Provides
    @Singleton
    fun manager(@ApplicationContext ctx: Context, boot: IBoot) = BootNotificationManager(ctx, boot)
    @Provides
    @Singleton
    fun provideBoot(@ApplicationContext ctx: Context): IBoot = runBlocking { return@runBlocking BootRepository(ctx).loadBoot().firstOrNull() } ?: Boot(null,null,null,null,null)
}

@Module
@InstallIn(ServiceComponent::class)
object BootServiceModule{
    @Provides
    @ServiceScoped
    fun provideMonitor(@ApplicationContext ctx: Context) = LocalBroadcastManager.getInstance(ctx)
}