package com.toochi.job_swift.common.fragments

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.checkIfOwnerOfJob
import com.toochi.job_swift.backend.AuthenticationManager.getCompany
import com.toochi.job_swift.common.dialogs.ApplyJobDialogFragment
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentPostedJobDetailsDialogBinding
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.util.Utils.currencyFormatter
import java.util.Locale


class PostedJobDetailsDialogFragment(private val postJob: PostJob) : DialogFragment() {

    private var _binding: FragmentPostedJobDetailsDialogBinding? = null

    private val binding get() = _binding!!

    private var aboutCompany: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPostedJobDetailsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        getCompanyDetails()

        binding.applyForJobButton.setOnClickListener {
            isOwnerOfJob()
        }

        refreshData()
    }

    private fun fillFieldsWithData() {
        binding.jobTitleTxt.text = postJob.title
        binding.locationTxt.text = postJob.location
        binding.jobTypeTxt.text = postJob.jobType
        binding.workplaceTypeTxt.text = postJob.workplaceType

        val daysAgoString = "Posted ${postJob.calculateDaysAgo()}"
        binding.timeTxt.text = daysAgoString

        val formatAmount = String.format(
            Locale.getDefault(),
            "%s%s",
            requireActivity().getString(R.string.naira),
            currencyFormatter(postJob.salary.toDouble())
        )

        val salary = when (postJob.salaryRate) {
            "Per month" -> "$formatAmount/m"
            else -> "$formatAmount/yr"
        }

        binding.salaryTxt.text = salary

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.aboutCompanyTxt.text =
                Html.fromHtml(aboutCompany ?: "", Html.FROM_HTML_MODE_COMPACT)
            binding.requirementsTxt.text =
                Html.fromHtml(postJob.description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            binding.aboutCompanyTxt.text = Html.fromHtml(aboutCompany ?: "")
            @Suppress("DEPRECATION")
            binding.requirementsTxt.text = Html.fromHtml(postJob.description)
        }
    }


    private fun getCompanyDetails() {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()

        getCompany(postJob.userId) { company, errorMessage ->
            if (company != null) {
                aboutCompany = company.description
                binding.companyNameTxt.text = company.title
            } else {
                showToast(errorMessage.toString())
            }

            fillFieldsWithData()

            loadingDialog.dismiss()
        }
    }

    private fun refreshData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getCompanyDetails()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun isOwnerOfJob() {
        checkIfOwnerOfJob(postJob.jobId) { isOwner, exception ->
            if (isOwner) {
                showToast("You can't apply for your own job.")
            } else if (exception == null) {
                ApplyJobDialogFragment(postJob).show(parentFragmentManager, "Apply for job")
            } else {
                showToast(exception.toString())
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

}