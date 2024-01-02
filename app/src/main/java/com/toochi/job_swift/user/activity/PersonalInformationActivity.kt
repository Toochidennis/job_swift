package com.toochi.job_swift.user.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.squareup.picasso.Picasso
import com.toochi.job_swift.BR
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.EducationManager.getUserEducationDetails
import com.toochi.job_swift.backend.ExperienceManager.getUserExperienceDetails
import com.toochi.job_swift.backend.PersonalDetailsManager.getUserPersonalDetails
import com.toochi.job_swift.common.activities.ImagePreviewActivity
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.ActivityPersonalInformationBinding
import com.toochi.job_swift.model.Education
import com.toochi.job_swift.model.Experience
import com.toochi.job_swift.model.User
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.user.fragment.AboutDialogFragment
import com.toochi.job_swift.user.fragment.EditPersonalInfoDialogFragment
import com.toochi.job_swift.user.fragment.EducationDialogFragment
import com.toochi.job_swift.user.fragment.UserExperienceDialogFragment
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Utils.updateSharedPreferences


class PersonalInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInformationBinding

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: LoadingDialog

    private var experiencesList = mutableListOf<Experience>()
    private var educationsList = mutableListOf<Education>()
    private var userModel: User? = null
    private var company = ""
    private var educationName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.apply {
            title = "Profile info"
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        loadingDialog = LoadingDialog(this)
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        initData()

        handleViewClicks()

        refreshData()
    }

    private fun initData() {
        loadingDialog.show()

        try {
            getUserPersonalDetails { user, _ ->
                if (user != null) {
                    userModel = user
                    setUpProfileDetails()
                    getExperienceDetails()
                    getEducationDetails()
                }

                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            Toast.makeText(
                this@PersonalInformationActivity,
                "An error occurred.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setUpProfileDetails() {
        userModel?.let { user ->
            val fullName = "${user.firstname} ${user.lastname}"
            val location = "${user.city}, ${user.country}"

            binding.nameTxt.text = fullName
            binding.headlineTxt.text = user.headline
            binding.locationTxt.text = location
            binding.bioTxt.text = user.about
            binding.skillTxt.text = user.skills

            if (user.profilePhotoUrl.isNotEmpty()) {
                Picasso.get().load(user.profilePhotoUrl).into(binding.profileImageView)
            }

            updateSharedPreferences(user, sharedPreferences)
        }
    }

    private fun getExperienceDetails() {
        var copiedExperiences = listOf<Experience>()

        getUserExperienceDetails { experiences, _ ->
            if (experiences != null) {
                experiencesList = experiences

                copiedExperiences = experiences.map { experience ->
                    val companyName = "${experience.companyName} . ${experience.jobType}"
                    val date = "${experience.startDate} - ${experience.endDate}"
                    val location = "${experience.location}, ${experience.workplace}"

                    company = experience.companyName

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


    private fun getEducationDetails() {
        var copiedEducations = listOf<Education>()

        getUserEducationDetails { educations, _ ->
            if (educations != null) {
                educationsList = educations

                copiedEducations = educations.map { education ->
                    val degree = "${education.degree}, ${education.discipline}"
                    val startDate = "${education.startDate} - ${education.endDate}"
                    val grade = "Grade: ${education.grade}"

                    educationName = education.school

                    Education(
                        school = education.school,
                        degree = degree,
                        startDate = startDate,
                        grade = grade
                    )
                }
            }

            val comEdu = "$company . $educationName"
            binding.industryTxt.text = comEdu

            setUpEducationAdapter(copiedEducations.toMutableList())
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
        ) { position ->
            UserExperienceDialogFragment(experienceModel = experiencesList[position]) {
                setUpProfileDetails()
            }.show(supportFragmentManager, getString(R.string.experience))
        }

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
        ) {
            EducationDialogFragment(educationsList[it]) {
                setUpProfileDetails()
            }.show(supportFragmentManager, getString(R.string.education))
        }

        binding.educationRecyclerView.apply {
            hasFixedSize()
            adapter = educationAdapter
        }
    }

    private fun handleViewClicks() {
        binding.editProfileButton.setOnClickListener {
            EditPersonalInfoDialogFragment(userModel) {
                initData()
            }.show(supportFragmentManager, "Profile")
        }

        binding.editAboutBtn.setOnClickListener {
            AboutDialogFragment(userModel) {
                initData()
            }.show(supportFragmentManager, getString(R.string.about))
        }

        binding.addExperienceBtn.setOnClickListener {
            UserExperienceDialogFragment {
                getExperienceDetails()
            }.show(supportFragmentManager, getString(R.string.experience))
        }

        binding.addEducationBtn.setOnClickListener {
            EducationDialogFragment {
                getEducationDetails()
            }.show(supportFragmentManager, getString(R.string.education))
        }

        binding.profileImageLayout.setOnClickListener {
            val intent = Intent(this, ImagePreviewActivity::class.java)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                binding.profileImageView,
                ViewCompat.getTransitionName(binding.profileImageView) ?: ""
            )

            startActivity(intent, options.toBundle())
        }
    }

    private fun refreshData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            initData()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else {
            false
        }
    }
}