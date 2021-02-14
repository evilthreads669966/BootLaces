package com.candroid.bootlaces

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
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
/**
 * @author Chris Basinger
 * @email evilthreads669966@gmail.com
 * @date 02/05/20
 * shouldn't be a facade anymore
 **/
@FlowPreview
@Singleton
class WorkRescheduler @Inject constructor(
    private val scheduler: WorkScheduler,
    private val dao: WorkDao,
){
    internal suspend fun reschedule(scope: CoroutineScope, work: Work?){
        dao.getPersistentWork().filterNotNull()
            .onEach {
                it.forEach {
                    val worker = Worker.createFromWork(it)
                    scheduler.use {
                        runBlocking {
                            worker.scheduleFuture(it.interval!!, false, it.repeating!!, it.allowWhileIdle!!, it.precision!!)
                        }
                    }
                }
            }.flowOn(Dispatchers.IO)
            .launchIn(scope)
    }
}