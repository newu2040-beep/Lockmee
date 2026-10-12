package com.example.platform.camera

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object IntruderCameraManager {

    private const val TAG = "IntruderCamera"

    fun isCameraPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun captureIntruderPhoto(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        targetPackage: String,
        targetAppName: String,
        attempts: Int,
        onCaptureSaved: (File) -> Unit,
        onCaptureFailed: (String) -> Unit
    ) {
        if (!isCameraPermissionGranted(context)) {
            onCaptureFailed("Camera permission not granted")
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Try front camera first, fallback to back camera if front is unavailable
                val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    onCaptureFailed("No camera available on device")
                    return@addListener
                }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)

                // App-private internal storage: context.filesDir/intruders
                val intruderDir = File(context.filesDir, "intruders").apply {
                    if (!exists()) mkdirs()
                }

                // Add .nomedia file so photos are completely hidden from public gallery
                val noMedia = File(intruderDir, ".nomedia")
                if (!noMedia.exists()) {
                    noMedia.createNewFile()
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
                val cleanPkg = targetPackage.replace(".", "_")
                val photoFile = File(intruderDir, "intruder_${timestamp}_${cleanPkg}.jpg")

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                // Short delay to let camera sensor stabilize auto-exposure
                Handler(Looper.getMainLooper()).postDelayed({
                    imageCapture.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                cameraProvider.unbindAll()
                                if (photoFile.exists() && photoFile.length() > 0) {
                                    onCaptureSaved(photoFile)
                                } else {
                                    onCaptureFailed("Captured photo file was empty")
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                cameraProvider.unbindAll()
                                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                                onCaptureFailed(exception.message ?: "Camera capture error")
                            }
                        }
                    )
                }, 350)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera: ${e.message}", e)
                onCaptureFailed(e.message ?: "Camera binding error")
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
