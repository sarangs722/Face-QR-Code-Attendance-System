package com.sarangs722.qrversion11

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.sarangs722.qrversion10.Retrofit.IMyService
import com.sarangs722.qrversion10.Retrofit.RetrofitClient
import com.sarangs722.qrversion11.databinding.ActivityPrimaryBinding
import com.sarangs722.qrversion11.databinding.ActivityRegisterUserBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.w3c.dom.Text
import retrofit2.Retrofit

class RegisterUser : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterUserBinding
    lateinit var iMyService: IMyService
    internal var compositeDisposable = CompositeDisposable()
    private lateinit var faceIntent: Intent

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Init API
        val retrofit: Retrofit = RetrofitClient.getInstance()
        iMyService = retrofit.create(IMyService::class.java)

        binding.signupButton.setOnClickListener {
            val user_name = binding.userNameTextinput.text.toString()
            val user_phone = binding.userPhoneTextinput.text.toString()
            val user_email_id = binding.userEmailIdTextinput.text.toString()
            val user_password = binding.userPasswordTextinput.text.toString()

            registerUser(user_name, user_phone, user_email_id, user_password)

            // DELETE BELOW
//            faceIntent = Intent(this, RegisterFace::class.java)
//            val uNameFromEmail = user_email_id.split("@")[0]
//            faceIntent.putExtra("uName", uNameFromEmail)
//            startActivity(faceIntent)
        }

    }

    private fun registerUser(name: String, phone: String, email: String, password: String) {
        if (TextUtils.isEmpty(phone) || phone.length != 10) {
            Toast.makeText(this, "Please enter correct phone number!", Toast.LENGTH_SHORT).show()
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return;
        }

        compositeDisposable.addAll(iMyService.registerUser(email, name, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {result ->
                Toast.makeText(this, ""+result, Toast.LENGTH_SHORT).show()

                if (result.toString() == "\"Registration success\"") {
                    faceIntent = Intent(this, RegisterFace::class.java)

                    val uNameFromEmail = email
//                    Toast.makeText(this, ""+ uNameFromEmail, Toast.LENGTH_SHORT).show()
//                    recogIntent.putExtra("nameFace", intent.extras?.getString("nameFace"))

                    faceIntent.putExtra("uName", uNameFromEmail)
                    startActivity(faceIntent)
                }
                else {
                    Toast.makeText(this, "Can't connect to the server.", Toast.LENGTH_SHORT).show();
                }
            }
        )
    }
}