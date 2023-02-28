package com.lebentech.lebentechtorniquetes.views.activities

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieDrawable
import com.lebentech.lebentechtorniquetes.R
import com.lebentech.lebentechtorniquetes.databinding.ActivityCameraBinding
import com.lebentech.lebentechtorniquetes.interfaces.WelcomeDialogListener
import com.lebentech.lebentechtorniquetes.models.Screen
import com.lebentech.lebentechtorniquetes.views.activities.base.BaseActivity
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.viewmodel.CameraRecognitionViewModel
import com.lebentech.lebentechtorniquetes.views.dialogs.WelcomeDialog
import java.util.*

class CameraActivity : BaseActivity() {

    companion object {
        // When the app is recently launched the last recognition date will be the launched time, then
        // it will change with the next face recognitions
        var lastRecognition: Date = Date()
    }

    private lateinit var binding: ActivityCameraBinding

    private val screen: Screen = Screen(true, Constants.CAMERA_ACTIVITY)

    private lateinit var viewModel: CameraRecognitionViewModel

    /**
     * Activity base class implementation
     */
    override fun getViewBinding(): ViewBinding {
        if (!this::binding.isInitialized) {
            binding = ActivityCameraBinding.inflate(layoutInflater)
        }
        return binding
    }

    override fun getScreen(): Screen {
        return screen
    }

