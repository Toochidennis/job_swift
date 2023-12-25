package com.toochi.job_swift.common.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import com.toochi.job_swift.databinding.DialogDatePickerBinding


class DatePickerDialog(
    context: Context,
    private val onDateSelected: (String) -> Unit
) : Dialog(context) {

    private var _binding: DialogDatePickerBinding? = null

    private val binding get() = _binding!!

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogDatePickerBinding.inflate(layoutInflater)
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


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }
}