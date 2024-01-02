package com.toochi.job_swift.user.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import com.toochi.job_swift.BR
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.ApplyForJobManager.updateJobsAppliedFor
import com.toochi.job_swift.backend.AuthenticationManager.auth
import com.toochi.job_swift.backend.EducationManager.getUserEducationDetails
import com.toochi.job_swift.backend.ExperienceManager.getUserExperienceDetails
import com.toochi.job_swift.backend.JobAppliedByOthersManager.getJobsAppliedByOthers
import com.toochi.job_swift.backend.JobAppliedByOthersManager.updateJobsAppliedByOthers
import com.toochi.job_swift.backend.PersonalDetailsManager.getUserPersonalDetails
import com.toochi.job_swift.backend.PersonalDetailsManager.getUserToken
import com.toochi.job_swift.common.dialogs.AlertDialog
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.fragments.ResumePreviewDialogFragment
import com.toochi.job_swift.databinding.ActivityApplicationReviewBinding
import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.model.Education
import com.toochi.job_swift.model.Experience
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.model.User
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.util.Constants.Companion.ACCEPTED
import com.toochi.job_swift.util.Constants.Companion.PENDING
import com.toochi.job_swift.util.Constants.Companion.REJECTED
import com.toochi.job_swift.util.Utils.sendNotification

class ApplicationReviewActivity : AppCompatActivity() {

    private var _binding: ActivityApplicationReviewBinding? = null

    private val binding get() = _binding!!

    private var applicantId: String? = null
    private var jobId: String? = null

    private var userModel: User? = null
    private var applyJob: ApplyJob? = null
    private var applicantPhoneNumber = ""
    private var applicantEmail = ""

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityApplicationReviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        applicantId = intent.getStringExtra("user_Id")
        jobId = intent.getStringExtra("job_id")

        loadingDialog = LoadingDialog(this)

        initData()

