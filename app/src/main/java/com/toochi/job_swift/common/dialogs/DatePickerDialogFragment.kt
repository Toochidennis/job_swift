package com.toochi.job_swift.common.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.FragmentDatePickerDialogBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DatePickerDialogFragment(
    context: Context,
    private val onSelected: (String) -> Unit
) : Dialog(context) {

    private val binding = FragmentDatePickerDialogBinding.inflate(layoutInflater)

    init {
        show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes?.windowAnimations = R.style.DialogAnimation
            setGravity(Gravity.BOTTOM)
        }

        setContentView(binding.root)

        populatePickers()
    }


    private fun populatePickers() {
        val yearList = mutableListOf<String>()
        val calendar = Calendar.getInstance().get(Calendar.YEAR)
        val futureYear = calendar + 2

        for (year in 2003..futureYear) {
            yearList.add("$year")
        }

        binding.yearPicker.apply {
            minValue = 0
            maxValue = yearList.size - 1
            wrapSelectorWheel = false
            displayedValues = yearList.toTypedArray()
            value = yearList.indexOf(yearList[yearList.lastIndex - 2])

        }

        val monthList = context.resources.getStringArray(R.array.months).toMutableList()
        val calendar2 = Calendar.getInstance()
        val monthInWords = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar2.time)
        var currentMonthIndex = 0

        monthList.forEachIndexed { index, month ->
            if (month == monthInWords) {
                currentMonthIndex = index
            }
        }


        binding.monthPicker.apply {
            minValue = 0
            maxValue = monthList.size - 1
            wrapSelectorWheel = false
            displayedValues = monthList.toTypedArray()
            value = monthList.indexOf(monthList[currentMonthIndex])
        }

        binding.confirmButton.setOnClickListener {
            val month = monthList[binding.monthPicker.value]
            val year = yearList[binding.yearPicker.value]

            onSelected.invoke("$month $year")

            dismiss()
        }
    }

}