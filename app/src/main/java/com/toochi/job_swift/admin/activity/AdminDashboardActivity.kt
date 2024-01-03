package com.toochi.job_swift.admin.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.toochi.job_swift.R
import com.toochi.job_swift.admin.fragment.AdminHomeFragment
import com.toochi.job_swift.admin.fragment.AdminInsightsFragment
import com.toochi.job_swift.backend.PersonalDetailsManager.updateExistingUser
import com.toochi.job_swift.common.fragments.NotificationsFragment
import com.toochi.job_swift.databinding.ActivityAdminDashboardBinding
import com.toochi.job_swift.util.Constants
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.NotificationViewModel
import com.toochi.job_swift.util.Utils.loadFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val userActivityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val viewModel: NotificationViewModel by viewModels()

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initData()
    }


    private fun initData(){
        bottomBarNavigation()

        onBackClicked()

        userActivityScope.launch {
            saveTokenInBackground()
        }

        viewModel.newNotification.observe(this) {
            badgeSetUp(R.id.notifications)
        }
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
                    loadFragment(this, AdminInsightsFragment(), container)
                    true
                }

                R.id.notifications -> {
                    loadFragment(this, NotificationsFragment(), container)
                    clearBadge(R.id.notifications)
                    requestPermission()
                    true
                }


                else -> false
            }
        }
    }

    private fun isHomeFragmentVisible(): Boolean {
        return binding.bottomNavigation.selectedItemId == R.id.home
    }

    private fun onBackClicked() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isHomeFragmentVisible()) {
                    if (doubleBackToExitPressedOnce) {
                        finish()
                    } else {
                        doubleBackToExitPressedOnce = true
                        Toast.makeText(
                            this@AdminDashboardActivity,
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

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    Constants.PERMISSION_REQUESTED_CODE
                )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun badgeSetUp(id: Int) {
        val badge = binding.bottomNavigation.getOrCreateBadge(id)
        badge.isVisible = true
    }

    private fun clearBadge(id: Int) {
        val badgeDrawable = binding.bottomNavigation.getBadge(id)
        if (badgeDrawable != null) {
            badgeDrawable.isVisible = false
        }
    }

    private suspend fun saveTokenInBackground() {
        try {
            // Get the user's FCM token
            val userToken = FirebaseMessaging.getInstance().token.await()
            val profileId =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString("profile_id", "")
            // Perform database update here using the obtained FCM token
            if (profileId != null) {
                updateExistingUser(
                    profileId,
                    hashMapOf("token" to userToken)
                ) { _, _ -> }
            }
        } catch (e: Exception) {
            // Handle exceptions, such as token retrieval failure or database update failure
            e.printStackTrace()
        }
    }

}