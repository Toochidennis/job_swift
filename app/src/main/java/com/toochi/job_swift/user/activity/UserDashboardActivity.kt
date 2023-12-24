package com.toochi.job_swift.user.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.toochi.job_swift.R
import com.toochi.job_swift.common.fragments.NotificationsFragment
import com.toochi.job_swift.databinding.ActivityUserDashboardBinding
import com.toochi.job_swift.user.fragment.*
import com.toochi.job_swift.util.Utils.loadFragment

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDashboardBinding

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomBarNavigation()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isHomeFragmentVisible()) {
                    if (doubleBackToExitPressedOnce) {
                        finish()
                    } else {
                        doubleBackToExitPressedOnce = true
                        Toast.makeText(
                            this@UserDashboardActivity,
                            "Press back again to exit",
                            Toast.LENGTH_SHORT
                        ).show()

                        Handler(Looper.getMainLooper()).postDelayed({
                            doubleBackToExitPressedOnce = false
                        }, 2000)
                    }
                } else {
                    bottomBarNavigation()
                    binding.bottomNavigation.selectedItemId = R.id.home
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
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

    private fun isHomeFragmentVisible(): Boolean {
        return binding.bottomNavigation.selectedItemId == R.id.home
    }

}
