package com.candroid.lacedboots.di;

import android.content.BroadcastReceiver
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import com.candroid.lacedboots.CloseDialogReceiver
import com.candroid.lacedboots.ILockManager
import com.candroid.lacedboots.LockManager
import com.candroid.lacedboots.LockScreenObserver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ServiceScoped

@InstallIn(ServiceComponent::class)
@Module
abstract class LockReceiverBinding{
    @Binds
    @ServiceScoped
    abstract fun bindReceiver(receiver: CloseDialogReceiver): BroadcastReceiver

    @Binds
    @ServiceScoped
    abstract fun bindLockManager(manager: LockManager): ILockManager
}

@InstallIn(ServiceComponent::class)
@Module
object LockReceiverProviders{

    @Provides
    fun providesReceiver(manager: ILockManager) = CloseDialogReceiver(manager)

    @ServiceScoped
    @Provides
    fun provideLockManager() = LockManager()
}

@InstallIn(ActivityComponent::class)
@Module
abstract class LockScreenBinding{
    @Binds
     abstract fun bindsLockObserver(observer: LockScreenObserver): LifecycleObserver
}