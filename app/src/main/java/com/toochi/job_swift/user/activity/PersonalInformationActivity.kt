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
import com.toochi.job_swift.backend.AuthenticationManager.getUserEducationDetails
import com.toochi.job_swift.backend.AuthenticationManager.getUserExperienceDetails
import com.toochi.job_swift.backend.AuthenticationManager.getUserPersonalDetails
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.activities.ImagePreviewActivity
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

        getUserPersonalDetails { user, error ->
            if (user != null) {
                userModel = user
                setUpProfileDetails()
                getExperienceDetails()
                getEducationDetails()
            } else {
                showToast(error.toString())
            }
            loadingDialog.dismiss()
        }
    }

    private fun setUpProfileDetails() {
        userModel?.let { user ->
            val fullName = "${user.firstname} ${user.lastname}"
            val location = "${user.city}, ${user.country}"

            binding.nameTxt.text = fullName
            binding.headlineTxt.text = user.headline
            binding.locationTxt.text = location
            binding.aboutTxt.text = user.about
            binding.skillTxt.text = user.skills

            if (user.profilePhotoUrl.isNotEmpty()) {
                Picasso.get().load(user.profilePhotoUrl).into(binding.profileImageView)
            }

            updateSharedPreferences(user, sharedPreferences)
        }
    }

    private fun getExperienceDetails() {
        experiencesList.clear()

        getUserExperienceDetails { experiences, error ->
            if (experiences != null) {
                experiencesList = experiences

                experiences.forEach { experience ->
                    experience.companyName = "${experience.companyName} . ${experience.jobType}"
                    experience.startDate = "${experience.startDate} - ${experience.endDate}"
                    experience.location = "${experience.location}, ${experience.workplace}"
                }

                setUpExperienceAdapter(experiences)
            } else {
                showToast(error.toString())
            }
        }
    }


    private fun getEducationDetails() {
        educationsList.clear()

        getUserEducationDetails { educations, error ->
            if (educations != null) {
                educationsList = educations

                educations.forEach { education ->
                    education.degree = "${education.degree}, ${education.discipline}"
                    education.startDate = "${education.startDate} - ${education.endDate}"
                    education.grade = "Grade: ${education.grade}"
                }

                setUpEducationAdapter(educations)

            } else {
                showToast(error.toString())
            }
        }
    }


    private fun setUpExperienceAdapter(experienceList: MutableList<Experience>) {
        val experienceAdapter = GenericAdapter(
            itemList = experienceList,
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

    private fun setUpEducationAdapter(educationList: MutableList<Education>) {
        val educationAdapter = GenericAdapter(
            itemList = educationList,
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
                setUpProfileDetails()
            }.show(supportFragmentManager, "Profile")
        }

        binding.editAboutBtn.setOnClickListener {
            AboutDialogFragment(userModel) {
                setUpProfileDetails()
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


    private fun showToast(message: String) {
        Toast.makeText(this@PersonalInformationActivity, message, Toast.LENGTH_SHORT).show()
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