package com.lebentech.lebentechtorniquetes.views.activities

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.lebentech.lebentechtorniquetes.R
import com.lebentech.lebentechtorniquetes.databinding.ActivityLoginBinding
import com.lebentech.lebentechtorniquetes.models.Screen
import com.lebentech.lebentechtorniquetes.views.activities.base.BaseActivity
import com.lebentech.lebentechtorniquetes.utils.Constants
import com.lebentech.lebentechtorniquetes.utils.Utils
import com.lebentech.lebentechtorniquetes.viewmodel.LoginViewModel

class LoginActivity : BaseActivity() {
    private val screen: Screen = Screen(true, Constants.LOGIN_ACTIVITY)
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun getViewBinding(): ViewBinding {
        if (!this::binding.isInitialized) {
            binding = ActivityLoginBinding.inflate(layoutInflater)
        }
        return binding
    }

    override fun getScreen(): Screen {
        return screen
    }

    @SuppressLint("ResourceAsColor")
    override fun prepareComponents() {
        hideActionBar()

        configureViewModel()

        binding.backButton.setOnClickListener {
            openRecognitionCamera()
        }

        binding.loginButton.setOnClickListener {
            val user = binding.usrField.text.toString()
            val pass = binding.passField.text.toString()

            if (user.isNotBlank() && pass.isNotBlank()) {
                viewModel.loginUser(user, pass)
            } else {
                Utils.createSnackBar(
                    applicationContext,
                    binding.root,
                    applicationContext.getString(R.string.lbl_user_pass_incorrect),
                    R.color.materialRed
                )
            }
            binding.lblWelcome.hideSoftInput()
        }
    }

    override fun onBackPressed() {
        openRecognitionCamera()
    }

    @SuppressLint("ResourceAsColor")
    private fun configureViewModel() {
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        viewModel.progress.observe(this) { inProgress ->
            if (inProgress) {
                binding.progressBar.visibility = View.VISIBLE
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
            } else {
                binding.progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }

        viewModel.loginProcess.observe(this) { loginProcess ->
            when (loginProcess) {
                Constants.ERROR_IN_LOGIN -> {
                    Utils.createSnackBar(
                        applicationContext,
                        binding.root,
                        applicationContext.getString(R.string.lbl_user_pass_incorrect),
                        R.color.materialRed
                    )
                }
                Constants.LOGGED_IN -> {
                    val minutesTimer = viewModel.minutesTimer.value ?: 0
                    openSedeActivity(isFinish = true, startTimer = true, minutes = minutesTimer)
                }
                Constants.UNAUTHORIZED_DEVICE -> {
                    Utils.createSnackBar(
                        applicationContext,
                        binding.root,
                        applicationContext.getString(R.string.lbl_unauthorized_device),
                        R.color.materialRed
                    )
                }
            }
        }
    }

    private fun View.hideSoftInput() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
}