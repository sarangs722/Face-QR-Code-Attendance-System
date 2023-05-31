package com.sarangs722.qrversion11

import android.content.Intent
import android.os.Build.VERSION_CODES.BASE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.widget.TableRow
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.sarangs722.qrversion11.databinding.ActivityPrimaryBinding
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PrimaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrimaryBinding

    private lateinit var faceRecogIntent: Intent
    private lateinit var recordsIntent: Intent
    private lateinit var homeIntent: Intent
    private lateinit var myUser: MyUserDetails
    private val BASE_URL = MyUrl().BASE_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrimaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.extras?.getString("uName")
//        binding.welcomeUserText.text = "Welcome, $userName"

        getUserDetails()



        binding.markAttendanceButton.setOnClickListener {
            faceRecogIntent = Intent(this, FaceRecog::class.java)
            faceRecogIntent.putExtra("uName", intent.extras?.getString("uName"))
//            if (intent.hasExtra("serCheck"))
//                faceRecogIntent.putExtra("serCheck", intent.extras?.getBoolean("serCheck"))
            startActivity(faceRecogIntent)
        }

        //onClickListener for viewRecords
        binding.viewRecordsButton.setOnClickListener {
            recordsIntent = Intent(this, RecordsActivity::class.java)
            recordsIntent.putExtra("uName", intent.extras?.getString("uName"))
            startActivity(recordsIntent)
        }

        binding.logoutButton.setOnClickListener {
            homeIntent = Intent(this, MainActivity::class.java)
//            recordsIntent.putExtra("uName", intent.extras?.getString("uName"))
            startActivity(homeIntent)
        }
    }

    fun getUserDetails() {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        val retrofitAttData = retrofitBuilder.getUserDetails(intent.extras?.getString("uName"))

        retrofitAttData.enqueue(object : Callback<List<MyUserDetails>?> {
            override fun onResponse(
                call: Call<List<MyUserDetails>?>,
                response: Response<List<MyUserDetails>?>
            ) {
                val responseBody = response.body()!!
//                val myStringBuilder = StringBuilder()

                for (myAtt in responseBody) {
                    val roll = myAtt.email
                    val name = myAtt.name
                    val phone = myAtt.phone
                    myUser = MyUserDetails(roll, name, phone)
                }

                var firstName = myUser.name
                if (myUser.name.indexOf(' ') != -1)
                    firstName = myUser.name.substring(0, myUser.name.indexOf(' ') + 1)
                binding.welcomeUserText.text = "Welcome, ${firstName}"
                binding.userDetailsContent.text = "Name: ${myUser.name} \nRoll: ${myUser.email}\nPhone: ${myUser.phone}"

//                welcomeUserText.text = "Welcome, $userName"

//                    var course = myAtt.course
//                    Toast.makeText(this@RecordsActivity, course, Toast.LENGTH_SHORT).show();
//                    if (course.indexOf('$') != -1)
//                        course = course.substring(0, course.indexOf('$'))
//                    myStringBuilder.append(course)
//                    myStringBuilder.append("        ")
//                    val date = myAtt.date
//                    myStringBuilder.append(myAtt.date)
//                    myStringBuilder.append("     ")
//                    val time = myAtt.timeslot.substring(0,2) + ":" + myAtt.timeslot.substring(2,4) + "-" + myAtt.timeslot.substring(5,7) + ":" + myAtt.timeslot.substring(7, 9);
//                    Log.e("course", course.toString())
//                    myStringBuilder.append(time)
//                    myStringBuilder.append("\n")

//                    val dataRecord = recordsAPIDataItem(course, date, time)
//                    Log.e("dataRecord", dataRecord.toString())
//                    recordsData.add(dataRecord)
//                }

//                Log.e("API Data 2", recordsData.toString())
//                binding.recordsData.text = myStringBuilder

//                for (record in recordsData) {
//                    val newRow = LayoutInflater.from(baseContext).inflate(R.layout.table_row, null) as TableRow
//                    newRow.findViewById<TextView>(R.id.courseTextView).text = record.course
//                    newRow.findViewById<TextView>(R.id.dateTextView).text = record.date
//                    newRow.findViewById<TextView>(R.id.timeTextView).text = record.timeslot
//
//                    binding.recordsTable.addView(newRow)
//                }
            }

            override fun onFailure(call: Call<List<MyUserDetails>?>, t: Throwable) {
                Log.d("RecordsActivity", "onFailure: " + t.message)
            }
        })

    }
}