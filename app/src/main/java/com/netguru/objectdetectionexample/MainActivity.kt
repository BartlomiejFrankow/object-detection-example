package com.netguru.objectdetectionexample

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.netguru.objectdetectionexample.databinding.ActivityMainBinding
import com.netguru.objectdetectionexample.utils.Draw

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var objectDetector: ObjectDetector

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
            else finish()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            bindCameraPreview(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))

        setObjectDetector()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraPreview(cameraProvider: ProcessCameraProvider) {

        val preview = Preview.Builder()
            .build()
            .also { preview -> preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider) }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = imageProxy.image

            if (image != null) processImage(image, rotationDegrees, imageProxy)
        })

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)
        } catch (e: Exception) {
            Log.d(Constants.TAG, "Camera start fail: ", e)
        }
    }

    private fun processImage(image: Image, rotationDegrees: Int, imageProxy: ImageProxy) {
        val imageInput = InputImage.fromMediaImage(image, rotationDegrees)

        objectDetector
            .process(imageInput)
            .addOnSuccessListener { objects ->
                for (ob in objects) {
                    if (binding.parentLayout.childCount > 1) binding.parentLayout.removeViewAt(1)

                    binding.parentLayout.addView(
                        Draw(
                            context = this,
                            rect = ob.boundingBox,
                            name = ob.labels.lastOrNull()?.text ?: "Undefined",
                            confidence = ob.labels.firstOrNull()?.confidence
                        )
                    )
                }
                imageProxy.close()
            }.addOnFailureListener {
                Log.v("MainActivity", "Error: ${it.message}")
                imageProxy.close()
            }
    }


    private fun setObjectDetector() {
        val model = LocalModel.Builder()
            .setAssetFilePath("lite-model_object_detection_mobile_object_labeler_v1_1.tflite")
            .build()


        val customOptions = CustomObjectDetectorOptions.Builder(model)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(2)
            .build()

        objectDetector = ObjectDetection.getClient(customOptions)
    }

}
