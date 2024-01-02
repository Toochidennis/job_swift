package com.toochi.job_swift.common.dialogs

import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.ApplyForJobManager.applyJob
import com.toochi.job_swift.backend.ApplyForJobManager.checkIfHaveAppliedJob
import com.toochi.job_swift.backend.PersonalDetailsManager.getUserToken
import com.toochi.job_swift.databinding.FragmentApplyJobDialogBinding
import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.util.Constants.Companion.FILE_TYPE
import com.toochi.job_swift.util.Constants.Companion.JOB_APPLICATION
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Constants.Companion.USER_ID_KEY
import com.toochi.job_swift.util.Utils.sendNotification
import java.io.IOException
import java.io.InputStream


class ApplyJobDialogFragment(private val postJob: PostJob) : BottomSheetDialogFragment() {

    private var _binding: FragmentApplyJobDialogBinding? = null

    private val binding get() = _binding!!

    private var userEmail: String? = null
    private var profileId: String? = null
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

        initializeUI()
    }

    private fun initializeUI() {
        autoFillTextFields()
        uploadCV()

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.applyForJobButton.setOnClickListener {
            checkIfAppliedJobBefore()
        }
    }

    private fun autoFillTextFields() {
        with(requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)) {
            val firstname = getString("firstname", "")
            val lastname = getString("lastname", "")
            userPhoneNumber = getString("phone_number", "")
            userEmail = getString("email", "")
            userId = getString(USER_ID_KEY, "")
            profileId = getString("profile_id", "")

            userName = "$firstname $lastname"

            binding.fullNameTextField.setText(userName ?: "")
            binding.phoneNumberTextField.setText(userPhoneNumber ?: "")
            binding.emailTextField.setText(userEmail ?: "")
        }
    }

    private fun uploadCV() {
        binding.uploadLayout.isVisible = postJob.isProvideCV

        binding.uploadCVButton.setOnClickListener {
            pdfLauncher.launch(FILE_TYPE)
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
        try {
            if (uri != null) {
                loadingDialog.show()

                val fileSizeInBytes = getFileSize(uri)
                val fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0)

                if (fileSizeInMB > 2048.0) {
                    loadingDialog.dismiss()
                    showToast(getString(R.string.file_size_is_larger_than_2mb))
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
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(e.message.toString())
        }
    }

    private fun getFileSize(uri: Uri): Long {
        var fileSize: Long = 0

        try {
            val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(uri)
            fileSize = inputStream?.available()?.toLong() ?: 0

            // Close the input stream after obtaining the file size
            inputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
            showToast("An error occurred.")
        }

        return fileSize
    }


    private fun isValidForm(): Boolean {
        userName = binding.fullNameTextField.text.toString().trim()
        userEmail = binding.emailTextField.text.toString().trim()
        userPhoneNumber = binding.phoneNumberTextField.text.toString().trim()

        return when {
            userName.isNullOrEmpty() -> {
                showToast(getString(R.string.please_provide_name))
                false
            }

            userPhoneNumber.isNullOrEmpty() -> {
                showToast(getString(R.string.please_provide_phone_number))
                false
            }

            userEmail.isNullOrEmpty() -> {
                showToast(getString(R.string.please_provide_email))
                false
            }

            cvName.isNullOrEmpty() -> {
                if (postJob.isProvideCV) {
                    showToast(getString(R.string.please_upload_cv))
                    false
                } else {
                    true
                }
            }

            else -> true
        }
    }

    private fun applyNow() {
        try {
            val job = getDataFromForm()

            if (isValidForm()) {
                applyJob("", job, cvFile, cvName ?: "") { success, errorMessage ->
                    if (success) {
                        applyJob(postJob.userId, job, cvFile, cvName ?: "") { isFinished, error ->
                            if (isFinished) {
                                notifyOwner()
                            } else {
                                showToast(error.toString())
                                loadingDialog.dismiss()
                            }
                        }
                    } else {
                        showToast(errorMessage.toString())
                        loadingDialog.dismiss()
                    }
                }
            } else {
                loadingDialog.dismiss()
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }

    private fun checkIfAppliedJobBefore() {
        try {
            loadingDialog.show()

            checkIfHaveAppliedJob(postJob.jobId) { haveApplied, exception ->
                if (haveApplied) {
                    showToast(getString(R.string.you_have_applied_for_this_job))
                } else if (exception == null) {
                    applyNow()
                } else {
                    loadingDialog.dismiss()
                    showToast(exception.toString())
                }
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }

    private fun notifyOwner() {
        try {
            getUserToken(postJob.userId) { token, _ ->
                if (token != null) {
                    Notification(
                        token = token,
                        title = "New Job Application",
                        body = "You have a new job application for the position of ${postJob.title}",
                        userId = "$userId",
                        employerId = postJob.userId,
                        jobId = postJob.jobId,
                        type = JOB_APPLICATION
                    ).also {
                        sendNotification(requireActivity(), it) { _ ->
                            loadingDialog.dismiss()

                            showCongratsMessage()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCongratsMessage() {
        AlertDialog.Builder(requireContext()).run {
            title = "Awesome!"
            body = "Your application was submitted successfully."
            isPositiveVisible = true
            isNegativeVisible = false
            positiveMessage = "Thank you"
            positiveClickListener = {
                dismiss()
            }
            build()
        }.show()
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

    private fun getDataFromForm(): ApplyJob {
        return ApplyJob(
            userId = userId ?: "",
            employerId = postJob.userId,
            jobTitle = postJob.title,
            company = postJob.company,
            jobId = postJob.jobId
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

/**
 * val myImageView: ImageView = findViewById(R.id.myImageView)
 *
 * // Set a click listener for the ImageView
 * myImageView.setOnClickListener {
 *     // Get the drawable from the ImageView
 *     val drawable = myImageView.drawable
 *
 *     // Set the inner color when clicked
 *     drawable?.colorFilter = PorterDuffColorFilter(
 *         ContextCompat.getColor(this, R.color.your_clicked_color),
 *         PorterDuff.Mode.SRC_IN
 *     )
 *
 *     // Redraw the ImageView to apply the color filter
 *     myImageView.invalidate()
 * }
 * */