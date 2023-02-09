/**
 * Created by Gerardo Garzon on 16/01/23.
 */

package com.lebentech.lebentechtorniquetes.viewmodel

import android.app.Application
import android.view.ViewGroup
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.lebentech.lebentechtorniquetes.databinding.ActivityCameraBinding
import com.lebentech.lebentechtorniquetes.interfaces.FaceRecognitionListener
import com.lebentech.lebentechtorniquetes.interfaces.PhotoTakenListener
import com.lebentech.lebentechtorniquetes.models.FaceSize
import com.lebentech.lebentechtorniquetes.modules.camerax.CameraManager
import com.lebentech.lebentechtorniquetes.modules.camerax.GraphicOverlay
import com.lebentech.lebentechtorniquetes.retrofit.reponses.EmployeeInfoResponse
import com.lebentech.lebentechtorniquetes.retrofit.reponses.GeneralResponse
import com.lebentech.lebentechtorniquetes.utils.Constants
import java.util.concurrent.Executors

class CameraRecognitionViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        lateinit var userInfo: EmployeeInfoResponse
        lateinit var welcomeMessage: String
    }

    private lateinit var cameraManager: CameraManager

    private lateinit var overlay: GraphicOverlay

    private var cameraExecutor = Executors.newSingleThreadExecutor()

    private var imageExecutor = Executors.newSingleThreadExecutor()

    private var isRunningFaceRecognition = MutableLiveData<Boolean>()

    private var recognitionState = MutableLiveData<Int>()


    fun getRecognitionState(): MutableLiveData<Int> {
        return recognitionState
    }

    fun setRecognitionState(state: Int) {
        recognitionState.postValue(state)
    }

    fun stopRecognition() {
        isRunningFaceRecognition.postValue(false)
    }

    fun setCameraManager(binding: ActivityCameraBinding, lifecycle: LifecycleOwner) {
        overlay = GraphicOverlay(getApplication<Application>().applicationContext)
        overlay.rotationY = 180.0f

        val layoutOverlay = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        binding.cameraView.addView(overlay, layoutOverlay)
        cameraManager = CameraManager(getApplication<Application>().applicationContext,
            binding,
            lifecycle,
            cameraExecutor,
            imageExecutor,
            overlay
        )
        cameraManager.startCamera()
    }

    fun setFacesSize(center: Float, width: Float) {
        // Get the maximum face size and the minimum face size

        val maxFaceSize: FaceSize
        val minFaceSize: FaceSize

        val startXMax = width * 0.20
        val endXMax = width * 0.80
        val startYMax = center * 0.20
        val endYMax = center * 0.80

        val startXMin = width * 0.35
        val endXMin = width * 0.65
        val startYMin = center * 0.35
        val endYMin = center * 0.65

        maxFaceSize = FaceSize(startXMax.toFloat(), endXMax.toFloat(), startYMax.toFloat(), endYMax.toFloat())
        minFaceSize = FaceSize(startXMin.toFloat(), endXMin.toFloat(), startYMin.toFloat(), endYMin.toFloat())

        overlay.setFaceSizes(maxFaceSize, minFaceSize)

        // Overlay face recognition listener, if the listener detect a face it should stop until the viewmodel
        // get a response from the api
        overlay.setFaceRecognitionListener( object: FaceRecognitionListener {
            override fun faceDetected() {
                if ( isRunningFaceRecognition.value != true ) {
                    recognitionState.postValue(Constants.FACE_DETECTED)
                    isRunningFaceRecognition.postValue(true)
                    takePhoto()
                }
            }

            override fun faceTooFar() {
                if ( isRunningFaceRecognition.value != true ) {
                    recognitionState.postValue(Constants.FACE_TOO_FAR)
                }
            }

            override fun faceTooClose() {
                if ( isRunningFaceRecognition.value != true ) {
                    recognitionState.postValue(Constants.FACE_TOO_CLOSE)
                }
            }

            override fun noFaceDetection() {
                if ( isRunningFaceRecognition.value != true ) {
                    recognitionState.postValue(Constants.NO_FACE_IN_FRONT)
                }
            }
        })
    }

    fun takePhoto() {
        cameraManager.takePhoto(object: PhotoTakenListener {
            override fun onSuccess(model: GeneralResponse<EmployeeInfoResponse>) {
                userInfo = model.data
                welcomeMessage = model.message
                recognitionState.postValue(Constants.CORRECT_DETECTION)
            }

            override fun onFailure(error: Int) {
                if (error == Constants.NO_DETECTION_COINCIDENCES) {
                    recognitionState.postValue(Constants.NO_DETECTION_COINCIDENCES)
                } else {
                    recognitionState.postValue(Constants.ERROR_IN_DETECTION)
                }
            }

            override fun photoTaken() {
                recognitionState.postValue(Constants.PHOTO_TAKEN)
            }
        })
    }
}