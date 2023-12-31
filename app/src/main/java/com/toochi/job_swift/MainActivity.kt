package com.toochi.job_swift

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.toochi.job_swift.admin.activity.AdminDashboardActivity
import com.toochi.job_swift.common.activities.LoginActivity
import com.toochi.job_swift.common.activities.WelcomeActivity
import com.toochi.job_swift.databinding.ActivityMainBinding
import com.toochi.job_swift.user.activity.UserDashboardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startActivity(Intent(this@MainActivity, UserDashboardActivity::class.java))

        finish()

        /* with(getSharedPreferences("loginDetail", MODE_PRIVATE)) {
             when (getString("user_type", "")) {
                 "admin" -> {
                     startActivity(Intent(this@MainActivity, AdminDashboardActivity::class.java))
                 }

                 "employer", "employee" -> {
                     startActivity(Intent(this@MainActivity, UserDashboardActivity::class.java))
                 }

                 "sign out" -> {
                     startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                 }

                 else -> {
                     startActivity(Intent(this@MainActivity, WelcomeActivity::class.java))
                 }
             }

             finish()
         }
 */
    }
}