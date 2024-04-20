package com.toochi.job_swift.common.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.squareup.picasso.Picasso
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.CompanyManager.getCompany
import com.toochi.job_swift.backend.PostJobManager.checkIfOwnerOfJob
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

    private lateinit var loadingDialog: LoadingDialog

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

        loadingDialog = LoadingDialog(requireContext())

        getCompanyDetails()

        handleViewsClick()
    }

    private fun handleViewsClick() {
        binding.mailButton.setOnClickListener {
            sendEmail()
        }

        refreshData()

        binding.applyForJobButton.setOnClickListener {
            isOwnerOfJob()
        }

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

    }

    private fun fillFieldsWithData() {
        binding.jobTitleTxt.text = postJob.title
        binding.locationTxt.text = postJob.location
        binding.jobTypeTxt.text = postJob.jobType
        binding.workplaceTypeTxt.text = postJob.workplaceType

        if (postJob.companyPhotoUrl.isNotEmpty()) {
            Picasso.get().load(postJob.companyPhotoUrl).into(binding.imageView)
        }

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
        loadingDialog.show()

        try {
            getCompany(postJob.userId) { company, errorMessage ->
                if (company != null) {
                    loadingDialog.dismiss()
                    aboutCompany = company.description
                    binding.companyNameTxt.text = company.title
                    fillFieldsWithData()
                    binding.applyForJobButton.isVisible = true
                } else {
                    loadingDialog.dismiss()
                    binding.applyForJobButton.isVisible = false
                    showToast(errorMessage.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }

    private fun refreshData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getCompanyDetails()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun isOwnerOfJob() {
        try {
            loadingDialog.show()
            checkIfOwnerOfJob(postJob.jobId) { isOwner, exception ->
                if (isOwner) {
                    loadingDialog.dismiss()
                    showToast("You can't apply for your own job.")
                } else if (exception == null) {
                    loadingDialog.dismiss()
                    ApplyJobDialogFragment(postJob).show(parentFragmentManager, "Apply for job")
                } else {
                    loadingDialog.dismiss()
                    showToast(exception.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast(e.message.toString())
        }
    }

    private fun sendEmail() {
        try {
            if (postJob.jobEmail.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${postJob.jobEmail}")
                    putExtra(Intent.EXTRA_SUBJECT, "")
                    putExtra(Intent.EXTRA_TEXT, "")
                }

                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(intent)
                } else {
                    startActivity(Intent.createChooser(intent, "Send us a mail"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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