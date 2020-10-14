package com.candroid.lacedboots.di;

import android.content.BroadcastReceiver
import androidx.lifecycle.LifecycleObserver
import com.candroid.lacedboots.CloseDialogReceiver
import com.candroid.lacedboots.ILockManager
import com.candroid.lacedboots.LockManager
import com.candroid.lacedboots.LockScreenObserver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.ServiceScoped

@InstallIn(ServiceComponent::class)
@Module
interface BackgroundModule{
    @Binds
    @ServiceScoped
    abstract fun bindReceiver(receiver: CloseDialogReceiver): BroadcastReceiver

    @Binds
    @ServiceScoped
    abstract fun bindLockManager(manager: LockManager): ILockManager
}

@InstallIn(ActivityComponent::class)
@Module
abstract class UiModule{
    @Binds
    @ActivityScoped
     abstract fun bindsLockObserver(observer: LockScreenObserver): LifecycleObserver
}