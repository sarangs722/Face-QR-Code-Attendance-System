package com.sarangs722.qrversion11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sarangs722.qrversion11.databinding.ActivityRecordsBinding
import com.sarangs722.qrversion11.databinding.TableRowBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

//const val BASE_URL = "https://d238-103-208-68-157.ngrok-free.app"

class RecordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordsBinding
//    private lateinit var rowBinding: TableRowBinding
    private lateinit var homeIntent: Intent
    private var recordsData : MutableList<recordsAPIDataItem> = ArrayList()
    private val BASE_URL = MyUrl().BASE_URL
//    private lateinit var faceRecogIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        getMyAttendance();
//        Log.e("API Data", recordsData.toString())
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

        val retrofitAttData = retrofitBuilder.getAttendanceUser(intent.extras?.getString("uName"))

        retrofitAttData.enqueue(object : Callback<List<MyAttendance>?> {
            override fun onResponse(
                call: Call<List<MyAttendance>?>,
                response: Response<List<MyAttendance>?>
            ) {
                val responseBody = response.body()!!
//                val myStringBuilder = StringBuilder()

                for (myAtt in responseBody) {
                    var course = myAtt.course.replace('-', ' ');
//                    var course = myAtt.course
//                    Toast.makeText(this@RecordsActivity, course, Toast.LENGTH_SHORT).show();
                    if (course.indexOf('$') != -1)
                        course = course.substring(0, course.indexOf('$'))
//                    myStringBuilder.append(course)
//                    myStringBuilder.append("        ")
                    val date = myAtt.date
//                    myStringBuilder.append(myAtt.date)
//                    myStringBuilder.append("     ")
                    val time = myAtt.timeslot.substring(0,2) + ":" + myAtt.timeslot.substring(2,4) + "-" + myAtt.timeslot.substring(5,7) + ":" + myAtt.timeslot.substring(7, 9);
//                    Log.e("course", course.toString())
//                    myStringBuilder.append(time)
//                    myStringBuilder.append("\n")

                    val dataRecord = recordsAPIDataItem(course, date, time)
//                    Log.e("dataRecord", dataRecord.toString())
                    recordsData.add(dataRecord)
                }
//                Log.e("API Data 2", recordsData.toString())
//                binding.recordsData.text = myStringBuilder

                for (record in recordsData) {
                    val newRow = LayoutInflater.from(baseContext).inflate(R.layout.table_row, null) as TableRow
                    newRow.findViewById<TextView>(R.id.courseTextView).text = record.course
                    newRow.findViewById<TextView>(R.id.dateTextView).text = record.date
                    newRow.findViewById<TextView>(R.id.timeTextView).text = record.timeslot

                    binding.recordsTable.addView(newRow)
                }
            }

            override fun onFailure(call: Call<List<MyAttendance>?>, t: Throwable) {
                Log.d("RecordsActivity", "onFailure: " + t.message)
            }
        })
    }
}