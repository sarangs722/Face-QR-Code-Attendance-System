package com.sarangs722.qrversion11

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ScanMode
import com.sarangs722.qrversion11.Retrofit.IMyService
import com.sarangs722.qrversion11.Retrofit.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit

class QRRecog : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    lateinit var iMyService: IMyService
    internal var compositeDisposable = CompositeDisposable()
    private lateinit var recordsIntent: Intent
    private lateinit var homeIntent: Intent

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrrecog)

        //Init API
        val retrofit: Retrofit = RetrofitClient.getInstance()
        iMyService = retrofit.create(IMyService::class.java)

        val alertDialog = AlertDialog.Builder( this ).apply {
            setTitle( "${intent.extras?.getString("uName")!!}, Face Recognized!")
            setMessage( "Please capture the QR Code now." )
            setCancelable( false )
            setPositiveButton( "Scan") { dialog, which ->
                dialog.dismiss()
            }
            create()
        }
        alertDialog.show()

        val homeButt = findViewById<Button>(R.id.homeButton)
        homeButt.setOnClickListener {
            homeIntent = Intent(this, PrimaryActivity::class.java)
            homeIntent.putExtra("uName", intent.extras?.getString("uName"))
            startActivity(homeIntent)
        }


        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
//            runOnUiThread {
                compositeDisposable.addAll(iMyService.sendQRDetails(it.text, intent.extras?.getString("uName")!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe ({result ->
//                        Toast.makeText(this, ""+result, Toast.LENGTH_LONG).show()

                        if (result.toString() == "\"Attendance success\"") {
                            recordsIntent = Intent(this, RecordsActivity::class.java)
                            recordsIntent.putExtra("uName", intent.extras?.getString("uName"))
                            startActivity(recordsIntent)
                        }
                        else {
                            Log.e("QRError", "QR not scanned")
                            Toast.makeText(this, "Invalid QR Code.", Toast.LENGTH_LONG).show();
                            homeIntent = Intent(this, PrimaryActivity::class.java)
                            homeIntent.putExtra("uName", intent.extras?.getString("uName"))
                            startActivity(homeIntent)
                        }
//                        else {
//                            Toast.makeText(this, "Can't connect to the server.", Toast.LENGTH_SHORT).show();
//                        }
                    },
                        { throwable->
                            Log.e("QRError", "QR not scanned")
                            Toast.makeText(this, "Invalid QR Code.", Toast.LENGTH_SHORT).show();
                            homeIntent = Intent(this, PrimaryActivity::class.java)
                            homeIntent.putExtra("uName", intent.extras?.getString("uName"))
                            startActivity(homeIntent)
                        }
                    )
                )



//                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
//            }
        }
//        codeScanner.errorCallback = ErrorCallback.SUPPRESS

//        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
//            runOnUiThread {
//                Toast.makeText(this, "Camera processing...: ${it.message}",
//                    Toast.LENGTH_LONG).show()
//            }
//        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}