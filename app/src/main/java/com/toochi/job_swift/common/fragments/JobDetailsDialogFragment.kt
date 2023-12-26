package com.toochi.job_swift.common.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.getCompanyByUserId
import com.toochi.job_swift.common.dialogs.ApplyJobDialogFragment
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentJobDetailsDialogBinding
import com.toochi.job_swift.model.Job
import com.toochi.job_swift.user.adapters.FragmentAdapter
import com.toochi.job_swift.util.Utils.currencyFormatter


class JobDetailsDialogFragment(private val job: Job) : DialogFragment() {

    private var _binding: FragmentJobDetailsDialogBinding? = null

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
        _binding = FragmentJobDetailsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        getCompanyDetails()

        binding.applyButton.setOnClickListener {
            ApplyJobDialogFragment(job).show(parentFragmentManager, "apply job")
        }
    }

    private fun setUpViewPager() {
        val tabTitles = listOf("Requirements", "About")
        val fragmentAdapter = FragmentAdapter(requireActivity()).apply {
            addFragment(RequirementsFragment.newInstance(job.description))
            addFragment(AboutCompanyFragment.newInstance(aboutCompany ?: ""))
        }

        binding.viewPager.adapter = fragmentAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun fillFieldsWithData() {
        binding.jobTitleTxt.text = job.title
        binding.locationTxt.text = job.location
        binding.jobTypeTxt.text = job.jobType
        binding.workplaceTypeTxt.text = job.workplaceType

        val formatAmount =
            "${requireActivity().getString(R.string.naira)}${currencyFormatter(job.salary.toDouble())}"
        val salary = when (job.salaryRate) {
            "Per month" -> "$formatAmount/m"
            else -> "$formatAmount/yr"
        }

        binding.salaryTxt.text = salary
    }


    private fun getCompanyDetails() {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()

        getCompanyByUserId(job.userId) { companies, errorMessage ->
            if (companies != null) {
                aboutCompany = companies[0].description
                binding.companyNameTxt.text = companies[0].title
            } else {
                showToast(errorMessage.toString())
            }

            fillFieldsWithData()
            setUpViewPager()
            loadingDialog.dismiss()
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