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
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates
@FlowPreview
@ServiceScoped
data class WorkManager @Inject constructor(
    @ApplicationContext override val ctx: Context,
    override val mutex: Mutex,
    override val workers: MutableCollection<Worker>,
    override val scheduler: WorkScheduler,
    override val coroutineContext: CoroutineContext
): IWorkManager<Worker>, CoroutineScope {
    var workerCount: Int by Delegates.observable(0) { _, _, newValue ->
        if (newValue == 0) (ctx as WorkService).stopWorkService()
    }

    fun releaseResources(ctx: Context) {
        this.cancel()
        workers.forEach { worker -> worker.unregisterReceiver(ctx) }
        runBlocking {
            mutex.withLock {
                workers.clear()
                this@WorkManager.workerCount = 0
            }
        }
    }

    suspend fun scheduleBeforeReboot(dao: WorkDao, work: Work){
        if(work == null) return
        dao.insert(work)
        scheduleReboot(dao.getPersistentWork())
    }

    private suspend fun scheduleReboot(flow: Flow<List<Work>>){
        flow.filterNotNull()
            .flatMapMerge(DEFAULT_CONCURRENCY){
                flow {
                    emit(it.map { Worker.createFromWork(it)} )
                }.flowOn(Dispatchers.IO)
            }
            .onEach {
                it.filterIsInstance<PersistentWorker>()
                    .forEach { worker -> scheduler.use {
                        worker.scheduleAfterReboot()
                    }
                    }
            }.flowOn(Dispatchers.IO)
            .launchIn(GlobalScope)
    }

    suspend fun scheduleAfterReboot(dao: WorkDao){
        withContext(Dispatchers.IO){
            scheduleReboot(dao.getPersistentWork())
        }
    }

    /*this particullar fuunction is up next for my attention*/
    internal suspend fun processExpiredWork(worker: Worker, foreground: ForegroundActivator, intentFactory: IntentFactory){
        if(workers.contains(worker)) return
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            if(!WorkService.state.equals(ServiceState.FOREGROUND))
                foreground.activate()
        with(worker){
            mutex.withLock {
                workers.add(this)
                workerCount++
            }
            val intent = intentFactory.createWorkNotificationIntent(this)
            if(withNotification == true)
                NotificatonService.enqueue(ctx, intent)
            registerReceiver(ctx)
            doWork(ctx)
            unregisterReceiver(ctx)
            if(withNotification == true)
                NotificatonService.enqueue(ctx, intent.apply { setAction(Actions.ACTION_FINISH.action) })
        }
        mutex.withLock { workerCount-- }
    }
}

internal interface IWorkManager<T>{
    val ctx: Context
    val mutex: Mutex
    val workers: MutableCollection<T>
    val scheduler: WorkScheduler
}