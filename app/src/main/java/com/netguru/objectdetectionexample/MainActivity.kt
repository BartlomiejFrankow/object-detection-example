package com.netguru.objectdetectionexample

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.netguru.objectdetectionexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (cameraPermissionGranted()) startCamera()
        else ActivityCompat.requestPermissions(this, Constants.CAMERA_PERMISSION, Constants.REQUEST_CODE_PERMISSION)
    }

    private fun cameraPermissionGranted() = Constants.CAMERA_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.REQUEST_CODE_PERMISSION) {
            if (cameraPermissionGranted()) startCamera()
            else {
                Toast.makeText(this, "Camera permission is needed", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { preview -> preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider) }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.d(Constants.TAG, "Camera start fail: ", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

}
