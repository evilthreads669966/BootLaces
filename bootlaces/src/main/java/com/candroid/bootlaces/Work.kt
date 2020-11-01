package com.candroid.bootlaces

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Work(@PrimaryKey(autoGenerate = false) val id: Int, val job: String){
    fun toWorker(): Worker = Class.forName(this.job).newInstance() as Worker
}