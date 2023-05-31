package com.sarangs722.qrversion11

import com.sarangs722.qrversion11.databinding.ActivityMainBinding

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns.RELATIVE_PATH
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.Toast
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.PermissionChecker
import com.sarangs722.qrversion11.databinding.ActivityRegisterFaceBinding
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Files.createDirectory
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.math.log

typealias LumaListener = (luma: Double) -> Unit


class RegisterFace : AppCompatActivity() {
    private lateinit var viewBinding: ActivityRegisterFaceBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var secondIntent: Intent

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var sharedPreferences: SharedPreferences
    private val SHARED_PREF_IS_DATA_STORED_KEY = "is_data_stored"

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRegisterFaceBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }


        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener {
            takePhoto()

            sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean(SHARED_PREF_IS_DATA_STORED_KEY, false).apply()

            //Now moving to the Options page for whether to go to Face Recognition check
            secondIntent = Intent(this, PrimaryActivity::class.java)
            secondIntent.putExtra("uName", intent.extras?.getString("uName"))
//            secondIntent.putExtra("serCheck", true)
            startActivity(secondIntent)
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.

//        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
//            .format(System.currentTimeMillis())
        val userNameIntent = intent
        val name = userNameIntent.getStringExtra("uName")
        if (name != null) {
            Log.e(TAG, "UserName found is:" + name)
        }

        //NEED TO ADD Image files into android app's internal storage where it is hidden from users
        //https://developer.android.com/training/data-storage/app-specific
//        context.openFileOutput(name, Context.MODE_PRIVATE).use {
//
//        }

        // CHANGING BELOW LINE FOR NOW

        val appImageDirectory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + "/QRApp")
        appImageDirectory.mkdirs()
//        Log.d("TESTING YOLO", "JUST BEFORE SAVING IMAGE")
        val relativeLocation = Environment.DIRECTORY_PICTURES + File.separator + "QRApp" + File.separator + name;
//        val relativeLocation = appImageDirectory.path + File.separator + name;

//        val path = File("/Pictures/QRApp")
//        if (path.mkdir()) {
//            Log.e(TAG, "Directory QRApp created successfully!");
//        }
//        else {
//            Log.e(TAG, "Directory QRAPP could not be created.")
//        }

//        val relativeLocation = path.absolutePath + File.separator + name;
        Log.d("TESTING YOLO", "JUST BEFORE SAVING IMAGE")
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, relativeLocation)
            }
            else {
                val filePath = File(Environment.getExternalStorageDirectory(), relativeLocation).absolutePath
                put(MediaStore.MediaColumns.DATA, filePath)
//
////                put(MediaStore.MediaColumns.DATA, File(relativeLocation).absolutePath)
//
//                val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
////                val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                val queryUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3APictures%2FQRApp")
//                val cursor: Cursor? = baseContext.contentResolver.query(queryUri, filePathColumn, null, null, null)
//                cursor?.let {
//                    if (it.moveToFirst()) {
//                        val columnIndex = it.getColumnIndex(filePathColumn[0])
//                        val filePath = it.getString(columnIndex)
//                        put(MediaStore.MediaColumns.DATA, filePath)
//                    }
//                    it.close()
//                }
            }

        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(baseContext, "Please click your photo again!", Toast.LENGTH_LONG).show()
                    finish()
                    val faceIntent = Intent(baseContext, RegisterFace::class.java)
                    faceIntent.putExtra("uName", intent.extras?.getString("uName"))
                    startActivity(faceIntent)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = intent.extras?.getString("uName") + "'s photo capture succeeded!"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

//    private fun captureVideo() {}

    private fun startCamera() {
        imageCapture = ImageCapture.Builder().build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
                val faceIntent = Intent(this, RegisterFace::class.java)
                faceIntent.putExtra("uName", intent.extras?.getString("uName"))
                startActivity(faceIntent)
            }
        }
    }
}