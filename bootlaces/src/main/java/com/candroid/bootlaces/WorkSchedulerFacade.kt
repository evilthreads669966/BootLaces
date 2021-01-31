package com.candroid.bootlaces

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ServiceScoped
class WorkShedulerFacade @Inject constructor(
    @ApplicationContext override val ctx: Context,
    override val scheduler: WorkScheduler,
    val factory: IntentFactory
): ISchedulerFacade<Worker> {

    suspend fun scheduleBeforeReboot(dao: WorkDao, work: Work, scope: CoroutineScope){
            withContext(Dispatchers.IO){
                dao.insert(work)
                getSavedWorkAndSchedule(dao.getPersistentWork(),scope)
            }
    }

    private suspend fun getSavedWorkAndSchedule(flow: Flow<List<Work>>, scope: CoroutineScope){
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

    suspend fun scheduleAfterReboot(dao: WorkDao, scope: CoroutineScope){
        withContext(Dispatchers.IO){
            getSavedWorkAndSchedule(dao.getPersistentWork(), scope)
        }
    }
}

internal interface ISchedulerFacade<T>{
    val ctx: Context
    val scheduler: WorkScheduler
}