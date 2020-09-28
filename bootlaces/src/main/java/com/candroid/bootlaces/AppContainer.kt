package com.candroid.bootlaces

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
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
val libraryModule = module {
    single { BootLacesRepositoryImpl(androidContext()) }
    single { BootLacesServiceImpl(get(), androidContext()) }
}

class AppContainer(val ctx: Context){
    val service by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BootLacesServiceImpl(BootLacesRepositoryImpl(ctx),ctx) }
    //val service by inject<BootLacesServiceImpl>(clazz = BootLacesServiceImpl::class.java)
    companion object{
        private var INSTANCE: AppContainer? = null
        fun getInstance(ctx: Context): AppContainer{
            if(INSTANCE == null)
                INSTANCE = AppContainer(ctx)
            return INSTANCE!!
        }
    }
}