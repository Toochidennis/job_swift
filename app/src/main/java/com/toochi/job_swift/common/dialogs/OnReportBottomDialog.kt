package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toochi.job_swift.backend.AuthenticationManager.auth
import com.toochi.job_swift.backend.PostJobManager.deleteUserPost
import com.toochi.job_swift.databinding.DialogOnReportBinding
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.util.Constants.Companion.REPORT

class OnReportBottomDialog(private val postJob: PostJob) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogOnReportBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogOnReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reportTextView.isVisible = postJob.userId != auth.uid
        binding.deleteTextView.isVisible = postJob.userId == auth.uid

        binding.reportTextView.setOnClickListener {
            ReportDialogFragment(postJob).show(parentFragmentManager, REPORT)
            dismiss()
        }

        binding.deleteTextView.setOnClickListener {
            confirmDeletion()
        }
    }

    private fun confirmDeletion() {
        AlertDialog.Builder(requireContext()).run {
            title = "Delete Job"
            body = "Are you sure you want to delete this job?"
            isPositiveVisible = true
            isNegativeVisible = true
            negativeMessage = "Cancel"
            positiveMessage = "Delete"
            positiveClickListener = {
                deleteJob()
            }
            negativeClickListener = {
                dismiss()
            }

            build().show()
        }
    }

    private fun deleteJob() {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()
        try {
            deleteUserPost(postJob.userId, postJob.jobId) { deleted, exception ->
                if (deleted) {
                    loadingDialog.dismiss()
                    dismiss()
                    showToast("Job deleted successfully")
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}