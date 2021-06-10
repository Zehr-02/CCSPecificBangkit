package com.argostock.capstoneapp.camera.local

import androidx.lifecycle.LiveData
import com.argostock.capstoneapp.camera.local.entitiy.AgroEntity
import com.argostock.capstoneapp.camera.local.room.AgroDao

class LocalDataSource private constructor(private val predictDao: AgroDao) {

    fun insertResult(data: AgroEntity) {
        predictDao.insertResult(data)
    }

    fun getResult(image: String): LiveData<AgroEntity> = predictDao.getResult(image)

    companion object {
        private var INSTANCE: LocalDataSource? = null

        fun getInstance(predictDao: AgroDao): LocalDataSource =
            INSTANCE ?: LocalDataSource(predictDao)
    }
}