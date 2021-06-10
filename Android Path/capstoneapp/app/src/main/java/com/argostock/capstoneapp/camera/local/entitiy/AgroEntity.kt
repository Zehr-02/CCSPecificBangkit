package com.argostock.capstoneapp.camera.local.entitiy

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agrostock_result_tabel")
data class AgroEntity(
    @PrimaryKey
    @NonNull
    val id: String,
    val file: String,
    val result: String? = null,
//    val akurasi: Double? = null,
//    val message: String = "aha"
)