package com.toochi.job_swift.common.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.DialogDatePickerBinding


class DatePickerDialog(
    context: Context,
    private val onDateSelected: (String) -> Unit
) : Dialog(context) {

    private val binding = DialogDatePickerBinding.inflate(layoutInflater)

    init {
        window?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes?.windowAnimations = R.style.DialogAnimation
            setGravity(Gravity.BOTTOM)
        }
        show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.datePicker.let { datePicker ->
            binding.saveButton.setOnClickListener {
                val selectedDate =
                    "${datePicker.year}-${datePicker.month + 1}-${datePicker.dayOfMonth}"

                onDateSelected.invoke(selectedDate)
                dismiss()
            }
        }

    }

}