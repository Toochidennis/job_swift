package com.toochi.job_swift

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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

        startActivity(Intent(this, UserDashboardActivity::class.java))
        finish()

    }
}