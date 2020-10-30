package com.candroid.bootlaces

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Work::class), version = 1, exportSchema = false)
abstract class WorkerDatabase: RoomDatabase(){
    companion object{
        fun getInstance(ctx: Context): WorkerDatabase{
            return Room.databaseBuilder(ctx, WorkerDatabase::class.java, "worker_database").build()
        }
    }
    abstract fun workerDao(): WorkerDao
}