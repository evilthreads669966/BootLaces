package com.candroid.lacedboots.di;

import android.content.BroadcastReceiver
import com.candroid.lacedboots.CloseDialogReceiver
import com.candroid.lacedboots.ILockManager
import com.candroid.lacedboots.LockManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.scopes.ServiceScoped

@InstallIn(ServiceComponent::class)
@Module
interface BackgroundModule{
    @Binds
    @ServiceScoped
    abstract fun bindLockManager(manager: LockManager): ILockManager
}