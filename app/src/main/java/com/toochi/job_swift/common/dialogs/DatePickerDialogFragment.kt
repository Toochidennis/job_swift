package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toochi.job_swift.R

class DatePickerDialogFragment:BottomSheetDialogFragment() {

 //   private var _binding:FragmentDate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         return View.inflate(requireContext(),R.layout.fragment_date_picker_dialog, container)
    }
}