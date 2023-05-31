package com.sarangs722.qrversion11.Retrofit

import com.sarangs722.qrversion11.MyAttendance
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface IMyService {
    @POST("register")
    @FormUrlEncoded
    fun registerUser(@Field("email") email: String,
                     @Field("name") name: String,
                     @Field("phone") phone: String,
                     @Field("password") password: String,
    ):Observable<String>

    @POST("login")
    @FormUrlEncoded
    fun loginUser(@Field("email") email: String,
                  @Field("password") password: String,
    ):Observable<String>

    @POST("qrStudentApp")
    @FormUrlEncoded
    fun sendQRDetails(@Field("qrData") course: String,
                      @Field("email") email: String,
    ):Observable<String>

//    @GET("attendanceRecords")
//    fun getAttendance(): Call<List<MyAttendance>>

//    @GET("attendanceRecords")
//    @FormUrlEncoded
//    fun getAttendance(@Field(""))

//    @POST("qrStudentApp")
//    @FormUrlEncoded
//    fun sendQRDetails(@Field("course") course: String,
//        @Field("lecdate") lecdate: String,
//        @Field("lectime") lectime: String,
//                      @Field("salt") salt: String,
//    ):Observable<String>


}