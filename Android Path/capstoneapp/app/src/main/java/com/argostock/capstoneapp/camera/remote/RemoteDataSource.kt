package com.argostock.capstoneapp.camera.remote

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.argostock.capstoneapp.camera.remote.network.ApiResponse
import com.argostock.capstoneapp.camera.remote.network.ConfigNetwork
import com.argostock.capstoneapp.camera.remote.network.UploadRequest
import com.argostock.capstoneapp.camera.remote.response.AgroResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RemoteDataSource {

    fun getPredictionResult(context: Context, file: File, body: UploadRequest): LiveData<ApiResponse<AgroResponse>> {
        val listDetails = MutableLiveData<ApiResponse<AgroResponse>>()
        ConfigNetwork.getRetrofit().uploadImage(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                body
            ),
        ).enqueue(object : Callback<AgroResponse> {
            override fun onFailure(call: Call<AgroResponse>, t: Throwable) {
                val data = AgroResponse(
                    file = "https://image.flaticon.com/icons/png/512/675/675564.png",
                    result = "Not Predicted Yet",
                    //akurasi = 0.0,
//                    message = ""
                )
                listDetails.postValue(ApiResponse.success(data))
                Toast.makeText(context, "Server on Failure", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<AgroResponse>,
                response: Response<AgroResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    val data = AgroResponse(
                        file = result?.file,
                        result = result?.result,
                        //akurasi = result?.akurasi,
//                        message = result?.message.toString()
                    )
                    listDetails.postValue(ApiResponse.success(data))
                    if (body.toString() == "[]") {
                        Toast.makeText(context, "Data not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        return listDetails
    }
    companion object {
        @Volatile
        private var instance: RemoteDataSource? = null

        fun getInstance(): RemoteDataSource =
            instance ?: synchronized(this) {
                instance ?: RemoteDataSource().apply { instance = this }
            }
    }
}