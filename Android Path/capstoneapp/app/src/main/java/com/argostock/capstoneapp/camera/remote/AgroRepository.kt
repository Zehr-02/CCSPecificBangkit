package com.argostock.capstoneapp.camera.remote

import android.content.Context
import androidx.lifecycle.LiveData
import com.argostock.capstoneapp.camera.local.LocalDataSource
import com.argostock.capstoneapp.camera.local.entitiy.AgroEntity
import com.argostock.capstoneapp.camera.remote.network.ApiResponse
import com.argostock.capstoneapp.camera.remote.network.UploadRequest
import com.argostock.capstoneapp.camera.remote.response.AgroResponse
import com.argostock.capstoneapp.camera.source.NetworkBoundResource
import com.argostock.capstoneapp.camera.source.Resource
import com.argostock.capstoneapp.camera.utils.AppExecutors
import java.io.File

class AgroRepository private constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val appExecutors: AppExecutors
) : AgroDataSource {
    override fun getResult(context: Context, file: File, body: UploadRequest): LiveData<Resource<AgroEntity>> {
        return object : NetworkBoundResource<AgroEntity, AgroResponse>(appExecutors){
            override fun loadFromDB(): LiveData<AgroEntity> =
                localDataSource.getResult(file.name)

            override fun shouldFetch(data: AgroEntity?): Boolean =
                data?.id == null

            override fun createCall(): LiveData<ApiResponse<AgroResponse>> =
                remoteDataSource.getPredictionResult(context, file, body)

            override fun saveCallResult(data: AgroResponse) {
                val result = AgroEntity(
                    id = file.name,
                    file = data.file.toString(),
                    result = data.result,
                    //akurasi = data.akurasi,
//                    message = data.message
                )
                localDataSource.insertResult(result)
            }

        }.asLiveData()
    }

    companion object {
        @Volatile
        private var instance: AgroRepository? = null

        fun getInstance(
            remoteData: RemoteDataSource,
            localData: LocalDataSource,
            appExecutors: AppExecutors
        ): AgroRepository =
            instance ?: synchronized(this) {
                instance ?: AgroRepository(remoteData, localData, appExecutors).apply {
                    instance = this
                }
            }
    }
}