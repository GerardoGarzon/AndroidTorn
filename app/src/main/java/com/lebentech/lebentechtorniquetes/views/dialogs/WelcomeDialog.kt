/**
 * Created by Gerardo Garzon on 16/01/23.
 */

package com.lebentech.lebentechtorniquetes.views.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieDrawable
import com.lebentech.lebentechtorniquetes.R
import com.lebentech.lebentechtorniquetes.databinding.DialogWelcomeBinding
import com.lebentech.lebentechtorniquetes.interfaces.WelcomeDialogListener
import com.lebentech.lebentechtorniquetes.utils.Constants
import java.text.SimpleDateFormat
import java.util.*


class WelcomeDialog(
    private val dialogListener: WelcomeDialogListener,
    private val employeeName: String,
    private val employeeNumber: String,
    private val employeeBirthday: String,
    private val welcomeMessage: String
): DialogFragment() {
    private lateinit var binding: DialogWelcomeBinding

    /**
     * Create the window dialog and set its background to Transparent color
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(requireActivity())

        binding = DialogWelcomeBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    /**
     * Set the employee name and number in the specific view, the post delayed allows the animations
     * to be presented for a moment
     * Then it calls the dialog listener on appear to display a darker background on the main view
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        binding.tvTitle.text = welcomeMessage
        binding.tvName.text = employeeName
        binding.tvEmployeeNumber.text = employeeNumber
        isUserBirthday()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog?.dismiss()
        }, 4000)


        dialogListener.onAppear()
    }

    /**
     * When the dialog is finished by the post delayed it will call the dialog listener on disappear
     * to hide the darker background in the main view
     */
    override fun onStop() {
        super.onStop()

        dialogListener.onDisappear()
    }

    /**
     * Verify the user birthday, if it fit with the actual date it will change the font to display
     * a happy birthday message, otherwise it will set the default font and show a simple animation
     */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun isUserBirthday() {
        val dateFormatted = SimpleDateFormat("dd/MM/yyyy").format(Date())
        val birthdayAnimations = Constants.ANIMATIONS_VALUE
        val randNumber = (1 until birthdayAnimations.size).shuffled().last()

        if ( dateFormatted.equals(employeeBirthday) ) {
            val birthdayFont = resources.getFont(R.font.lb)

            binding.tvTitle.typeface = birthdayFont
            binding.tvName.typeface = birthdayFont
            binding.tvEmployeeNumber.typeface = birthdayFont
            binding.tvBirthday.typeface = birthdayFont
            binding.tvBirthday.visibility = View.VISIBLE

            binding.ivAnimation.setAnimation(birthdayAnimations[randNumber])
            binding.ivAnimation.repeatCount = LottieDrawable.INFINITE
        } else {
            binding.ivAnimation.setAnimation(R.raw.user)
        }
        binding.ivAnimation.speed = 0.75F
        binding.ivAnimation.playAnimation()
    }
}