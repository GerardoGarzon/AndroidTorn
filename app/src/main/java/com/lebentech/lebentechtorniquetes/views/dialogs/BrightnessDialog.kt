/**
 * Created by Gerardo Garzon on 23/02/23.
 */

package com.lebentech.lebentechtorniquetes.views.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.lebentech.lebentechtorniquetes.databinding.DialogBrightnessBinding

class BrightnessDialog: DialogFragment() {
    private lateinit var binding: DialogBrightnessBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val builder = AlertDialog.Builder(requireActivity())

        binding = DialogBrightnessBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)
        binding.button.setOnClickListener {
            dismiss()
        }

        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }
}