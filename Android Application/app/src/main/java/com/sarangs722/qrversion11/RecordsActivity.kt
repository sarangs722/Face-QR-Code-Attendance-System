package com.sarangs722.qrversion11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sarangs722.qrversion11.databinding.ActivityPrimaryBinding
import com.sarangs722.qrversion11.databinding.ActivityRecordsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://35e2-45-119-31-10.ngrok-free.app"

class RecordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordsBinding
    private lateinit var homeIntent: Intent
//    private lateinit var faceRecogIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getMyAttendance();
        //onClickListener for viewRecords

        binding.homeButton.setOnClickListener {
            homeIntent = Intent(this, PrimaryActivity::class.java)
            homeIntent.putExtra("uName", intent.extras?.getString("uName"))
            startActivity(homeIntent)
        }
    }

    private fun getMyAttendance() {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        val retrofitAttData = retrofitBuilder.getAttendance()

        retrofitAttData.enqueue(object : Callback<List<MyAttendance>?> {
            override fun onResponse(
                call: Call<List<MyAttendance>?>,
                response: Response<List<MyAttendance>?>
            ) {
                val responseBody = response.body()!!
                val myStringBuilder = StringBuilder()

                for (myAtt in responseBody) {
                    myStringBuilder.append(myAtt.course)
                    myStringBuilder.append("        ")
                    myStringBuilder.append(myAtt.date)
                    myStringBuilder.append("     ")
                    val time = myAtt.timeslot.substring(0,2) + ":" + myAtt.timeslot.substring(2)

                    myStringBuilder.append(time)
                    myStringBuilder.append("\n")
                }

                binding.recordsData.text = myStringBuilder
            }

            override fun onFailure(call: Call<List<MyAttendance>?>, t: Throwable) {
                Log.d("RecordsActivity", "onFailure: " + t.message)
            }
        })
    }
}