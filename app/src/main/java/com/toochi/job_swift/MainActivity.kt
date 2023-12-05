package com.toochi.job_swift

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.toochi.job_swift.admin.activity.AdminDashboardActivity
import com.toochi.job_swift.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startActivity(Intent(this, AdminDashboardActivity::class.java))

        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.singInButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }
}