package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toochi.job_swift.databinding.FragmentWorkplaceDialogBinding

class WorkplaceDialogFragment(
    private val onSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentWorkplaceDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentWorkplaceDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListenerWithText()
    }

    private fun setOnClickListenerWithText() {
        binding.remoteButton.setOnClickListener {
            onSelected.invoke("Remote")
            dismiss()
        }

        binding.hybridButton.setOnClickListener {
            onSelected.invoke("Hybrid")
            dismiss()
        }

        binding.onSiteButton.setOnClickListener {
            onSelected.invoke("On-site")
            dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}