package com.candroid.bootlaces

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@FlowPreview
@Singleton
class WorkShedulerFacade @Inject constructor(
    private val scheduler: WorkScheduler,
    private val dao: WorkDao,
){
    suspend fun scheduleWorkForReboot(work: Work?, scope: CoroutineScope){
        withContext(Dispatchers.IO){
            if(work != null) dao.insert(work)
            scheduleWorkFromDatabase(dao.getPersistentWork(),scope)
        }
    }

    private suspend fun scheduleWorkFromDatabase(flow: Flow<List<Work>>, scope: CoroutineScope){
        flow.filterNotNull()
            .flatMapMerge(DEFAULT_CONCURRENCY){
                flow {
                    emit(it.map { Worker.createFromWork(it)} )
                }
            }
            .onEach {
                it.filterIsInstance<PersistentWorker>()
                    .forEach { worker -> scheduler.use { worker.scheduleAfterReboot() } }
            }.flowOn(Dispatchers.Default)
            .launchIn(scope)
    }
}