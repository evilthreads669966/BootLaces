package com.candroid.bootlaces

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

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