package com.toochi.job_swift.user.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.usersDocument
import com.toochi.job_swift.common.fragments.NotificationsFragment
import com.toochi.job_swift.databinding.ActivityUserDashboardBinding
import com.toochi.job_swift.user.fragment.UserHomeFragment
import com.toochi.job_swift.user.fragment.UserJobsFragment
import com.toochi.job_swift.user.fragment.UserSettingsFragment
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Utils.loadFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDashboardBinding
    private val userActivityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userActivityScope.launch {
            saveTokenInBackground()
        }

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

    private suspend fun saveTokenInBackground() {
        try {
            // Get the user's FCM token
            val userToken = FirebaseMessaging.getInstance().token.await()

            // Perform database update here using the obtained FCM token
            updateTokenInFirestore(userToken)
        } catch (e: Exception) {
            // Handle exceptions, such as token retrieval failure or database update failure
            e.printStackTrace()
        }
    }

    private suspend fun updateTokenInFirestore(userToken: String?) {
        try {
            val profileId =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString("profile_id", "")

            profileId?.let {
                usersDocument?.collection("personalDetails")
                    ?.document(it)
                    ?.update("token", userToken)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the coroutine scope to avoid potential memory leaks
        userActivityScope.cancel()
    }
}