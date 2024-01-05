package com.toochi.job_swift.user.fragment

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
import com.toochi.job_swift.backend.CompanyManager.getCompany
import com.toochi.job_swift.backend.CompanyManager.updateCompany
import com.toochi.job_swift.backend.PersonalDetailsManager.updateExistingUser
import com.toochi.job_swift.common.dialogs.AlertDialog
import com.toochi.job_swift.common.dialogs.CompanyPositionDialogFragment
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.dialogs.NumberOfEmployeesDialogFragment
import com.toochi.job_swift.databinding.FragmentCompanyInfoDialogBinding
import com.toochi.job_swift.model.Company
import com.toochi.job_swift.util.Constants.Companion.EMPLOYEE
import com.toochi.job_swift.util.Constants.Companion.EMPLOYER
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME

class CompanyInfoDialogFragment : DialogFragment() {

    private var _binding: FragmentCompanyInfoDialogBinding? = null

    private val binding get() = _binding!!

    private var company: Company? = null
    private lateinit var loadingDialog: LoadingDialog
    private var isFirstTime = false
    private var description: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCompanyInfoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()

        handleViewClick()
    }


    private fun initData() {
        loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()

        try {

            getCompany { company, error ->
                if (company != null) {
                    loadingDialog.dismiss()

                    this.company = company

                    binding.companyNameEditText.setText(company.title)
                    binding.numberOfEmployeesEditText.setText(company.noOfEmployees)
                    setDescriptionText(company.description)
                    binding.locationTextField.setText(company.location)
                    binding.positionTextField.setText(company.position)
                    binding.regNoTextField.setText(company.regNumber)
                } else {
                    loadingDialog.dismiss()
                    showToast(error.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }


    private fun handleViewClick() {
        binding.numberOfEmployeesEditText.setOnClickListener {
            NumberOfEmployeesDialogFragment { value ->
                binding.numberOfEmployeesEditText.setText(value)
            }.show(parentFragmentManager, EMPLOYEE)
        }

        binding.positionTextField.setOnClickListener {
            CompanyPositionDialogFragment { position ->
                binding.positionTextField.setText(position)
            }.show(parentFragmentManager, EMPLOYER)
        }

        binding.descriptionLayout.setOnClickListener {
            DescriptionDialogFragment(
                { des ->
                    setDescriptionText(des)
                }, description
            ).show(parentFragmentManager, EMPLOYER)
        }

        binding.saveButton.setOnClickListener {
            processForm()
        }

        binding.navigateUp.setOnClickListener {
            dismiss()
        }
    }


    private fun processForm() {
        loadingDialog = LoadingDialog(requireContext())
        val data = getDataFromForm()

        try {
            if (isValidForm(data)) {
                loadingDialog.show()

                if (company == null) {
                    isFirstTime = true
                    createCompany(data) { success, error ->
                        updateSharedPreference()
                        handleAuthResult(success, error.toString())
                    }
                } else {
                    isFirstTime = false
                    company?.companyId?.let {
                        updateCompany(
                            companyId = it,
                            hashMapOf(
                                "companyId" to it,
                                "title" to data.title,
                                "position" to data.position,
                                "noOfEmployees" to data.noOfEmployees,
                                "description" to data.description,
                                "location" to data.location,
                                "regNumber" to data.regNumber
                            )
                        ) { success, error ->
                            updateSharedPreference()
                            handleAuthResult(success, error.toString())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }

    private fun handleAuthResult(success: Boolean, error: String) {
        if (success) {
            loadingDialog.dismiss()
            showCongratulationsMessage()
        } else {
            loadingDialog.dismiss()
            showToast(error)
        }
    }

    private fun setDescriptionText(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.descriptionTextView.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            binding.descriptionTextView.text = Html.fromHtml(text)
        }

        description = text
    }

    private fun showCongratulationsMessage() {
        if (isFirstTime) {
            AlertDialog.Builder(requireContext()).run {
                title = getString(R.string.welcome_to_swift_job)
                body = getString(R.string.employer_body)
                isNegativeVisible = false
                isPositiveVisible = true
                positiveMessage = getString(R.string.great)
                positiveClickListener = {
                    dismiss()
                }
                build().show()
            }

        } else {
            dismiss()
            showToast(getString(R.string.saved_successfully))
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

            else -> true
        }
    }

    private fun updateSharedPreference() {
        val sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        sharedPreferences.edit().putString("userType", EMPLOYER).apply()
        val profileId = sharedPreferences.getString("profile_id", "")

        profileId?.let {
            updateExistingUser(it, hashMapOf("userType" to EMPLOYER)) { _, _ -> }
        }
    }

    private fun getDataFromForm(): Company {
        return Company(
            title = binding.companyNameEditText.text.toString().trim(),
            position = binding.positionTextField.text.toString().trim(),
            noOfEmployees = binding.numberOfEmployeesEditText.text.toString().trim(),
            description = description,
            location = binding.locationTextField.text.toString().trim(),
            regNumber = binding.regNoTextField.text.toString().trim()
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