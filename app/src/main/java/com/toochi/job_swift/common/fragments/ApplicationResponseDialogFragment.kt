package com.toochi.job_swift.common.fragments

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.ApplyForJobManager.getJobsAppliedForById
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentApplicationResponseDialogBinding
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.util.Constants.Companion.ACCEPTED
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME


class ApplicationResponseDialogFragment(private val notification: Notification) : DialogFragment() {

    private var _binding: FragmentApplicationResponseDialogBinding? = null

    private val binding get() = _binding!!
    private var userName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentApplicationResponseDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply {
            title = if (notification.type == ACCEPTED) "Job accepted" else "Job rejected"
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        val sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val firstname = sharedPreferences.getString("firstname", "")
        val lastname = sharedPreferences.getString("lastname", "")

        userName = "$firstname $lastname"

        getAppliedJob()

    }

    private fun getAppliedJob() {
        val loadingDialog = LoadingDialog(requireContext())

        try {
            getJobsAppliedForById(
                notification.userId,
                notification.jobId,
                notification.employerId
            ) { applyJob, errorMessage ->
                if (applyJob != null) {
                    binding.titleTextView.text = if (applyJob.status == ACCEPTED)
                        getString(R.string.congratulations_your_job_application_was_accepted) else
                        getString(R.string.update_on_your_job_application)

                    binding.messageTextView.text = getMessage(
                        applyJob.status,
                        userName ?: "",
                        applyJob.jobTitle,
                        applyJob.company
                    )
                } else {
                    Toast.makeText(requireContext(), errorMessage.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "An error occurred.", Toast.LENGTH_SHORT).show()
        }

    }


    private fun getMessage(
        status: String,
        name: String,
        jobTitle: String,
        companyName: String
    ): String {
        return if (status == ACCEPTED) {
            """
               Dear $name,
                
               We are pleased to inform you that your application for the position of $jobTitle has been accepted. Congratulations! Welcome to our team.
    
               Please stay tuned for further details and instructions regarding the next steps.
     
     
               Best regards,
               $companyName
                
            """.trimIndent()
        } else {
            """
              Dear $name,
     
              We appreciate your interest in the position of $jobTitle at $companyName. After careful consideration, we regret to inform you that your application has not been successful at this time.
     
              We sincerely thank you for your effort and interest in our company. We encourage you to explore other opportunities and wish you success in your future endeavors.
     
     
              Best regards,
              $companyName
                
            """.trimIndent()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}