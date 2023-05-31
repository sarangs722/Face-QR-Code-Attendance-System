package com.sarangs722.qrversion11.Retrofit

import com.sarangs722.qrversion11.MyUrl
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

val BASE_URL = MyUrl().BASE_URL

object RetrofitClient {
    private var instance: Retrofit?=null
    fun getInstance(): Retrofit {
        if (instance == null)
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return instance!!
    }
}
//https://5b1e-103-208-68-235.ngrok-free.app
//http://10.0.2.2:3000/