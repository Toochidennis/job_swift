package com.toochi.job_swift.admin.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.PersonalDetailsManager.updateExistingUser
import com.toochi.job_swift.databinding.ActivityAdminDashboardBinding
import com.toochi.job_swift.util.Constants
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.NotificationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val adminActivityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val viewModel: NotificationViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initData()
    }


    private fun initData() {
        setUpNavigation()

        adminActivityScope.launch {
            saveTokenInBackground()
        }

        viewModel.newNotification.observe(this) {
            badgeSetUp(R.id.notificationsFragment)
        }
    }

    private fun setUpNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.findNavController()
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.notificationsFragment) {
                requestPermission()
                clearBadge(R.id.notificationsFragment)
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        adminActivityScope.cancel()
    }

}