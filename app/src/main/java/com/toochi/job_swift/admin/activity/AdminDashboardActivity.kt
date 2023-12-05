package com.toochi.job_swift.admin.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.toochi.job_swift.R
import com.toochi.job_swift.admin.fragment.AdminHomeFragment
import com.toochi.job_swift.admin.fragment.AdminInsightsFragment
import com.toochi.job_swift.admin.fragment.AdminNotificationFragment
import com.toochi.job_swift.admin.fragment.AdminSettingsFragment
import com.toochi.job_swift.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        homeNavigation()

    }

    private fun homeNavigation() {
        loadFragment(AdminHomeFragment())

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    loadFragment(AdminHomeFragment())
                    true
                }

                R.id.insight -> {
                    loadFragment(AdminInsightsFragment())
                    true
                }

                R.id.notification -> {
                    loadFragment(AdminNotificationFragment())
                    true
                }

                R.id.settings -> {
                    loadFragment(AdminSettingsFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.adminContainer, fragment)
        }
    }

}