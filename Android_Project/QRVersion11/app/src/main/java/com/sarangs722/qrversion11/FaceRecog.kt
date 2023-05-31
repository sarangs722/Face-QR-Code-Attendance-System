package com.sarangs722.qrversion11

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.sarangs722.qrversion11.databinding.ActivityFaceRecogBinding
import com.sarangs722.qrversion11.faceRecog.BitmapUtils
import com.sarangs722.qrversion11.faceRecog.FileReader
import com.sarangs722.qrversion11.faceRecog.FrameAnalyser
import com.sarangs722.qrversion11.faceRecog.models.FaceNetModel
import com.sarangs722.qrversion11.faceRecog.models.Models
import com.sarangs722.qrversion11.R
import java.io.*
import java.net.URI
import java.util.concurrent.Executors

class FaceRecog : AppCompatActivity() {
    private var isSerializedDataStored = false

    // Serialized data will be stored ( in app's private storage ) with this filename.
    private val SERIALIZED_DATA_FILENAME = "image_data"

    // Shared Pref key to check if the data was stored.
    private val SHARED_PREF_IS_DATA_STORED_KEY = "is_data_stored"

    private lateinit var faceBinding: ActivityFaceRecogBinding
    private lateinit var homeIntent: Intent
    private lateinit var previewView: PreviewView
    private lateinit var frameAnalyser: FrameAnalyser
    private lateinit var faceNetModel: FaceNetModel
    private lateinit var fileReader: FileReader
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var sharedPreferences: SharedPreferences


    // <----------------------- User controls --------------------------->

    // Use the device's GPU to perform faster computations.
    // Refer https://www.tensorflow.org/lite/performance/gpu
    private val useGpu = true

    // Use XNNPack to accelerate inference.
    // Refer https://blog.tensorflow.org/2020/07/accelerating-tensorflow-lite-xnnpack-integration.html
    private val useXNNPack = true

    // You may the change the models here.
    // Use the model configs in Models.kt
    // Default is Models.FACENET ; Quantized models are faster
    private val modelInfo = Models.FACENET

    // <---------------------------------------------------------------->


//    companion object {
//
//        lateinit var logTextView : TextView
//
//        fun setMessage( message : String ) {
//            logTextView.text = message
//        }
//
//    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove the status bar to have a full screen experience
        // See this answer on SO -> https://stackoverflow.com/a/68152688/10878733
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.decorView.windowInsetsController!!
//                .hide( WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
//        }
//        else {
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//        }

        faceBinding = ActivityFaceRecogBinding.inflate(layoutInflater)
        setContentView(faceBinding.root)
//        Log.e(TAG, userName)
        //userName +
//        Toast.makeText(this, intent.extras?.getString("uName") + " Received...", Toast.LENGTH_SHORT).show()
        previewView = faceBinding.previewView
//        logTextView = activityMainBinding.logTextview
//        logTextView.movementMethod = ScrollingMovementMethod()

        // Necessary to keep the Overlay above the PreviewView so that the boxes are visible.
        val boundingBoxOverlay = faceBinding.bboxOverlay
        boundingBoxOverlay.setWillNotDraw(false)
        boundingBoxOverlay.setZOrderOnTop(true)

        faceNetModel = FaceNetModel(this, modelInfo, useGpu, useXNNPack)
        frameAnalyser = FrameAnalyser(
            this, boundingBoxOverlay, faceNetModel,
            intent.extras?.getString("uName")!!
        )
        fileReader = FileReader(faceNetModel)

        faceBinding.homeButton.setOnClickListener {
            homeIntent = Intent(this, PrimaryActivity::class.java)
            homeIntent.putExtra("uName", intent.extras?.getString("uName"))
            startActivity(homeIntent)
        }

//        Toast.makeText(this, frameAnalyser.faceUserName + " Received...", Toast.LENGTH_SHORT).show()

        // We'll only require the CAMERA permission from the user.
        // For scoped storage, particularly for accessing documents, we won't require WRITE_EXTERNAL_STORAGE or
        // READ_EXTERNAL_STORAGE permissions. See https://developer.android.com/training/data-storage
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        } else {
            startCameraPreview()
        }

        sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        isSerializedDataStored = sharedPreferences.getBoolean(SHARED_PREF_IS_DATA_STORED_KEY, false)

