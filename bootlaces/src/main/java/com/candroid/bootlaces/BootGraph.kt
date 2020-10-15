package com.candroid.bootlaces

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.annotation.Signed
import javax.inject.Singleton
/*
@Module
@InstallIn(ApplicationComponent::class)
interface BootModule{
    @Singleton
    @Binds
    fun bindBoot(boot: Boot): IBoot
}*/

/*@Module
@InstallIn(ServiceComponent::class)
interface Background{
    @Binds
    fun bindMonitor(monitor: LocalBroadcastManager): LocalBroadcastManager
}*/

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