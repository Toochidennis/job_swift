package com.toochi.job_swift.common.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.CompanyManager.createCompany
import com.toochi.job_swift.backend.PersonalDetailsManager.updateExistingUser
import com.toochi.job_swift.common.dialogs.AlertDialog
import com.toochi.job_swift.common.dialogs.CompanyPositionDialogFragment
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.dialogs.NumberOfEmployeesDialogFragment
import com.toochi.job_swift.databinding.FragmentCreateEmployerAccountDialogBinding
import com.toochi.job_swift.model.Company
import com.toochi.job_swift.user.fragment.DescriptionDialogFragment
import com.toochi.job_swift.util.Constants.Companion.EMPLOYEE
import com.toochi.job_swift.util.Constants.Companion.EMPLOYER
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME


class CreateEmployerAccountDialogFragment : DialogFragment() {

    private var _binding: FragmentCreateEmployerAccountDialogBinding? = null
    private val binding get() = _binding!!

    private var description: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog2)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCreateEmployerAccountDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewClickListener()
    }

    private fun processForm() {
        val loadingDialog = LoadingDialog(requireContext())
        try {
            val company = getDataFromForm()

            if (isValidForm(company)) {
                loadingDialog.show()

                createCompany(company) { success, errorMessage ->
                    if (success) {
                        updateSharedPreference(company)
                        loadingDialog.dismiss()
                        showWelcomeMessage()
                    } else {
                        loadingDialog.dismiss()
                        showToast(errorMessage.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }


    private fun updateSharedPreference(company: Company) {
        val sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("user_type", EMPLOYER)
            putString("company_name", company.title)
            putString("company_location", company.location)
            apply()
        }

        val profileId = sharedPreferences.getString("profile_id", "")

        profileId?.let {
            updateExistingUser(it, hashMapOf("userType" to EMPLOYER)) { _, _ -> }
        }
    }

    private fun showWelcomeMessage() {
        AlertDialog.Builder(requireContext()).run {
            title = getString(R.string.welcome_to_swift_job)
            body = getString(R.string.employer_body)
            isNegativeVisible = false
            isPositiveVisible = true
            positiveMessage = getString(R.string.continue_)
            positiveClickListener = {
                AddJobBasicsDialogFragment().show(parentFragmentManager, "job basics")
                dismiss()
            }
            build()
        }.show()
    }

    private fun setDescriptionText(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.descriptionTxt.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            binding.descriptionTxt.text = Html.fromHtml(text)
        }

        description = text
    }

    private fun viewClickListener() {
        binding.numberOfEmployeesEditText.setOnClickListener {
            NumberOfEmployeesDialogFragment { selectedText ->
                binding.numberOfEmployeesEditText.setText(selectedText)
            }.show(parentFragmentManager, EMPLOYEE)
        }

        binding.descriptionButton.setOnClickListener {
            DescriptionDialogFragment({ descriptionText ->
                setDescriptionText(descriptionText)
            }, description).show(parentFragmentManager, "description")
        }

        binding.positionEditText.setOnClickListener {
            CompanyPositionDialogFragment {
                binding.positionEditText.setText(it)
            }.show(parentFragmentManager, "position")
        }

        binding.continueButton.setOnClickListener {
            processForm()
        }


        binding.navigateUp.setOnClickListener {
            dismiss()
        }

    }

    private fun isValidForm(company: Company): Boolean {
        return when {
            company.title.isEmpty() -> {
                showToast(getString(R.string.please_provide_company_name))
                false
            }

            company.description.isEmpty() -> {
                showToast(getString(R.string.please_provide_description))
                false
            }

            company.regNumber.isEmpty() -> {
                showToast(getString(R.string.please_provide_cac_id))
                false
            }

            company.location.isEmpty() -> {
                showToast(getString(R.string.please_provide_location))
                false
            }

            else -> true
        }

    }

    private fun getDataFromForm(): Company {
        return Company(
            title = binding.companyNameEditText.text.toString().trim(),
            position = binding.positionEditText.text.toString().trim(),
            noOfEmployees = binding.numberOfEmployeesEditText.text.toString().trim(),
            description = description,
            location = binding.locationEditText.text.toString().trim(),
            regNumber = binding.cacIdEditText.text.toString().trim()
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
