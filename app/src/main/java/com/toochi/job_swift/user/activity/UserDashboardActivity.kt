package com.toochi.job_swift.user.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.toochi.job_swift.R
import com.toochi.job_swift.common.fragments.NotificationsFragment
import com.toochi.job_swift.databinding.ActivityUserDashboardBinding
import com.toochi.job_swift.user.fragment.*
import com.toochi.job_swift.util.Utils.loadFragment

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomBarNavigation()
    }

    private fun bottomBarNavigation() {
        val container = R.id.userContainer
        loadFragment(this, UserHomeFragment(), container)

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    loadFragment(this, UserHomeFragment(), container)
                    true
                }

                R.id.my_jobs -> {
                    loadFragment(this, UserJobsFragment(), container)
                    true
                }

                R.id.notifications -> {
                    loadFragment(this, NotificationsFragment(), container)
                    true
                }

                R.id.settings -> {
                    loadFragment(this, UserSettingsFragment(), container)
                    true
                }

                else -> false
            }
        }
    }

}