    /**
     * Prepare the view model, listeners for the buttons and the camera manager
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun prepareComponents() {
        // View model
        setFullScreenActivity()
        setViewModel()

        // Listener
        binding.configButton.setOnClickListener {
            openLoginActivity()
        }

        binding.brightnessButton.setOnClickListener {
            viewModel.getCameraExposure()
            if (binding.seekBar.visibility == View.GONE) {
                binding.seekBar.visibility = View.VISIBLE
            } else {
                binding.seekBar.visibility = View.GONE
            }
            // BrightnessDialog().show(supportFragmentManager, Constants.DIALOG_TAG)
        }

        viewModel.setCameraManager(binding, this)

        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.setCameraRangeExposure(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        lastRecognition = Date()
    }

    /**
     * When the app is already launched it will wait for 3 seconds to start recording faces
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.setFacesSize(binding.cameraView.height.toFloat(), binding.cameraView.width.toFloat())
            setObservers()
            val range = viewModel.getCameraRangeExposure()
            binding.seekBar.min = range.lower
            binding.seekBar.max = range.upper
            binding.seekBar.progress = viewModel.getCameraExposure()
        }, 3000)
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(this)[CameraRecognitionViewModel::class.java]
    }

    private fun setObservers() {
        viewModel.getRecognitionState().observe(this) { state ->
            when (state) {
                Constants.NO_FACE_IN_FRONT -> {
                    binding.tvState.setText(R.string.textNoFaceDetected)
                    binding.llContainer.background = ContextCompat.getDrawable(applicationContext, R.color.colorGolden)
                    binding.stepper.currentStep = 0
                }
                Constants.FACE_TOO_FAR -> {
                    binding.tvState.setText(R.string.textFaceTooFar)
                    binding.llContainer.background = ContextCompat.getDrawable(applicationContext, R.color.colorGolden)
                    binding.stepper.currentStep = 1
                }
                Constants.FACE_TOO_CLOSE -> {
                    binding.tvState.setText(R.string.textFaceTooClose)
                    binding.llContainer.background = ContextCompat.getDrawable(applicationContext, R.color.colorGolden)
                    binding.stepper.currentStep = 1
                }
                Constants.FACE_DETECTED -> {
                    binding.tvState.setText(R.string.textFaceDetected)
                    binding.llContainer.background = ContextCompat.getDrawable(applicationContext, R.color.colorGolden)
                    binding.stepper.currentStep = 2

                    // Start the spinner animation when the request is sent
                    binding.configButton.visibility = View.GONE
                    startSpinnerAnimation()
                }
                Constants.PHOTO_TAKEN -> {
                    // When the photo is taken it will stop the spinner animation to show the camera
                    // animation, then when the animation is finished it will show again the spinner
                    // animation
                    binding.ivAnimation.visibility = View.GONE
                    binding.ivAnimationCamera.visibility = View.VISIBLE
                    binding.ivAnimationCamera.setAnimation(R.raw.camera)
                    binding.ivAnimationCamera.playAnimation()
                    binding.ivAnimationCamera.addAnimatorListener(object: AnimatorListener {
                        override fun onAnimationStart(animation: Animator) { }

                        override fun onAnimationEnd(animation: Animator) {
                            binding.ivAnimationCamera.visibility = View.GONE
                            binding.ivAnimationCamera.cancelAnimation()
                        }

                        override fun onAnimationCancel(animation: Animator) {
                            if (viewModel.getRecognitionState().value == Constants.PHOTO_TAKEN) {
                                startSpinnerAnimation()
                            }
                        }

                        override fun onAnimationRepeat(animation: Animator) { }
                    })
                }
                Constants.ERROR_IN_DETECTION -> {
                    if (CameraRecognitionViewModel.welcomeMessage.isEmpty()) {
                        binding.tvState.setText(R.string.textErrorInDetection)
                    } else {
                        binding.tvState.text = CameraRecognitionViewModel.welcomeMessage
                    }
                    binding.llContainer.background = ContextCompat.getDrawable(applicationContext, R.color.colorRed)
                    binding.stepper.currentStep = 0

                    binding.configButton.visibility = View.VISIBLE
                    binding.ivAnimation.visibility = View.GONE
                    restartRecognition()
                }
                Constants.NO_DETECTION_COINCIDENCES -> {
                    if (CameraRecognitionViewModel.welcomeMessage.isEmpty()) {
                        binding.tvState.setText(R.string.textNoCoincidences)
                    } else {
                        binding.tvState.text = CameraRecognitionViewModel.welcomeMessage
                    }
                    binding.llContainer.background = ContextCompat.getDrawable(applicationContext, R.color.colorRed)
                    binding.stepper.currentStep = 2

                    binding.configButton.visibility = View.VISIBLE
                    binding.ivAnimation.visibility = View.GONE
                    restartRecognition()
                }
                Constants.CORRECT_DETECTION -> {
                    binding.tvState.text = CameraRecognitionViewModel.welcomeMessage
                    binding.llContainer.background = ContextCompat.getDrawable(applicationContext, R.color.colorGreen)
                    binding.stepper.currentStep = 3

                    binding.configButton.visibility = View.VISIBLE
                    binding.ivAnimation.visibility = View.GONE

                    val userInfo = CameraRecognitionViewModel.userInfo
                    val welcomeMessage = CameraRecognitionViewModel.welcomeMessage

                    lastRecognition = Date()

                    WelcomeDialog( object: WelcomeDialogListener {
                        override fun onAppear() {
                            binding.ivAnimation.visibility = View.GONE
                            binding.faceSizeReference.visibility = View.GONE
                            binding.backgroundDialog.visibility = View.VISIBLE
                        }

                        override fun onDisappear() {
                            binding.faceSizeReference.visibility = View.VISIBLE
                            binding.backgroundDialog.visibility = View.GONE
                            restartRecognition()
                        }
                    }, userInfo.employeeName.lowercase(), userInfo.employeeNumber, userInfo.employeeBirthday ?: "", welcomeMessage)
                        .show(supportFragmentManager, Constants.DIALOG_TAG)
                }
            }
        }
    }

    fun restartRecognition() {
        Handler(Looper.getMainLooper()).postDelayed( {
            viewModel.setRecognitionState(Constants.NO_FACE_IN_FRONT)
            viewModel.stopRecognition()
        }, 1500 )
    }

    fun startSpinnerAnimation() {
        binding.ivAnimation.visibility = View.VISIBLE
        binding.ivAnimation.setAnimation(R.raw.spinner)
        binding.ivAnimation.repeatCount = LottieDrawable.INFINITE
        binding.ivAnimation.speed = 1.25F
        binding.ivAnimation.playAnimation()
    }
}