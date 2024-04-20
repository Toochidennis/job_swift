package com.toochi.job_swift.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.ExperienceManager.createUserExperience
import com.toochi.job_swift.backend.ExperienceManager.updateUserExperience
import com.toochi.job_swift.common.dialogs.DatePickerDialogFragment
import com.toochi.job_swift.common.dialogs.JobTypeDialogFragment
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.dialogs.WorkplaceDialogFragment
import com.toochi.job_swift.databinding.FragmentUserExperienceDialogBinding
import com.toochi.job_swift.model.Experience
import com.toochi.job_swift.util.Constants.Companion.PRESENT


class UserExperienceDialogFragment(
    private val experienceModel: Experience? = null,
    private val onSave: () -> Unit
) : DialogFragment() {

    private var _binding: FragmentUserExperienceDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserExperienceDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleViewClicks()

        fillForm()
    }

    private fun fillForm() {
        if (experienceModel != null) {
            binding.jobTitleTextField.setText(experienceModel.jobTitle)
            binding.employeeTypeTextField.setText(experienceModel.jobType)
            binding.companyNameTextField.setText(experienceModel.companyName)
            binding.locationTextField.setText(experienceModel.location)
            binding.locationTypeTextField.setText(experienceModel.workplace)
            binding.startDateTextField.setText(experienceModel.startDate)

            if (experienceModel.endDate == PRESENT) {
                binding.statusCheckBox.isChecked = true
                binding.endDateTextField.setText(PRESENT)
            } else {
                binding.endDateTextField.setText(experienceModel.endDate)
            }
        }
    }

    private fun handleViewClicks() {
        binding.employeeTypeTextField.setOnClickListener {
            JobTypeDialogFragment { type ->
                binding.employeeTypeTextField.setText(type)
            }.show(parentFragmentManager, "job type")
        }

        binding.locationTypeTextField.setOnClickListener {
            WorkplaceDialogFragment { workplace ->
                binding.locationTypeTextField.setText(workplace)
            }.show(parentFragmentManager, "location type")
        }

        binding.startDateTextField.setOnClickListener {
            DatePickerDialogFragment(requireContext()) { date ->
                binding.startDateTextField.setText(date)
            }.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        binding.endDateTextField.setOnClickListener {
            DatePickerDialogFragment(requireContext()) { date ->
                binding.endDateTextField.setText(date)
            }.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }


        binding.statusCheckBox.setOnClickListener {
            if (binding.statusCheckBox.isChecked) {
                binding.endDateTextField.isEnabled = false
                binding.endDateTextField.setText(PRESENT)
            } else {
                binding.endDateTextField.isEnabled = true
                binding.endDateTextField.setText("")
            }
        }

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            addExperience()
        }
    }

    private fun isValidForm(experience: Experience): Boolean {
        return when {
            experience.jobType.isEmpty() -> {
                showToast(getString(R.string.please_provide_job_title))
                false
            }

            experience.companyName.isEmpty() -> {
                showToast(getString(R.string.please_provide_company_name))
                false
            }

            experience.startDate.isEmpty() -> {
                showToast(getString(R.string.please_provide_start_date))
                false
            }

            experience.endDate.isEmpty() -> {
                showToast(getString(R.string.please_provide_end_date))
                false
            }

            else -> true
        }
    }

    private fun addExperience() {
        val loadingDialog = LoadingDialog(requireContext())

        try {
            val experience = getDataFromForm()

            if (isValidForm(experience)) {
                loadingDialog.show()

                if (experienceModel != null) {
                    loadingDialog.dismiss()
                    updateUserExperience(
                        experienceId = experienceModel.experienceId,
                        data = hashMapOf(
                            "jobTitle" to experience.jobTitle,
                            "jobType" to experience.jobType,
                            "companyName" to experience.companyName,
                            "location" to experience.location,
                            "workplace" to experience.workplace,
                            "startDate" to experience.startDate,
                            "endDate" to experience.endDate
                        )
                    ) { success, error ->
                        loadingDialog.dismiss()
                        handleAuthResult(success, error.toString())
                    }

                } else {
                    createUserExperience(experience) { success, error ->
                        loadingDialog.dismiss()
                        handleAuthResult(success, error.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }

    private fun getDataFromForm(): Experience {
        return Experience(
            jobTitle = binding.jobTitleTextField.text.toString().trim(),
            jobType = binding.employeeTypeTextField.text.toString().trim(),
            companyName = binding.companyNameTextField.text.toString().trim(),
            location = binding.locationTextField.text.toString().trim(),
            workplace = binding.locationTypeTextField.text.toString().trim(),
            startDate = binding.startDateTextField.text.toString().trim(),
            endDate = binding.endDateTextField.text.toString().trim()
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun handleAuthResult(success: Boolean, error: String) {
        if (success) {
            onSave.invoke()
            dismiss()
            showToast(getString(R.string.saved_successfully))
        } else {
            showToast(error)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}