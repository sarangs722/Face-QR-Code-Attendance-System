package com.sarangs722.qrversion11

import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("attendanceRecords")
    fun getAttendance(): Call<List<MyAttendance>>
}