//        isSerializedDataStored = !intent.hasExtra("serCheck")

        if (!isSerializedDataStored) {
//            directoryAccess();
            showSelectDirectoryDialog()
        }
        else {
            frameAnalyser.faceList = loadSerializedImageData()
        }


//
//        if ( !isSerializedDataStored ) {
////            Logger.log( "No serialized data was found. Select the images directory.")
//            showSelectDirectoryDialog()
//        }
//        else {
//            val alertDialog = AlertDialog.Builder(this).apply {
//                setTitle("Serialized Data")
//                setMessage("Existing image data was found on this device. Would you like to load it?")
//                setCancelable(false)
//                setNegativeButton("LOAD") { dialog, which ->
//                    dialog.dismiss()
///////////                    frameAnalyser.faceList = loadSerializedImageData()
//                    Logger.log( "Serialized data loaded.")
//                }
//                setPositiveButton("RESCAN") { dialog, which ->
//                    dialog.dismiss()
//                    launchChooseDirectoryIntent()
//                }
//                create()
//            }
//            alertDialog.show()

//            if (frameAnalyser.isFaceMatched) {
//                val alertDialog = AlertDialog.Builder( this ).apply {
//                            setTitle( "Face Recognized!")
//                            setMessage( "Please capture the QR Code now." )
//                            setCancelable( false )
//                            setPositiveButton( "Scan") { dialog, which ->
//                                dialog.dismiss()
//                                //moving to another Activity to scan QR Codes
//                                launchQRRecog()
//                            }
//                            create()
//                        }
//                alertDialog.show()
//            }
//        }
    }


    // ---------------------------------------------- //

    // Attach the camera stream to the PreviewView.
    private fun startCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder().build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val imageFrameAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(480, 640))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser)
        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
            imageFrameAnalysis
        )
    }

    // We let the system handle the requestCode. This doesn't require onRequestPermissionsResult and
    // hence makes the code cleaner.
    // See the official docs -> https://developer.android.com/training/permissions/requesting#request-permission
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCameraPreview()
            } else {
                val alertDialog = AlertDialog.Builder(this).apply {
                    setTitle("Camera Permission")
                    setMessage("The app couldn't function without the camera permission.")
                    setCancelable(false)
                    setPositiveButton("ALLOW") { dialog, which ->
                        dialog.dismiss()
                        requestCameraPermission()
                    }
                    setNegativeButton("CLOSE") { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    create()
                }
                alertDialog.show()
            }

        }


    // ---------------------------------------------- //

