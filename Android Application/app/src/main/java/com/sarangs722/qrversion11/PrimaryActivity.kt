package com.sarangs722.qrversion11

import android.content.Intent
import android.os.Bundle
import android.view.Menu
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
import com.sarangs722.qrversion10.Retrofit.IMyService
import com.sarangs722.qrversion11.databinding.ActivityPrimaryBinding
import io.reactivex.disposables.CompositeDisposable

class PrimaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrimaryBinding
    private lateinit var faceRecogIntent: Intent
    private lateinit var recordsIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrimaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.extras?.getString("uName")
        binding.welcomeUserText.text = "Welcome, $userName"

        binding.markAttendanceButton.setOnClickListener {
            faceRecogIntent = Intent(this, FaceRecog::class.java)
            faceRecogIntent.putExtra("uName", intent.extras?.getString("uName"))
            startActivity(faceRecogIntent)
        }

        //onClickListener for viewRecords
        binding.viewRecordsButton.setOnClickListener {
            recordsIntent = Intent(this, RecordsActivity::class.java)
            recordsIntent.putExtra("uName", intent.extras?.getString("uName"))
            startActivity(recordsIntent)
        }
    }
}