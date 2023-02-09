/**
 * Created by Gerardo Garzon on 23/12/22.
 */

package com.lebentech.lebentechtorniquetes.modules.camerax

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.lebentech.lebentechtorniquetes.databinding.ActivityCameraBinding
import com.lebentech.lebentechtorniquetes.interfaces.FaceRecognitionResponseListener
import com.lebentech.lebentechtorniquetes.interfaces.PhotoTakenListener
import com.lebentech.lebentechtorniquetes.modules.camerax.listeners.ImageCapturedListener
import com.lebentech.lebentechtorniquetes.repositories.FaceRecognitionRepository
import com.lebentech.lebentechtorniquetes.retrofit.reponses.EmployeeInfoResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.retrofit.request.FaceRecognitionRequest
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.GetProperImageRotation
import com.lebentech.lebentechtorniquetes.utils.LogUtils
import com.lebentech.lebentechtorniquetes.utils.Utils
import java.io.File
import java.util.concurrent.ExecutorService

class CameraManager(appContext: Context, appBinding: ActivityCameraBinding, private var lifeCycle: LifecycleOwner, private var cameraExecutor: ExecutorService, private var imageExecutorService: ExecutorService, private var overlay: GraphicOverlay) {

    private var context: Context = appContext

    private var binding: ActivityCameraBinding = appBinding

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var cameraSelector: CameraSelector

    private var imageCapture: ImageCapture? = null

    private var recognitionRepository: FaceRecognitionRepository = FaceRecognitionRepository()

    /**
     * Start the camera preview
     */
    fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Connecting a preview use case to the preview in the xml file.
            val previewUseCase = Preview.Builder().build().also{
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            // Connecting a image analyzer use case to detect faces and extract the image preview
            // to send it through the server socket
            val analysisUseCase = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, FaceAnalyzer(lifeCycle.lifecycle, overlay, object:
                        ImageCapturedListener {
                        override fun imageCaptured(image: Bitmap) {
                            LogUtils.printLog("Algo")
                        }
                    }))
                }

            // Create the image capture usage case to take a photo form the camera view
            imageCapture = ImageCapture.Builder()
                .build()

            try{
                // clear all the previous use cases first.
                cameraProvider.unbindAll()
                // binding the lifecycle of the camera to the lifecycle of the application.
                cameraProvider.bindToLifecycle(lifeCycle, cameraSelector, previewUseCase,
                    imageCapture, analysisUseCase)
            } catch (e: Exception) {
                LogUtils.printLog("CameraManager",
                    "Use case binding failed: " + e.localizedMessage)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Takes a photo from the camera when the face recognition is ready
     * It will be saved on the device documents directory with the name FR_Image.jpeg
     */
    fun takePhoto(listener: PhotoTakenListener) {
        Handler(Looper.getMainLooper()).postDelayed({
            imageCapture?.let {
                val externalMediaDirs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val file = File(externalMediaDirs,"FR_IMAGE.jpeg")

                val outputFileOptions = ImageCapture.OutputFileOptions
                    .Builder(file)
                    .build()

                it.takePicture(outputFileOptions, imageExecutorService, object: ImageCapture.OnImageSavedCallback {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            listener.photoTaken()

                            val newPath = file.toURI().toString().replace("file:", "")
                            GetProperImageRotation.getRotatedImageFile(File(newPath),context)
                            sendPhotoToRecognition(newPath, listener)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            listener.onFailure(Constants.ERROR_IN_DETECTION)
                        }

                    }
                )
            }
        }, 1500)
    }

    /**
     * When the photo is saved it will take it and convert to byte array
     * Then it will call the recognition service and consume the listener depending on the result
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendPhotoToRecognition(path: String, listener: PhotoTakenListener) {
        val byteImage = Utils.convertImageToByteArray(path)
        val androidID = Utils.getAndroidID(context)

        val model = FaceRecognitionRequest(androidID, byteImage)

        recognitionRepository.sendFaceRecognitionRequest(model, object : FaceRecognitionResponseListener {
            override fun onSuccess(model: GeneralResponse<EmployeeInfoResponse>) {
                listener.onSuccess(model)
            }

            override fun onFailure(code: Int) {
                if (code == 409) {
                    listener.onFailure(Constants.NO_DETECTION_COINCIDENCES)
                } else {
                    listener.onFailure(Constants.ERROR_IN_DETECTION)
                }
            }
        }, context)

    }
}