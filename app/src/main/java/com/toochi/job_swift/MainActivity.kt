package com.toochi.job_swift

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.toochi.job_swift.admin.activity.AdminDashboardActivity
import com.toochi.job_swift.databinding.ActivityMainBinding
import com.toochi.job_swift.user.activity.UserDashboardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)

        binding.signUpButton.setOnClickListener {
            sharedPreferences.edit().putString("from", "sign up").apply()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.singInButton.setOnClickListener {
            sharedPreferences.edit().putString("from", "sign in").apply()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        startActivity(Intent(this, UserDashboardActivity::class.java))
        finish()

    }
}