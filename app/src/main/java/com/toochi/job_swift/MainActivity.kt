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
import com.toochi.job_swift.util.Constants.Companion.ADMIN
import com.toochi.job_swift.util.Constants.Companion.EMPLOYEE
import com.toochi.job_swift.util.Constants.Companion.EMPLOYER
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Constants.Companion.SIGN_OUT

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(getSharedPreferences(PREF_NAME, MODE_PRIVATE)) {
            val intent = when (getString("user_type", "")) {
                ADMIN -> {
                    Intent(this@MainActivity, AdminDashboardActivity::class.java)
                }

                EMPLOYER, EMPLOYEE -> {
                    Intent(this@MainActivity, UserDashboardActivity::class.java)
                }

                SIGN_OUT -> {
                    Intent(this@MainActivity, LoginActivity::class.java)
                }

                else -> {
                    Intent(this@MainActivity, WelcomeActivity::class.java)
                }
            }

            startActivity(intent)

            finish()
        }
    }

}