package com.candroid.bootlaces

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
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
class WorkShedulerFacade @Inject constructor(
    private val scheduler: WorkScheduler,
    private val dao: WorkDao,
){
    internal suspend fun rescheduleWorkAfterReboot(scope: CoroutineScope, work: Work?){
        dao.getPersistentWork().filterNotNull()
            .flatMapMerge(DEFAULT_CONCURRENCY){
                flow {
                    emit(it.map { Worker.createFromWork(it)} )
                }
            }
            .onEach {
                if(work != null)
                    it.find { it.id == work.id }?.let { scheduler.use { (it as PersistentWorker).scheduleFuture() } }
                else
                    it.filterIsInstance<PersistentWorker>()
                        .forEach { worker -> scheduler.use { worker.scheduleFuture() } }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }
}