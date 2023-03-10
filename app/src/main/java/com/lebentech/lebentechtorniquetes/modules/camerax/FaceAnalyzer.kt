/**
 * Created by Gerardo Garzon on 02/01/23.
 */

package com.lebentech.lebentechtorniquetes.modules.camerax

import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import android.os.SystemClock
import android.util.Log
import android.util.Size
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.lebentech.lebentechtorniquetes.modules.camerax.listeners.ImageCapturedListener
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.ImageUtils
import com.lebentech.lebentechtorniquetes.utils.LogUtils
import kotlin.properties.Delegates


class FaceAnalyzer(lifecycle: Lifecycle, private val overlay: GraphicOverlay, private val listener: ImageCapturedListener): ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = Constants.DETECTION_TAG
        var fps by Delegates.notNull<Double>()
        var lastFrameTime = SystemClock.elapsedRealtime()
    }

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.15f)
        .build()

    private val detector = FaceDetection.getClient(options)

    init {
        lifecycle.addObserver(detector)
        fps = 0.0
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        overlay.setPreviewSize(Size(image.width, image.height))
        detectFaces(image)
        val currentTime = SystemClock.elapsedRealtime()
        val elapsedTime = currentTime - lastFrameTime
        lastFrameTime = currentTime
        fps = 1000.0 / (elapsedTime)
        Log.println(Log.INFO, "FPS", "Fps counter: $fps")
    }

    @ExperimentalGetImage
    private fun detectFaces(imageProxy: ImageProxy) {
        val image = InputImage.fromMediaImage(imageProxy.image as Image, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener { faces ->
                overlay.setFaces(faces)
            }
            .addOnFailureListener { exception ->
                LogUtils.printLogError(TAG, "Face analysis failure.", exception)
            }
            .addOnCompleteListener{
                imageProxy.close()
            }
    }

    @ExperimentalGetImage
    private fun sendImageListener(image: ImageProxy) {
        image.image?.let { ImageUtils.toBitmap(it) }?.let {
            ImageUtils.rotateBitmap(it, 270.0f)?.let { bitmapRotated ->
                ImageUtils.mirrorBitmap(bitmapRotated)?.let { bitmapMirrored ->
                    listener.imageCaptured(bitmapMirrored)
                }
            }
        }
    }
}