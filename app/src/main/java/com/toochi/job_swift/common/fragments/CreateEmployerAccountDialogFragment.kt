package com.toochi.job_swift.common.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.createCompany
import com.toochi.job_swift.common.dialogs.CompanyPositionDialogFragment
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.dialogs.NumberOfEmployeesDialogFragment
import com.toochi.job_swift.databinding.FragmentCreateEmployerAccountDialogBinding
import com.toochi.job_swift.model.Company
import com.toochi.job_swift.user.fragment.DescriptionDialogFragment


class CreateEmployerAccountDialogFragment : DialogFragment() {

    private var _binding: FragmentCreateEmployerAccountDialogBinding? = null

    private val binding get() = _binding!!

    private var companyName: String? = null
    private var noOfEmployees: String? = null
    private var description: String? = null
    private var regNo: String? = null
    private var location: String? = null
    private var position: String? = null

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

        processFormData()

    }


    private fun processFormData() {
        val loadingDialog = LoadingDialog(requireContext())

        binding.continueButton.setOnClickListener {
            companyName = binding.companyNameEditText.text.toString().trim()
            noOfEmployees = binding.numberOfEmployeesEditText.text.toString()
            regNo = binding.cacIdEditText.text.toString().trim()
            position = binding.positionEditText.text.toString()
            location = binding.locationEditText.text.toString()

            if (isValidForm()) {
                val company = Company(
                    "",
                    companyName ?: "",
                    position ?: "",
                    noOfEmployees ?: "",
                    description ?: "",
                    location ?: "",
                    regNo ?: ""
                )

                loadingDialog.show()

                createCompany(company) { success, errorMessage ->
                    if (success) {
                        requireActivity().getSharedPreferences("loginDetail", MODE_PRIVATE)
                            .edit().putBoolean("haveCompany", true).apply()

                        AddJobBasicsDialogFragment().show(parentFragmentManager, "job basics")
                        dismiss()
                    } else {
                        showToast(errorMessage.toString())
                    }

                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun viewClickListener() {
        binding.numberOfEmployeesEditText.setOnClickListener {
            NumberOfEmployeesDialogFragment { selectedText ->
                binding.numberOfEmployeesEditText.setText(selectedText)
            }.show(parentFragmentManager, "Number of employees")
        }

        binding.descriptionButton.setOnClickListener {
            DescriptionDialogFragment({ descriptionText ->
                description = descriptionText
                binding.descriptionTxt.text =
                    Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)
            }, description.toString()).show(parentFragmentManager, "description")
        }

        binding.positionEditText.setOnClickListener {
            CompanyPositionDialogFragment {
                binding.positionEditText.setText(it)
            }.show(parentFragmentManager, "position")
        }

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

    }

    private fun isValidForm(): Boolean {
        return when {
            companyName.isNullOrEmpty() -> {
                showToast("Please provide company name")
                false
            }

            description.isNullOrEmpty() -> {
                showToast("Please provide description")
                false
            }

            regNo.isNullOrEmpty() -> {
                showToast("Please provide CAC ID no")
                false
            }

            position.isNullOrEmpty() -> {
                showToast("Please provide position")
                false
            }

            location.isNullOrEmpty() -> {
                showToast("Please provide location")
                false
            }

            else -> true
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
