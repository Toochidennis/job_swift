package com.toochi.job_swift.common.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.common.dialogs.JobLocationDialogFragment
import com.toochi.job_swift.common.dialogs.JobTitleDialogFragment
import com.toochi.job_swift.common.dialogs.JobTypeDialogFragment
import com.toochi.job_swift.common.dialogs.WorkplaceDialogFragment
import com.toochi.job_swift.databinding.FragmentAddJobBasicsDialogBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddJobBasicsDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentAddJobBasicsDialogBinding

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog2)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddJobBasicsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.continueButton.setOnClickListener {
            JobDescriptionDialogFragment().show(parentFragmentManager, "job description")
        }

        binding.jobTitleButton.setOnClickListener {
            JobTitleDialogFragment {
                binding.jobTitleTxt.text = it
            }.show(parentFragmentManager, "title")
        }

        binding.workTypeButton.setOnClickListener {
            WorkplaceDialogFragment().show(parentFragmentManager, "work place")
        }

        binding.jobLocationButton.setOnClickListener {
            JobLocationDialogFragment().show(parentFragmentManager, "location")
        }

        binding.jobTypeButton.setOnClickListener {
            JobTypeDialogFragment().show(parentFragmentManager, "type")
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddJobBasicsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}