package com.toochi.job_swift.common.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.CompanyManager.getCompany
import com.toochi.job_swift.common.dialogs.JobTitleDialogFragment
import com.toochi.job_swift.common.dialogs.JobTypeDialogFragment
import com.toochi.job_swift.common.dialogs.WorkplaceDialogFragment
import com.toochi.job_swift.databinding.FragmentAddJobBasicsDialogBinding
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Constants.Companion.USER_ID_KEY


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddJobBasicsDialogFragment : DialogFragment() {

    private var _binding: FragmentAddJobBasicsDialogBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    private var userId: String? = null
    private var email: String? = null
    private var companyPhoto: String? = null

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

        val sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        with(sharedPreferences) {
            userId = getString(USER_ID_KEY, "")
            email = getString("email", "")
            companyPhoto = sharedPreferences.getString("photo_url", "")

            getCompanyDetails()
        }

        handleViewClicks()
    }

    private fun handleViewClicks() {
        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.jobTitleButton.setOnClickListener {
            JobTitleDialogFragment {
                binding.jobTitleTxt.text = it
            }.show(parentFragmentManager, "title")
        }

        binding.workTypeButton.setOnClickListener {
            WorkplaceDialogFragment {
                binding.workTitleTxt.text = it
            }.show(parentFragmentManager, "work place")
        }


        binding.jobTypeButton.setOnClickListener {
            JobTypeDialogFragment {
                binding.jobTypeTxt.text = it
            }.show(parentFragmentManager, "type")
        }

        binding.continueButton.setOnClickListener {
            processForm()
        }

    }

    private fun getCompanyDetails() {
        try {
            getCompany { company, _ ->
                if (company != null) {
                    binding.companyTitleTxt.text = company.title
                    binding.locationTitleTxt.text = company.location
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isValidForm(job: PostJob): Boolean {
        return when {
            job.title.isEmpty() -> {
                showToast(getString(R.string.please_provide_job_title))
                false
            }

            job.workplaceType.isEmpty() -> {
                showToast(getString(R.string.please_select_workplace_type))
                false
            }

            job.jobType.isEmpty() -> {
                showToast(getString(R.string.please_select_job_type))
                false
            }

            else -> true
        }
    }

    private fun processForm() {
        val job = getDataFromForm()

        if (isValidForm(job)) {
            JobDescriptionDialogFragment(job).show(parentFragmentManager, "job description")
            dismiss()
        }
    }


    private fun getDataFromForm(): PostJob {
        return PostJob(
            userId = userId ?: "",
            title = binding.jobTitleTxt.text.toString(),
            company = binding.companyTitleTxt.text.toString(),
            companyPhotoUrl = companyPhoto ?: "",
            location = binding.locationTitleTxt.text.toString(),
            workplaceType = binding.workTitleTxt.text.toString(),
            jobEmail = email ?: "",
            jobType = binding.jobTypeTxt.text.toString()
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}