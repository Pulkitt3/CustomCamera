package com.example.assignment.ui.dashboard

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Point
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.SurfaceHolder
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import com.example.assignment.R
import com.example.assignment.databinding.ActivityCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback, Camera.PictureCallback {

    private lateinit var binding: ActivityCameraBinding
    private var surfaceHolder: SurfaceHolder? = null
    private var cameraId: String? = null
    private lateinit var cameraManager: CameraManager
    private var camera: Camera? = null
    private var isFlashOn: Boolean = false
    private var isFlashlightOn = false
    private var isFrontCameraActive = false
    private lateinit var flashlightButton: ImageView
    private lateinit var switchCameraButton: ImageView
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private val neededPermissions = arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE)

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
        binding.settingsButton.setOnClickListener {
            openAppSettings()
        }

        binding.flashBtn.setOnClickListener {
            checkFlashIsOnOrNot()
        }
    }

    private fun openAppSettings() {
        val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        appSettingsIntent.data = uri
        startActivity(appSettingsIntent)
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
        grantResults: IntArray,
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
                        binding.settingsButton.visibility = View.VISIBLE
                        return
                    }
                }

                setupSurfaceHolder()
            }

            else -> {

                binding.showPermissionMsg.visibility = View.GONE
                binding.settingsButton.visibility = View.GONE
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupSurfaceHolder() {
        binding.startBtn.visibility = View.VISIBLE
        binding.surfaceView.visibility = View.VISIBLE

        binding.showPermissionMsg.visibility = View.GONE
        binding.settingsButton.visibility = View.GONE

        surfaceHolder = binding.surfaceView.holder
        binding.surfaceView.holder.addCallback(this)
        setBtnClick()
    }

    private fun setBtnClick() {
        binding.startBtn.setOnClickListener { captureImage() }
    }

    private fun captureImage() {
        if (camera != null) {
            if (isFlashOn && !isFrontCameraActive) {
                turnOnFlash()
            } else {
                turnOffFlash()
            }
            camera!!.takePicture(null, null, this)
        }
    }

    private fun checkFlashIsOnOrNot() {
        if (camera != null) {
            isFlashOn = !isFlashOn
            if (isFlashOn && !isFrontCameraActive) {
                turnOnFlash()
            } else {
                turnOffFlash()
            }
        }
    }


    private fun turnOnFlash() {
        val parameters = camera?.parameters
        parameters?.flashMode = Camera.Parameters.FLASH_MODE_ON
        camera?.parameters = parameters

        binding.flashBtn.setImageDrawable(getDrawable(R.drawable.baseline_flash_on))

    }

    private fun turnOffFlash() {
        val parameters = camera?.parameters
        parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
        camera?.parameters = parameters
        binding.flashBtn.setImageDrawable(getDrawable(R.drawable.baseline_flash_off))
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (isFlashOn) {
            turnOnFlash()
//            toggleFlash()
        }
        if (camera != null) {
            camera?.let { configureCameraParameters(it) }
        }
        startCamera()
    }

    private fun startCamera() {
        val cameraId = if (isFrontCameraActive) {
            getFrontCameraId()

        } else {
            getRearCameraId()
        }
        setSurfaceViewAspectRatio(cameraId)
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

    private fun setSurfaceViewAspectRatio(cameraId: Int) {
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, cameraInfo)

        val parameters = camera?.parameters
        val previewSizes = parameters?.supportedPreviewSizes

        val surfaceViewWidth = binding.surfaceView.width ?: 0
        val surfaceViewHeight = binding.surfaceView.height ?: 0

        if (previewSizes != null && surfaceViewWidth != 0 && surfaceViewHeight != 0) {
            val targetRatio = if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                // For front camera, use the same aspect ratio as the rear camera
                val rearCameraInfo = Camera.CameraInfo()
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, rearCameraInfo)
                val rearAspectRatio = getAspectRatio(rearCameraInfo, previewSizes)
                if (rearAspectRatio != null) {
                    rearAspectRatio.width.toDouble() / rearAspectRatio.height.toDouble()
                } else {
                    0.0
                }
            } else {
                // For rear camera, use the aspect ratio of the available preview sizes
                getAspectRatio(cameraInfo, previewSizes)?.let { aspectRatio ->
                    aspectRatio.width.toDouble() / aspectRatio.height.toDouble()
                } ?: 0.0
            }

            val display = windowManager.defaultDisplay
            val displaySize = Point()
            display.getSize(displaySize)
            val displayAspectRatio = displaySize.x.toDouble() / displaySize.y.toDouble()

            val layoutParams = binding.surfaceView?.layoutParams as FrameLayout.LayoutParams
            if (displayAspectRatio > targetRatio) {
                // Fit width, adjust height
                layoutParams.width = surfaceViewWidth
                layoutParams.height = (surfaceViewWidth / targetRatio).toInt()
            } else {
                // Fit height, adjust width
                layoutParams.width = (surfaceViewHeight * targetRatio).toInt()
                layoutParams.height = surfaceViewHeight
            }
            binding.surfaceView.layoutParams = layoutParams
        }
    }

    private fun getAspectRatio(cameraInfo: Camera.CameraInfo, previewSizes: List<Camera.Size>): Camera.Size? {
        val targetRatio = 4.0 / 3.0 // Desired aspect ratio, e.g., 4:3
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        for (size in previewSizes) {
            val ratio = size.width.toDouble() / size.height.toDouble()
            val diff = Math.abs(ratio - targetRatio)
            if (diff < minDiff) {
                optimalSize = size
                minDiff = diff
            }
        }

        return optimalSize
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

    private fun configureCameraParameters(camera: Camera) {
        val parameters = camera.parameters

        // Find the suitable preview size based on the aspect ratio of the view
        val previewSize = getOptimalPreviewSize(camera)

        // Set the preview size
        parameters.setPreviewSize(previewSize.width, previewSize.height)

        // Set the desired orientation (portrait or landscape)
        camera.setDisplayOrientation(90)

        // Apply the updated parameters
        camera.parameters = parameters
    }

    private fun getOptimalPreviewSize(camera: Camera): Camera.Size {
        val targetRatio = 1.3333 // Example aspect ratio: 4:3
        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        val sizes = camera.parameters.supportedPreviewSizes
        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height.toDouble()
            val diff = Math.abs(ratio - targetRatio)
            if (diff < minDiff) {
                optimalSize = size
                minDiff = diff
            }
        }

        return optimalSize ?: sizes.first() // Return the first size if no suitable size is found
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
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val folderName = "XbizImages" // Replace with your desired folder name
                val outStream: FileOutputStream
                try {
                    val fileName = "XBIZ" + System.currentTimeMillis() + ".jpg"
                    val picturesDirectory =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val folder = File(picturesDirectory, folderName)
                    if (!folder.exists()) {
                        folder.mkdirs() // Create the folder if it doesn't exist
                    }
                    val file = File(folder, fileName)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    // Rotate the bitmap by 90 degrees
                    val rotatedBitmap = rotateImage(bitmap, ExifInterface.ORIENTATION_ROTATE_90)

                    // Save the rotated bitmap to the file
                    outStream = FileOutputStream(file)
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                    outStream.close()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@CameraActivity,
                            "Picture Saved: $fileName",
                            Toast.LENGTH_LONG
                        )
                            .show()

                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }
    }

    private fun rotateImage(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    companion object {
        const val REQUEST_CODE = 100
    }

    private fun turnOnFlashlight() {
        val parameters = camera?.parameters
        parameters?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
        camera?.parameters = parameters
        flashlightButton.setImageDrawable(getDrawable(R.drawable.baseline_tourch_on))

    }

    private fun turnOffFlashlight() {
        val parameters = camera?.parameters
        parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
        camera?.parameters = parameters
        flashlightButton.setImageDrawable(getDrawable(R.drawable.baseline_tourch_off))

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