        handleViewClicks()
    }

    private fun initData() {
        loadingDialog.show()

        try {

            applicantId?.let {
                getUserPersonalDetails(userId = it) { user, _ ->
                    if (user != null) {
                        userModel = user
                        setUpProfileDetails()
                        getApplicantApplication()
                        getApplicantExperience()
                        getApplicantEducation()
                    }

                    loadingDialog.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("An error occurred.")
        } finally {
            loadingDialog.dismiss()
        }
    }

    private fun getApplicantApplication() {
        if (applicantId != null && jobId != null) {
            getJobsAppliedByOthers(
                userId = applicantId!!,
                jobId = jobId!!
            ) { applyJob, errorMessage ->
                if (applyJob != null) {
                    this.applyJob = applyJob
                    handleApplicantApplicationResult(applyJob)
                } else {
                    showToast("Error fetching application details: $errorMessage")
                }
            }
        }
    }

    private fun handleApplicantApplicationResult(applyJob: ApplyJob) {
        binding.cvButton.isVisible = applyJob.cvURl.isNotEmpty()
        binding.rejectButton.isVisible = applyJob.status == PENDING
        binding.acceptButton.isVisible = applyJob.status == PENDING

        if (applyJob.status == REJECTED) {
            showToast("You've rejected this job.")
        } else if (applyJob.status == ACCEPTED) {
            showToast("You've accepted this job.")
        }
    }

    private fun setUpProfileDetails() {
        userModel?.let { user ->
            val fullName = "${user.firstname} ${user.lastname}"
            applicantEmail = user.email
            applicantPhoneNumber = user.phoneNumber

            binding.userNameTextView.text = fullName
            binding.headlineTextView.text = user.headline

            if (user.profilePhotoUrl.isNotEmpty()) {
                Picasso.get().load(user.profilePhotoUrl).into(binding.imageView)
            }
        }
    }

    private fun getApplicantExperience() {
        var copiedExperiences = listOf<Experience>()

        applicantId?.let {
            getUserExperienceDetails(userId = it) { experiences, _ ->
                if (experiences != null) {
                    copiedExperiences = experiences.map { experience ->
                        val companyName = "${experience.companyName} . ${experience.jobType}"
                        val date = "${experience.startDate} - ${experience.endDate}"
                        val location = "${experience.location}, ${experience.workplace}"

                        Experience(
                            jobTitle = experience.jobTitle,
                            companyName = companyName,
                            startDate = date,
                            location = location
                        )
                    }
                }

                setUpExperienceAdapter(copiedExperiences.toMutableList())
            }
        }
    }

    private fun getApplicantEducation() {
        var copiedEducations = listOf<Education>()

        applicantId?.let {
            getUserEducationDetails(userId = it) { educations, _ ->
                if (educations != null) {
                    copiedEducations = educations.map { education ->
                        val degree = "${education.degree}, ${education.discipline}"
                        val startDate = "${education.startDate} - ${education.endDate}"
                        val grade = "Grade: ${education.grade}"

                        Education(
                            school = education.school,
                            degree = degree,
                            startDate = startDate,
                            grade = grade
                        )
                    }
                }

                setUpEducationAdapter(copiedEducations.toMutableList())
            }
        }
    }

    private fun setUpExperienceAdapter(itemList: MutableList<Experience>) {
        val experienceAdapter = GenericAdapter(
            itemList = itemList,
            itemResLayout = R.layout.item_experience,
            bindItem = { binding, model ->
                binding.setVariable(BR.experience, model)
                binding.executePendingBindings()
            }
        ) {}

        binding.experienceRecyclerView.apply {
            hasFixedSize()
            adapter = experienceAdapter
        }
    }

    private fun setUpEducationAdapter(itemList: MutableList<Education>) {
        val educationAdapter = GenericAdapter(
            itemList = itemList,
            itemResLayout = R.layout.item_education,
            bindItem = { binding, model ->
                binding.setVariable(BR.education, model)
                binding.executePendingBindings()
            }
        ) {}

        binding.educationRecyclerView.apply {
            hasFixedSize()
            adapter = educationAdapter
        }
    }

    private fun handleViewClicks() {
        binding.cvButton.setOnClickListener {
            applyJob?.cvURl?.let { it1 ->
                ResumePreviewDialogFragment(it1).show(
                    supportFragmentManager,
                    "Preview"
                )
            }
        }

        binding.rejectButton.setOnClickListener {
            try {
                loadingDialog.show()
                updateApplicantJobStatus(
                    "Update on job application",
                    "Your job application has been rejected",
                    REJECTED
                )
                updateEmployerJobStatus(
                    "Update on job application",
                    "Your job application has been rejected",
                    REJECTED
                )
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("An error occurred")
            } finally {
                loadingDialog.dismiss()
            }
        }


        binding.acceptButton.setOnClickListener {
            try {
                loadingDialog.show()

                updateApplicantJobStatus(
                    title = "Congratulations!",
                    body = "Your job application has been accepted.",
                    status = ACCEPTED
                )

                updateEmployerJobStatus(
                    title = "Congratulations!",
                    body = "Your job application has been accepted.",
                    status = ACCEPTED
                )
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("An error occurred")
            } finally {
                loadingDialog.dismiss()
            }
        }

        binding.mailButton.setOnClickListener {
            try {
                sendEmail()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.callButton.setOnClickListener {
            try {
                makePhoneCall()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendEmail() {
        if (applicantEmail.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$applicantEmail")
                putExtra(Intent.EXTRA_SUBJECT, "")
                putExtra(Intent.EXTRA_TEXT, "")
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                startActivity(Intent.createChooser(intent, "Send Us a mail"))
            }
        }
    }

    private fun makePhoneCall() {
        if (applicantPhoneNumber.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$applicantPhoneNumber")
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    private fun showMessageToEmployer(title: String, body: String) {
        AlertDialog.Builder(this).apply {
            this.title = title
            this.body = body
            isPositiveVisible = true
            isNegativeVisible = false
            positiveMessage = "Okay"
            positiveClickListener = {
                finish()
            }
            build()
        }
    }

    private fun sendNotification(title: String, body: String, status: String) {
        applicantId?.let { userId ->
            getUserToken(userId) { token, _ ->
                if (token != null) {
                    auth.currentUser?.let { user ->
                        Notification(
                            token = token,
                            title = title,
                            body = body,
                            userId = userId,
                            employerId = user.uid,
                            jobId = jobId ?: "",
                            type = status
                        ).also {
                            sendNotification(this, it) { _, _ ->
                                if (status == REJECTED) {
                                    showMessageToEmployer(
                                        title = "The job has been rejected",
                                        body = "Thank you for your consideration."
                                    )
                                } else {
                                    showMessageToEmployer(
                                        title = "Congratulations!",
                                        body = "The job has been accepted."
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateJobStatus(updateFunction: (String) -> Unit) {
        val applicationId = applyJob?.applicationId
        applicationId?.let {
            updateFunction(it)
        }
    }

    private fun updateApplicantJobStatus(title: String, body: String, status: String) {
        updateJobStatus { applicationId ->
            applicantId?.let { userId ->
                updateJobsAppliedFor(
                    userId = userId,
                    applicationId = applicationId,
                    hashMapOf("status" to status)
                ) { success, error ->
                    handleUpdateResult(success, error, title, body, status)
                }
            }
        }
    }


    private fun updateEmployerJobStatus(title: String, body: String, status: String) {
        updateJobStatus { applicationId ->
            updateJobsAppliedByOthers(
                applicationId = applicationId,
                data = hashMapOf("status" to status)
            ) { success, error ->
                handleUpdateResult(success, error, title, body, status)
            }
        }
    }

    private fun handleUpdateResult(
        success: Boolean,
        error: String?,
        title: String,
        body: String,
        status: String
    ) {
        if (success) {
            sendNotification(title = title, body = body, status = status)
        } else {
            showToast(error ?: "An error occurred")
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this@ApplicationReviewActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding?.let {
            it.imageView.setImageDrawable(null)
            _binding = null
        }
    }
}