package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textview.MaterialTextView
import com.toochi.job_swift.databinding.FragmentJobTypeDialogBinding


class JobTypeDialogFragment(
    private val onSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentJobTypeDialogBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentJobTypeDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textView1.setOnClickListenerWithText()
        binding.textView2.setOnClickListenerWithText()
        binding.textView3.setOnClickListenerWithText()
        binding.textView4.setOnClickListenerWithText()
        binding.textView5.setOnClickListenerWithText()
        binding.textView6.setOnClickListenerWithText()
    }

    private fun View.setOnClickListenerWithText() {
        if (this is MaterialTextView) {
            this.setOnClickListener {
                onSelected.invoke(this.text.toString())
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}