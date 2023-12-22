package com.toochi.job_swift.user.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.ActivityPersonalInformationBinding
import com.toochi.job_swift.user.fragment.AboutDialogFragment
import com.toochi.job_swift.user.fragment.EditPersonalInfoDialogFragment
import com.toochi.job_swift.user.fragment.EducationDialogFragment
import com.toochi.job_swift.user.fragment.UserExperienceDialogFragment


class PersonalInformationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalInformationBinding

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


        binding.editProfileButton.setOnClickListener {
            EditPersonalInfoDialogFragment().show(supportFragmentManager, "Profile")
        }

        binding.editAboutBtn.setOnClickListener {
            AboutDialogFragment().show(supportFragmentManager, "about")
        }

        binding.editExperienceBtn.setOnClickListener {
            UserExperienceDialogFragment().show(supportFragmentManager, "experience")
        }

        binding.editEducationBtn.setOnClickListener {
            EducationDialogFragment().show(supportFragmentManager, "education")
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