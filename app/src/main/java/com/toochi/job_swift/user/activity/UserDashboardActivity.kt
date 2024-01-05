package com.toochi.job_swift.user.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.PersonalDetailsManager.updateExistingUser
import com.toochi.job_swift.databinding.ActivityUserDashboardBinding
import com.toochi.job_swift.util.Constants.Companion.PERMISSION_REQUESTED_CODE
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.NotificationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDashboardBinding
    private val userActivityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()

    }

    private fun initData(){
        setUpNavigation()

        userActivityScope.launch {
            saveTokenInBackground()
        }

        viewModel.newNotification.observe(this) {
            badgeSetUp(R.id.notificationsFragment)
        }

    }


    private fun setUpNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNavigation.setupWithNavController(navController)


        navController.addOnDestinationChangedListener{_, destination, _->
            if (destination.id == R.id.notificationsFragment){
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
                    PERMISSION_REQUESTED_CODE
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


    private suspend fun saveTokenInBackground() {
        try {
            // Get the user's FCM token
            val userToken = FirebaseMessaging.getInstance().token.await()
            val profileId =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString("profile_id", "")
            // Perform database update here using the obtained FCM token
            if (profileId != null) {
                updateExistingUser(profileId, hashMapOf("token" to userToken)) { _, _ -> }
            }
        } catch (e: Exception) {
            // Handle exceptions, such as token retrieval failure or database update failure
            e.printStackTrace()
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

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the coroutine scope to avoid potential memory leaks
        userActivityScope.cancel()
    }
}