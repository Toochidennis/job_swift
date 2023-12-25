package com.toochi.job_swift.common.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.common.dialogs.JobLocationDialogFragment
import com.toochi.job_swift.common.dialogs.JobTitleDialogFragment
import com.toochi.job_swift.common.dialogs.JobTypeDialogFragment
import com.toochi.job_swift.common.dialogs.WorkplaceDialogFragment
import com.toochi.job_swift.databinding.FragmentAddJobBasicsDialogBinding
import com.toochi.job_swift.model.Job


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddJobBasicsDialogFragment : DialogFragment() {

    private var _binding: FragmentAddJobBasicsDialogBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    private var jobTitle: String? = null
    private var company: String? = null
    private var workplaceType: String? = null
    private var jobLocation: String? = null
    private var jobType: String? = null
    private var userId: String? = null

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
        _binding = FragmentAddJobBasicsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("loginDetail", MODE_PRIVATE)
        with(sharedPreferences) {
            company = getString("company_name", "")
            jobLocation = getString("company_location", "")
            userId = getString("user_id", "")
        }

        handleViewClicks()

        processForm()

    }

    private fun handleViewClicks() {
        binding.companyTitleTxt.text = company
        binding.locationTitleTxt.text = jobLocation

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.jobTitleButton.setOnClickListener {
            JobTitleDialogFragment {
                jobTitle = it
                binding.jobTitleTxt.text = it
            }.show(parentFragmentManager, "title")
        }

        binding.workTypeButton.setOnClickListener {
            WorkplaceDialogFragment {
                workplaceType = it
                binding.workTitleTxt.text = it
            }.show(parentFragmentManager, "work place")
        }


        binding.jobTypeButton.setOnClickListener {
            JobTypeDialogFragment {
                jobType = it
                binding.jobTypeTxt.text = it
            }.show(parentFragmentManager, "type")
        }
    }

    private fun isValidForm(): Boolean {
        return when {
            jobTitle.isNullOrEmpty() -> {
                showToast("Please select job title")
                false
            }

            workplaceType.isNullOrEmpty() -> {
                showToast("Please select workplace type")
                false
            }

            jobType.isNullOrEmpty() -> {
                showToast("Please select job type")
                false
            }

            else -> true
        }
    }

    private fun processForm() {
        binding.continueButton.setOnClickListener {
            if (isValidForm()) {
                val job = Job(
                    userId = userId ?: "",
                    title = jobTitle ?: "",
                    company = company ?: "",
                    location = jobLocation ?: "",
                    workplaceType = workplaceType ?: "",
                    jobType = jobType ?: ""
                )

                JobDescriptionDialogFragment(job).show(parentFragmentManager, "job description")
                dismiss()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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