//    private fun searchDirectoryForImageData() {
//
//    }


     //Open File chooser to choose the images directory.
    @RequiresApi(Build.VERSION_CODES.N)
    private fun showSelectDirectoryDialog() {
        val alertDialog = AlertDialog.Builder(this).apply {
            setTitle("Select the QRApp directory")
            setMessage("This is for security reasons, just a one time hassle.")
            setCancelable(false)
            setPositiveButton("SELECT") { dialog, which ->
                dialog.dismiss()
                launchChooseDirectoryIntent()
            }
            create()
        }
        alertDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun launchChooseDirectoryIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        val file = DocumentFile.fromTreeUri(this, Uri.parse("content://com.android.externalstorage.documents/tree/primary%3APictures%2FQRApp"))
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, file!!.uri)
//        val path = Uri.parse(Environment.DIRECTORY_PICTURES + File.separator + "/QRApp")
//        val file = DocumentFile.fromTreeUri(this, path)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, file!!.uri)
//        }
        // startForActivityResult is deprecated.
        // See this SO thread -> https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
        directoryAccessLauncher.launch(intent)
    }


    // Read the contents of the select directory here.
    // The system handles the request code here as well.
    // See this SO question -> https://stackoverflow.com/questions/47941357/how-to-access-files-in-a-directory-given-a-content-uri

    private fun directoryAccess() {
//        Log.e("it", it.toString())
//        // ActivityResult{resultCode=RESULT_OK, data=Intent { dat=content://com.android.externalstorage.documents/tree/primary:Pictures/QRApp flg=0xc3 }}
//        Log.e("it.data", it.data.toString())
//        //Intent { dat=content://com.android.externalstorage.documents/tree/primary:Pictures/QRApp flg=0xc3 }

//        Uri.Builder().path("content//com.android.externalstorage.documents/tree/primary:Pictures/QRApp")
//        val dirUri = Uri.fromFile(File(Environment.getExternalStorageDirectory().getPath() +"/Pictures/QRApp"))


//        val dirUri = Uri.parse("content//com.android.externalstorage.documents/tree/primary:Pictures/QRApp")
        val dirUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3APictures%2FQRApp")
        Log.e("dirUri", dirUri.toString())


//        val dirUri = "content//com.android.externalstorage.documents/tree/primary:Pictures/QRApp"
        val childrenUri =
            DocumentsContract.buildChildDocumentsUriUsingTree(
                dirUri,
                DocumentsContract.getTreeDocumentId(dirUri)
            )
        Log.e("childrenUri",  childrenUri.toString())
        val tree = DocumentFile.fromTreeUri(this, childrenUri)
        Log.e("tree != null", (tree != null).toString())
        Log.e("tree", tree.toString())
        val images = ArrayList<Pair<String, Bitmap>>()
        var errorFound = false
        Log.e("tree!!", tree!!.toString())
        Log.e("tree!!.listFiles()", tree!!.listFiles().toString())
        Log.e("tree!!.listFiles().isNotEmpty()", tree!!.listFiles().isNotEmpty().toString())

        if (tree!!.listFiles().isNotEmpty()) {
            for (doc in tree.listFiles()) {
                if (doc.isDirectory && !errorFound) {
                    val name = doc.name!!
                    for (imageDocFile in doc.listFiles()) {
                        try {
                            images.add(Pair(name, getFixedBitmap(imageDocFile.uri)))
                        } catch (e: Exception) {
                            errorFound = true
//                            Logger.log( "Could not parse an image in $name directory. Make sure that the file structure is " +
//                                    "as described in the README of the project and then restart the app." )
                            break
                        }
                        Log.e("for imageDocFile", errorFound.toString())
                    }
//                    Logger.log( "Found ${doc.listFiles().size} images in $name directory" )
                } else {
                    errorFound = true
                    Log.e("for imageDocFile ELSE", errorFound.toString())
//                    Logger.log( "The selected folder should contain only directories. Make sure that the file structure is " +
//                            "as described in the README of the project and then restart the app." )
                }
            }
        } else {
            errorFound = true
            Log.e("if tree!!! ka else", errorFound.toString())
//            Logger.log( "The selected folder doesn't contain any directories. Make sure that the file structure is " +
//                    "as described in the README of the project and then restart the app." )
        }
        if (!errorFound) {
            fileReader.run(images, fileReaderCallback)
//            Logger.log( "Detecting faces in ${images.size} images ..." )
        } else {
            val alertDialog = AlertDialog.Builder(this).apply {
                setTitle("Error while parsing directory")
                setMessage(
                    "There were some errors while parsing the directory. Please see the log below. Make sure that the file structure is " +
                            "as described in the README of the project and then tap RESELECT"
                )
                setCancelable(false)
                setPositiveButton("RESELECT") { dialog, which ->
                    dialog.dismiss()
//                        launchChooseDirectoryIntent()
                }
                setNegativeButton("CANCEL") { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
                create()
            }
            alertDialog.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private val directoryAccessLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            Log.e("it", it.toString())
            // ActivityResult{resultCode=RESULT_OK, data=Intent { dat=content://com.android.externalstorage.documents/tree/primary:Pictures/QRApp flg=0xc3 }}
//            Log.e("it.data", it.data.toString())
            //Intent { dat=content://com.android.externalstorage.documents/tree/primary:Pictures/QRApp flg=0xc3 }

            val dirUri = it.data?.data ?: return@registerForActivityResult
            Log.e("dirUri", dirUri.toString())
//            content://com.android.externalstorage.documents/tree/primary%3APictures%2FQRApp

            val childrenUri =
                DocumentsContract.buildChildDocumentsUriUsingTree(
                    dirUri,
                    DocumentsContract.getTreeDocumentId(dirUri)
                )
            Log.e("childrenUri",  childrenUri.toString())
            val tree = DocumentFile.fromTreeUri(this, childrenUri)
            Log.e("tree != null", (tree != null).toString())
            Log.e("tree", tree.toString())

//            val tree = DocumentFile.fromTreeUri(this, childrenUri)
            val images = ArrayList<Pair<String, Bitmap>>()
            var errorFound = false
            Log.e("tree!!", tree!!.toString())
            Log.e("tree!!.listFiles()", tree!!.listFiles().toString())
            Log.e("tree!!.listFiles().isNotEmpty()", tree!!.listFiles().isNotEmpty().toString())

            if (tree!!.listFiles().isNotEmpty()) {
                for (doc in tree.listFiles()) {
                    if (doc.isDirectory && !errorFound) {
                        val name = doc.name!!
                        for (imageDocFile in doc.listFiles()) {
                            try {
                                images.add(Pair(name, getFixedBitmap(imageDocFile.uri)))
                            } catch (e: Exception) {
                                errorFound = true
//                            Logger.log( "Could not parse an image in $name directory. Make sure that the file structure is " +
//                                    "as described in the README of the project and then restart the app." )
                                break
                            }
                        }
//                    Logger.log( "Found ${doc.listFiles().size} images in $name directory" )
                    } else {
                        errorFound = true
//                    Logger.log( "The selected folder should contain only directories. Make sure that the file structure is " +
//                            "as described in the README of the project and then restart the app." )
                    }
                }
            } else {
                errorFound = true
//            Logger.log( "The selected folder doesn't contain any directories. Make sure that the file structure is " +
//                    "as described in the README of the project and then restart the app." )
            }
            if (!errorFound) {
                fileReader.run(images, fileReaderCallback)
//            Logger.log( "Detecting faces in ${images.size} images ..." )
            } else {
                val alertDialog = AlertDialog.Builder(this).apply {
                    setTitle("Error while parsing directory")
                    setMessage(
                        "There were some errors while parsing the directory. Please see the log below. Make sure that the file structure is " +
                                "as described in the README of the project and then tap RESELECT"
                    )
                    setCancelable(false)
                    setPositiveButton("RESELECT") { dialog, which ->
                        dialog.dismiss()
//                        launchChooseDirectoryIntent()
                    }
                    setNegativeButton("CANCEL") { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }
                    create()
                }
                alertDialog.show()
            }
        }


    // Get the image as a Bitmap from given Uri and fix the rotation using the Exif interface
    // Source -> https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
    @RequiresApi(Build.VERSION_CODES.N)
    private fun getFixedBitmap(imageFileUri: Uri): Bitmap {
        var imageBitmap = BitmapUtils.getBitmapFromUri(contentResolver, imageFileUri)
        val exifInterface = ExifInterface(contentResolver.openInputStream(imageFileUri)!!)
        imageBitmap =
            when (exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> BitmapUtils.rotateBitmap(imageBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> BitmapUtils.rotateBitmap(imageBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> BitmapUtils.rotateBitmap(imageBitmap, 270f)
                else -> imageBitmap
            }
        return imageBitmap
    }


    // ---------------------------------------------- //


    private val fileReaderCallback = object : FileReader.ProcessCallback {
        override fun onProcessCompleted(
            data: ArrayList<Pair<String, FloatArray>>,
            numImagesWithNoFaces: Int
        ) {
            frameAnalyser.faceList = data
            saveSerializedImageData(data)
//            Logger.log( "Images parsed. Found $numImagesWithNoFaces images with no faces." )
        }
    }


    private fun saveSerializedImageData(data: ArrayList<Pair<String, FloatArray>>) {
        val serializedDataFile = File(filesDir, SERIALIZED_DATA_FILENAME)
        ObjectOutputStream(FileOutputStream(serializedDataFile)).apply {
            writeObject(data)
            flush()
            close()
        }
        sharedPreferences.edit().putBoolean(SHARED_PREF_IS_DATA_STORED_KEY, true).apply()
    }


    private fun loadSerializedImageData(): ArrayList<Pair<String, FloatArray>> {
        val serializedDataFile = File(filesDir, SERIALIZED_DATA_FILENAME)
        val objectInputStream = ObjectInputStream(FileInputStream(serializedDataFile))
        val data = objectInputStream.readObject() as ArrayList<Pair<String, FloatArray>>
        objectInputStream.close()
        return data
    }
}
