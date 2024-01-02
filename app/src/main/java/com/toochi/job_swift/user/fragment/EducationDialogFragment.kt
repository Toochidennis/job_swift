package com.toochi.job_swift.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.EducationManager.createUserEducation
import com.toochi.job_swift.backend.EducationManager.updateUserEducation
import com.toochi.job_swift.common.dialogs.DatePickerDialogFragment
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentEducationDialogBinding
import com.toochi.job_swift.model.Education


class EducationDialogFragment(
    private val education: Education? = null,
    private val onSave: () -> Unit
) : DialogFragment() {

    private var _binding: FragmentEducationDialogBinding? = null
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
        _binding = FragmentEducationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleViewClicks()

        processForm()

        fillForm()
    }

    private fun fillForm() {
        if (education != null) {
            binding.schoolNameTextField.setText(education.school)
            binding.degreeTextField.setText(education.degree)
            binding.fieldOfStudyTextField.setText(education.discipline)
            binding.gradeTextField.setText(education.grade)
            binding.startDateTextField.setText(education.startDate)
            binding.endDateTextField.setText(education.endDate)
        }
    }

    private fun handleViewClicks() {
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

        binding.navigateUp.setOnClickListener {
            dismiss()
        }
    }

    private fun processForm() {
        binding.saveButton.setOnClickListener {
            val loadingDialog = LoadingDialog(requireContext())
            val data = getDataFromForm()

            try {
                if (isValidForm(data)) {
                    loadingDialog.show()

                    if (education != null) {
                        updateUserEducation(
                            educationId = education.educationId,
                            data = hashMapOf(
                                "school" to data.school,
                                "degree" to data.degree,
                                "discipline" to data.discipline,
                                "startDate" to data.startDate,
                                "endDate" to data.endDate,
                                "grade" to data.grade
                            )
                        ) { success, error ->
                            handleAuthResult(success, error.toString())
                            loadingDialog.dismiss()
                        }
                    } else {
                        createUserEducation(data) { success, error ->
                            handleAuthResult(success, error.toString())
                            loadingDialog.dismiss()
                        }
                    }
                } else {
                    showToast(getString(R.string.please_provide_school))

                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("An error occurred.")
            }
        }
    }

    private fun handleAuthResult(success: Boolean, error: String) {
        if (success) {
            onSave.invoke()
            dismiss()
        } else {
            showToast(error)
        }
    }

    private fun isValidForm(education: Education): Boolean {
        return education.school.isNotEmpty()
    }

    private fun getDataFromForm(): Education {
        return Education(
            school = binding.schoolNameTextField.text.toString().trim(),
            degree = binding.degreeTextField.text.toString().trim(),
            discipline = binding.fieldOfStudyTextField.text.toString().trim(),
            startDate = binding.startDateTextField.text.toString().trim(),
            endDate = binding.endDateTextField.text.toString().trim(),
            grade = binding.gradeTextField.text.toString().trim(),
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