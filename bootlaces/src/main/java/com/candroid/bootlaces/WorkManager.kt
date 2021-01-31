package com.candroid.bootlaces

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.properties.Delegates

@FlowPreview
@ServiceScoped
data class WorkManager @Inject constructor(
    @ApplicationContext override val ctx: Context,
    override val mutex: Mutex,
    override val workers: MutableCollection<Worker>,
    override val scheduler: WorkScheduler,
    val factory: IntentFactory
): IWorkManager<Worker> {
    var workerCount: Int by Delegates.observable(0) { _, _, newValue ->
        if (newValue == 0){
            val intent = factory.createShutDownServiceIntent()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ctx.startForegroundService(intent)
            else
                ctx.startService(intent)
        }
    }

    fun releaseResources(ctx: Context) {
        workers.forEach { worker -> worker.unregisterReceiver(ctx) }
        runBlocking {
            mutex.withLock {
                workers.clear()
                this@WorkManager.workerCount = 0
            }
        }
    }

    suspend fun saveWorkAndScheduleBeforeReboot(dao: WorkDao, work: Work, scope: CoroutineScope){
            withContext(Dispatchers.IO){
                dao.insert(work)
                processSavedWorkForReboot(dao.getPersistentWork(),scope)
            }
    }

    private suspend fun processSavedWorkForReboot(flow: Flow<List<Work>>, scope: CoroutineScope){
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

    suspend fun scheduleWorkAfterReboot(dao: WorkDao, scope: CoroutineScope){
        withContext(Dispatchers.IO){
            processSavedWorkForReboot(dao.getPersistentWork(), scope)
        }
    }

    /*this particullar fuunction is up next for my attention*/
    internal suspend fun processExpiredWork(worker: Worker, foreground: ForegroundActivator, intentFactory: IntentFactory, scope: CoroutineScope){
        if(workers.contains(worker)) return
        mutex.withLock {
            workers.add(worker)
            workerCount++
        }
        val intent = intentFactory.createWorkNotificationIntent(worker)
        if(worker.withNotification == true)
            NotificatonService.enqueue(ctx, intent)
        worker.registerReceiver(ctx)
        scope.launch { worker.doWork(ctx) }.join()
        worker.unregisterReceiver(ctx)
        if(worker.withNotification == true)
            NotificatonService.enqueue(ctx, intent.apply { setAction(Actions.ACTION_FINISH.action) })
        mutex.withLock { workerCount-- }
    }
}

internal interface IWorkManager<T>{
    val ctx: Context
    val mutex: Mutex
    val workers: MutableCollection<T>
    val scheduler: WorkScheduler
}