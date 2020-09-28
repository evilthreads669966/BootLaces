package com.candroid.bootlaces

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.candroid.bootlaces.BootLacesRepository.Keys.KEY_ACTIVITY_NAME
import com.candroid.bootlaces.BootLacesRepository.Keys.KEY_CONTENT
import com.candroid.bootlaces.BootLacesRepository.Keys.KEY_SMALL_ICON
import com.candroid.bootlaces.BootLacesRepository.Keys.KEY_TITLE
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
class BootLacesServiceImpl(repo: BootLacesRepository, ctx: Context): BootLacesService(repo, ctx){
    companion object Util : BootNotificationUtil() {
        override fun setContentIntent(ctx: Context, builder: NotificationCompat.Builder, prefs: SharedPreferences) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) prefs.getString(KEY_ACTIVITY_NAME, null)?.let {
                with(Intent(ctx, Class.forName(it))){
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    action = Intent.ACTION_VIEW
                    builder.setContentIntent(PendingIntent.getActivity(ctx, 0, this, 0))
                }
            }
        }

        override fun createChannel(ctx: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                with(ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager){
                    getNotificationChannel(getChannelId(ctx))?.let { return }
                    val notificationChannel = NotificationChannel(getChannelId(ctx), getChannelName(ctx), NotificationManager.IMPORTANCE_HIGH)
                    createNotificationChannel(notificationChannel)
                }
            }
        }

        override fun getId(ctx: Context) = ctx.resources.getInteger(R.integer.notification_id)

        override fun getChannelId(ctx: Context): String = ctx.resources.getString(R.string.channel_id)

        override fun getChannelName(ctx: Context): String = ctx.resources.getString(R.string.channel_name)
    }

    override fun create(): Notification {
        with(NotificationCompat.Builder(ctx, getChannelId(ctx))){
            setContentTitle(getNotificationTitle())
            setContentText(getNotificationContent())
            setSmallIcon(getNotificationIcon().takeIf { it != -1 } ?: android.R.drawable.sym_def_app_icon)
            setContentIntent(ctx, this, repo.mPrefs)
            setShowWhen(false)
            setAutoCancel(false)
            setOngoing(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                setChannelId(getChannelId(ctx))
            return build()
        }
    }

    override fun update(title: String?, content: String?, icon: Int) {
        with(repo.mPrefs){
            edit().apply{
                title?.let { if(!getString(KEY_TITLE, ctx.getString(R.string.author)).equals(it)) putString(KEY_TITLE, it) }
                content?.let { if(!getString(KEY_CONTENT, ctx.getString(R.string.author)).equals(it)) putString(KEY_CONTENT, it) }
                if(icon != -1) if(getInt(KEY_SMALL_ICON, -1) != icon) putInt(KEY_SMALL_ICON, icon)
            }.apply()
            val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(getId(ctx), this@BootLacesServiceImpl.create())
        }
    }
}


abstract class BootNotificationUtil{
    /*set activity screen to display when boot service notification is pressed*/
    internal abstract fun setContentIntent(ctx: Context, builder: NotificationCompat.Builder, prefs: SharedPreferences)

    /*create notification channel for boot service*/
    internal abstract fun createChannel(ctx: Context)

    internal abstract fun getId(ctx: Context): Int

    internal abstract fun getChannelId(ctx: Context): String

    internal abstract fun getChannelName(ctx: Context): String
}

sealed class BootLacesService(val repo: BootLacesRepository, val ctx: Context){
    /*internal function for creating notification object*/
    internal abstract fun create(): Notification

    /*update title and/or content text and/or icon of boot service notification. This is called inside UpdateReceiver which is triggered through updateBootNotification.*/
    internal abstract fun update(title : String? = null, content : String? = null, icon : Int = -1)

    /*retrieves the BootService implementation's class name*/
    internal fun getServiceName() = repo.fetchBootService()

    /*retrieves the BootService's notification title*/
    internal fun getNotificationTitle() = repo.fetchTitle()

    /*retrieves the BootService's notification content*/
    internal fun getNotificationContent() = repo.fetchContent()

    /*retrieves the BootService notification icon's resource id*/
    internal fun getNotificationIcon() = repo.fetchIcon()

    /*retrieves the BootService notification's onPressed activity*/
    internal fun getNotificationActivity() = repo.fetchActivity()

    /*sets the BootService implementation's class name*/
    internal fun setServiceName(serviceName: String) = repo.putBootService(serviceName)

    /*sets the BootService's notification title*/
    internal fun setNotificationTitle(title: String) = repo.putTitle(title)

    /*sets the BootService's notification content*/
    internal fun setNotificationContent(content: String) = repo.putContent(content)

    /*sets the BootService notification icon's resource id*/
    internal fun setNotificationIcon(icon: Int) = repo.putIcon(icon)

    /*sets the BootService notification's onPressed activity*/
    internal fun setNotificationActivity(activityName: String) = repo.putActivity(activityName)

    /*retrieves shared preferences from device protected storage*/
    internal fun getPreferences() = repo.getPreferences()
}