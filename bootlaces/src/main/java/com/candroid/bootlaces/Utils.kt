package com.candroid.bootlaces

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

private fun <T> Class<T>.isRebootEnabled(ctx: Context): Boolean{
    val componentName = ComponentName(ctx, this)
    val state = ctx.packageManager.getComponentEnabledSetting(componentName)
    return (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
}

private fun <T> Class<T>.enableReboot(ctx: Context){
    val componentName = ComponentName(ctx, this)
    val state = ctx.packageManager.getComponentEnabledSetting(componentName)
    if(state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
        ctx.packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
}

internal object Utils{
    fun isRebootEnabled(ctx: Context) = BootReceiver::class.java.isRebootEnabled(ctx)
    fun enableReboot(ctx: Context) = BootReceiver::class.java.enableReboot(ctx)
}