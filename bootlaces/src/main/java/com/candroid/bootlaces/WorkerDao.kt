package com.candroid.bootlaces

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao{

    @Query("SELECT * FROM work")
    fun getAll(): Flow<Work>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(worker: Work)

    @Delete
    fun delete(worker: Work)
}