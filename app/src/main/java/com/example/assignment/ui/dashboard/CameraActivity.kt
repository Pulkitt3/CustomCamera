package com.example.assignment.ui.dashboard

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import com.example.assignment.R
import com.example.assignment.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity(),SurfaceHolder.Callback, Camera.PictureCallback {

    private lateinit var binding: ActivityCameraBinding
    private var surfaceHolder: SurfaceHolder? = null
    private var cameraId: String? = null
    private lateinit var cameraManager: CameraManager
    private var camera: Camera? = null
    private var isFlashOn: Boolean = false
    private var isFlashlightOn = false
    private var isFrontCameraActive = false
    private lateinit var flashlightButton: Button
    private lateinit var switchCameraButton: Button
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private val neededPermissions = arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE,)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val result = checkPermission()
        if (result) {
            setupSurfaceHolder()
        }
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        switchCameraButton = findViewById(R.id.switchCameraButton)
        switchCameraButton.setOnClickListener {
            switchCamera()
        }
        flashlightButton = findViewById(R.id.alwaysOnFlash)
        flashlightButton.setOnClickListener {
            toggleFlashlight()
        }
    }

    
    private fun switchCamera() {
        releaseCamera()
        isFrontCameraActive = !isFrontCameraActive
        startCamera()
    }
    private fun checkPermission(): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            val permissionsNotGranted = ArrayList<String>()
            for (permission in neededPermissions) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsNotGranted.add(permission)
                }
            }
            if (permissionsNotGranted.size > 0) {
                var shouldShowAlert = false
                for (permission in permissionsNotGranted) {
                    shouldShowAlert =
                        ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                }
                
                val arr = arrayOfNulls<String>(permissionsNotGranted.size)
                val permissions = permissionsNotGranted.toArray(arr)
                if (shouldShowAlert) {
                    showPermissionAlert(permissions)
                } else {
                    requestPermissions(permissions)
                }
                return false
            }
        }
        return true
    }
    
    private fun showPermissionAlert(permissions: Array<String?>) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setCancelable(true)
        alertBuilder.setTitle(R.string.permission_required)
        alertBuilder.setMessage(R.string.permission_message)
        alertBuilder.setPositiveButton(R.string.yes) { _, _ -> requestPermissions(permissions) }
        val alert = alertBuilder.create()
        alert.show()
    }
    
    private fun requestPermissions(permissions: Array<String?>) {
        ActivityCompat.requestPermissions(this@CameraActivity, permissions, REQUEST_CODE)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> {
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(
                            this@CameraActivity,
                            R.string.permission_warning,
                            Toast.LENGTH_LONG
                        ).show()
                        binding.showPermissionMsg.visibility = View.VISIBLE
                        return
                    }
                }
                
                setupSurfaceHolder()
            }
        }
        
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    
    private fun setupSurfaceHolder() {
        binding.startBtn.visibility = View.VISIBLE
        binding.surfaceView.visibility = View.VISIBLE
        
        surfaceHolder = binding.surfaceView.holder
        binding.surfaceView.holder.addCallback(this)
        setBtnClick()
    }
    private fun setBtnClick() {
        binding.startBtn.setOnClickListener { captureImage() }
    }
    
    private fun captureImage() {
        if (camera != null) {
            if (binding.flashBtn.isChecked && !isFrontCameraActive ) {
                turnOnFlash()
            } else {
                turnOffFlash()
            }
            camera!!.takePicture(null, null, this)
        }
    }
    
    private fun turnOnFlash() {
        val parameters = camera?.parameters
        parameters?.flashMode = Camera.Parameters.FLASH_MODE_ON
        camera?.parameters = parameters
        
    }
    
    private fun turnOffFlash() {
        val parameters = camera?.parameters
        parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
        camera?.parameters = parameters
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (binding.flashBtn.isChecked) {
            turnOnFlash()
//            toggleFlash()
        }
        startCamera()
    }
    private fun startCamera() {
        val cameraId = if (isFrontCameraActive) {
            getFrontCameraId()
            
        } else {
            getRearCameraId()
        }
        
        camera = Camera.open(cameraId)
        camera!!.setDisplayOrientation(90)
        try {
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
    }
    private fun getFrontCameraId(): Int {
        
        val cameraInfo = Camera.CameraInfo()
        val numberOfCameras = Camera.getNumberOfCameras()
        for (cameraId in 0 until numberOfCameras) {
            Camera.getCameraInfo(cameraId, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return cameraId
            }
        }
        // Default to the first camera if a front camera is not available
        return 0
    }
    private fun turnFlashAlwaysOn() {
        try {
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH
            )
            
        } catch (e: CameraAccessException) {
            // Handle flashlight access exception
        }
    }
    private fun getRearCameraId(): Int {
        val cameraInfo = Camera.CameraInfo()
        val numberOfCameras = Camera.getNumberOfCameras()
        for (cameraId in 0 until numberOfCameras) {
            Camera.getCameraInfo(cameraId, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return cameraId
            }
        }
        // Default to the first camera if a rear camera is not available
        return 0
    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        resetCamera()
        
    }
    private fun resetCamera() {
        if (surfaceHolder!!.surface == null) {
            // Return if preview surface does not exist
            return
        }
        
        // Stop if preview surface is already running.
        camera!!.stopPreview()
        try {
            // Set preview display
            camera!!.setPreviewDisplay(surfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // Start the camera preview...
        camera!!.startPreview()
    }
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseCamera()
    }
    private fun releaseCamera() {
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }
    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        saveImage(data!!)
        resetCamera()
    }
    private fun saveImage(bytes: ByteArray) {
        val folderName = "XBIz" // Replace with your desired folder name
        val outStream: FileOutputStream
        try {
            val fileName = "XBIZ" + System.currentTimeMillis() + ".jpg"
            val picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val folder = File(picturesDirectory, folderName)
            if (!folder.exists()) {
                folder.mkdirs() // Create the folder if it doesn't exist
            }
            val file = File(folder, fileName)
            outStream = FileOutputStream(file)
            outStream.write(bytes)
            outStream.close()
            Toast.makeText(this@CameraActivity, "Picture Saved: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    companion object {
        const val REQUEST_CODE = 100
    }
    private fun turnOnFlashlight() {
        val parameters = camera?.parameters
        parameters?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
        camera?.parameters = parameters
    }
    private fun turnOffFlashlight() {
        val parameters = camera?.parameters
        parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
        camera?.parameters = parameters
    }
    private fun toggleFlashlight() {
        isFlashlightOn = !isFlashlightOn
        if (isFlashlightOn) {
            turnOnFlashlight()
        } else {
            turnOffFlashlight()
        }
    }
}