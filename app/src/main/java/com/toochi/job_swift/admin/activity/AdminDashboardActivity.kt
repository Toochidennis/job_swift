package com.toochi.job_swift.admin.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.toochi.job_swift.R
import com.toochi.job_swift.admin.fragment.AdminHomeFragment
import com.toochi.job_swift.admin.fragment.AdminInsightsFragment
import com.toochi.job_swift.admin.fragment.AdminSettingsFragment
import com.toochi.job_swift.common.fragments.NotificationsFragment
import com.toochi.job_swift.databinding.ActivityAdminDashboardBinding
import com.toochi.job_swift.util.Utils.loadFragment

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        bottomBarNavigation()

    }

    private fun bottomBarNavigation() {
        val container = R.id.adminContainer
        loadFragment(this, AdminHomeFragment(), container)

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    loadFragment(this, AdminHomeFragment(), container)
                    true
                }

                R.id.insight -> {
                    loadFragment(this,AdminInsightsFragment(), container)
                    true
                }

                R.id.notifications -> {
                    loadFragment(this, NotificationsFragment(), container)
                    true
                }

                R.id.settings -> {
                    loadFragment(this,AdminSettingsFragment(), container)
                    true
                }

                else -> false
            }
        }
    }

}