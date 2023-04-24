package com.sarangs722.qrversion11

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.sarangs722.qrversion10.Retrofit.IMyService
import com.sarangs722.qrversion10.Retrofit.RetrofitClient
import com.sarangs722.qrversion11.databinding.ActivityMainBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit


class MainActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var iMyService: IMyService
    internal var compositeDisposable = CompositeDisposable()
    private lateinit var primaryIntent: Intent

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Init API
        val retrofit: Retrofit = RetrofitClient.getInstance()
        iMyService = retrofit.create(IMyService::class.java)

        //event
        binding.loginButton.setOnClickListener {
            val user_email_id = binding.userEmailIdTextinput.text.toString()
            val user_password = binding.userPasswordTextinput.text.toString()

            loginUser(user_email_id, user_password)

            // DELETE BELOW
//            val uNameFromEmail = user_email_id.split("@")[0]
//            primaryIntent = Intent(this, PrimaryActivity::class.java)
//            primaryIntent.putExtra("uName", uNameFromEmail)
//            startActivity(primaryIntent)
        }

        binding.newUserButton.setOnClickListener {
            primaryIntent = Intent(this, RegisterUser::class.java)
            startActivity(primaryIntent)
        }

        hideKeyboard(currentFocus ?: View(this))
    }

    private fun loginUser(email: String, password: String) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return;
        }
        val uNameFromEmail = email

        compositeDisposable.addAll(iMyService.loginUser(email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {result ->
                Toast.makeText(this, ""+result, Toast.LENGTH_SHORT).show()


                if (result.toString() == "\"Login success\"") {
                    primaryIntent = Intent(this, PrimaryActivity::class.java)
                    primaryIntent.putExtra("uName", uNameFromEmail)
                    startActivity(primaryIntent)
                }
                else {
                    Toast.makeText(this, "Can't connect to the server.", Toast.LENGTH_SHORT).show();
                }
            }
        )


    }
}