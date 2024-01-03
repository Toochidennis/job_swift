package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager
import com.toochi.job_swift.backend.PersonalDetailsManager.getUserToken
import com.toochi.job_swift.backend.PostJobManager.deleteUserPost
import com.toochi.job_swift.databinding.FragmentDeleteJobDialogBinding
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.util.Constants.Companion.DELETE_JOB
import com.toochi.job_swift.util.Constants.Companion.NOT_AVAILABLE
import com.toochi.job_swift.util.Utils.sendNotification

class DeleteJobDialogFragment(private val postJob: PostJob) : DialogFragment() {


    private var _binding: FragmentDeleteJobDialogBinding? = null

    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteJobDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.deleteJobButton.setOnClickListener {
            processForm()
        }
    }

    private fun processForm() {
        val reportText = binding.deleteTextField.text.toString().trim()

        if (reportText.isEmpty()) {
            binding.deleteTextField.error = "Provide reason"
        } else {
            binding.deleteTextField.error = null
            confirmDeletion(reportText)
        }
    }

    private fun confirmDeletion(report: String) {
        AlertDialog.Builder(requireContext()).run {
            title = "Delete Job"
            body = "Are you sure you want to delete this job?"
            isPositiveVisible = true
            isNegativeVisible = true
            negativeMessage = "Cancel"
            positiveMessage = "Delete"
            positiveClickListener = {
                deleteJob(report)
            }
            negativeClickListener?.invoke()

            build().show()
        }
    }

    private fun deleteJob(report: String) {
        loadingDialog.show()
        try {
            deleteUserPost(postJob.userId, postJob.jobId) { deleted, exception ->
                if (deleted) {
                    sendReport(report)
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

    private fun sendReport(report: String) {
        try {
            getUserToken(postJob.userId) { token, exception ->
                if (token != null) {
                    val userId = AuthenticationManager.auth.currentUser?.uid

                    if (userId != null) {
                        Notification(
                            token = token,
                            title = "Job Deletion",
                            body = "Your job has been deleted. Please contact admin for more info.",
                            userId = userId,
                            employerId = postJob.userId,
                            jobId = postJob.jobId,
                            type = DELETE_JOB,
                            comments = report,
                        ).also {
                            sendNotification(requireActivity(), it) { _ ->
                                loadingDialog.dismiss()
                                dismiss()
                                showToast("Job deleted successfully")
                            }
                        }
                    }
                } else if (exception == NOT_AVAILABLE) {
                    loadingDialog.dismiss()
                    showToast("User not available")
                } else {
                    loadingDialog.dismiss()
                    showToast(exception.toString())
                }
            }
        } catch (e: Exception) {
            loadingDialog.dismiss()
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