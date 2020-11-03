package com.candroid.bootlaces

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Work(@PrimaryKey(autoGenerate = false) val id: Int, val job: String): Parcelable{
    fun toWorker(): Worker = Class.forName(this.job).newInstance() as Worker
    companion object{
       const val KEY_PARCEL = "KEY_PARCEL"
    }
}