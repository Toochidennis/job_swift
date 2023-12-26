package com.toochi.job_swift.common.dialogs

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toochi.job_swift.backend.AuthenticationManager.applyJob
import com.toochi.job_swift.databinding.FragmentApplyJobDialogBinding
import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.model.Job


class ApplyJobDialogFragment(private val job: Job) : BottomSheetDialogFragment() {

    private var _binding: FragmentApplyJobDialogBinding? = null

    private val binding get() = _binding!!

    private var userEmail: String? = null
    private var userName: String? = null
    private var userId: String? = null
    private var userPhoneNumber: String? = null
    private var cvFile: Uri? = null
    private var cvName: String? = null

    private lateinit var pdfLauncher: ActivityResultLauncher<String?>
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                onPickedPdf(uri, getFileName(uri))
            } else {
                onPickedPdf(null, null)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentApplyJobDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        autoFillTextFields()

        uploadCV()

        applyNow()


        binding.navigateUp.setOnClickListener {
            dismiss()
        }
    }

    private fun autoFillTextFields() {
        with(requireActivity().getSharedPreferences("loginDetail", Context.MODE_PRIVATE)) {
            val firstname = getString("firstname", "")
            val lastname = getString("lastname", "")
            userPhoneNumber = getString("phone_number", "")
            userEmail = getString("email", "")
            userId = getString("user_id", "")

            userName = "$firstname $lastname"

            binding.fullNameTextField.setText(userName ?: "")
            binding.phoneNumberTextField.setText(userPhoneNumber ?: "")
            binding.emailTextField.setText(userEmail ?: "")
        }
    }

    private fun uploadCV() {
        binding.uploadLayout.isVisible = job.isProvideCV

        binding.uploadCVButton.setOnClickListener {
            pdfLauncher.launch("application/pdf")
        }

        binding.cvNameTxt.apply {
            setOnClickListener {
                isVisible = false
                text = ""

                cvName = null
                cvFile = null
            }
        }
    }

    private fun onPickedPdf(uri: Uri?, fileName: String?) {
        if (uri != null) {
            loadingDialog.show()

            val fileSizeInMB = uri.toFile().length() / (1024.0 * 1024.0)

            if (fileSizeInMB > 2048.0) {
                loadingDialog.dismiss()
                showToast("File size is larger than 2MB")
            } else {
                binding.cvNameTxt.apply {
                    text = fileName
                    isVisible = true

                    postDelayed({
                        loadingDialog.dismiss()
                    }, 2000)
                }

                cvFile = uri
                cvName = fileName
            }
        }
    }

    private fun isValidForm(): Boolean {
        userName = binding.fullNameTextField.text.toString().trim()
        userEmail = binding.emailTextField.text.toString().trim()
        userPhoneNumber = binding.phoneNumberTextField.text.toString().trim()

        return when {
            userName.isNullOrEmpty() -> {
                showToast("Please provide name")
                false
            }

            userPhoneNumber.isNullOrEmpty() -> {
                showToast("Please provide phone number")
                false
            }

            userEmail.isNullOrEmpty() -> {
                showToast("Please provide email")
                false
            }

            cvName.isNullOrEmpty() -> {
                if (job.isProvideCV) {
                    showToast("Please upload CV")
                    false
                } else {
                    true
                }
            }

            else -> true
        }
    }

    private fun applyNow() {
        binding.applyButton.setOnClickListener {
            if (isValidForm()) {
                loadingDialog.show()

                val details = ApplyJob("", userId ?: "", job.jobId, "Pending")
                applyJob(details, cvFile, cvName) { success, errorMessage ->
                    if (success) {
                        showToast("success")
                        dismiss()
                    } else {
                        showToast(errorMessage.toString())
                    }

                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        requireActivity().contentResolver.query(
            uri, null, null,
            null, null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            return cursor.getString(nameIndex)
        }
        return null
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}