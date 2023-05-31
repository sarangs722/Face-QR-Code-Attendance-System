package com.sarangs722.qrversion11

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    // For specific course, teacher, etc
//    @GET("userAttendanceRecords")
//    fun getUserAttendance(@Query("roll") roll: String): Call<List<MyAttendance>>

    @GET("attendanceRecords/{roll}")
    fun getAttendanceUser(@Path("roll") roll: String?): Call<List<MyAttendance>>

    @GET("attendanceRecords")
    fun getAttendance(): Call<List<MyAttendance>>

    @GET("userDetails/{roll}")
    fun getUserDetails(@Path("roll") roll: String?): Call<List<MyUserDetails>>
}