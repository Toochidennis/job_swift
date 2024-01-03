package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.auth
import com.toochi.job_swift.backend.PersonalDetailsManager.getAdminToken
import com.toochi.job_swift.databinding.FragmentReportBinding
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.util.Constants.Companion.NOT_AVAILABLE
import com.toochi.job_swift.util.Constants.Companion.REPORT
import com.toochi.job_swift.util.Utils.sendNotification

class ReportDialogFragment(private val postJob: PostJob) : DialogFragment() {

    private var _binding: FragmentReportBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reportButton.setOnClickListener {
            processForm()
        }

        binding.navigateUp.setOnClickListener {
            dismiss()
        }
    }

    private fun processForm() {
        val reportText = binding.reportTextField.text.toString().trim()

        if (reportText.isEmpty()) {
            binding.reportTextField.error = "Provide reason"
        } else {
            binding.reportTextField.error = null
            confirmReport(reportText)
        }
    }

    private fun confirmReport(report: String) {
        AlertDialog.Builder(requireContext()).run {
            title = "Report job"
            body = "Are you sure you want to report this job?"
            isPositiveVisible = true
            isNegativeVisible = true
            negativeMessage = "Cancel"
            positiveMessage = "Report"
            positiveClickListener = {
                sendReport(report)
            }
            negativeClickListener?.invoke()

            build().show()
        }
    }


    private fun sendReport(report: String) {
        val loadingDialog = LoadingDialog(requireContext())
        try {
            loadingDialog.show()

            getAdminToken { token, adminId, exception ->
                if (token != null && adminId != null) {
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        Notification(
                            token = token,
                            title = "Job Report",
                            body = "A job has been reported. Please review.",
                            userId = userId,
                            employerId = postJob.userId,
                            jobId = postJob.jobId,
                            type = REPORT,
                            comments = report,
                            adminId = adminId
                        ).also {
                            sendNotification(requireActivity(), it) { _ ->
                                loadingDialog.dismiss()
                                dismiss()
                                showToast("Your report has been submitted successfully.")
                            }
                        }
                    }
                } else if (exception == NOT_AVAILABLE) {
                    loadingDialog.dismiss()
                    showToast("Admin not available")
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