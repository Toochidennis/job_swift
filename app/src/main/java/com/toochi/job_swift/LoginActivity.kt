package com.toochi.job_swift

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.toochi.job_swift.admin.activity.AdminDashboardActivity
import com.toochi.job_swift.backend.AuthenticationManager.getGoogleSignInClient
import com.toochi.job_swift.backend.AuthenticationManager.registerUser
import com.toochi.job_swift.backend.AuthenticationManager.registerWithGoogle
import com.toochi.job_swift.databinding.ActivityLoginBinding
import com.toochi.job_swift.model.User

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = ""
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.signUpInsteadButton.setOnClickListener {
            // startActivity(Intent(this, CreateAccountActivity::class.java))
            binding.signInLayout.isVisible = false
            binding.signUpLayout.isVisible = true
        }

        binding.signInInsteadButton.setOnClickListener {
            binding.signInLayout.isVisible = true
            binding.signUpLayout.isVisible = false
        }

        binding.signInButton.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
        }

        binding.signUpButton.setOnClickListener {
            signUpWithEmailAndPassword()
            /*startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()*/
        }

        binding.googleSignUpButton.setOnClickListener {
            signUpWithGoogle()
        }

    }

    private fun signUpWithEmailAndPassword() {
        val firstname = binding.firstnameTextInput.editText?.text.toString().trim()
        val lastname = binding.lastnameTextInput.editText?.text.toString().trim()
        val password = binding.passwordTextInput.editText?.text.toString().trim()
        val email = binding.emailTextInput.editText?.text.toString().trim()

        registerUser(
            User(email, password, firstname, lastname, "admin")
        ) { response, error ->
            println("$response, $error")
        }
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result?.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            }
        }

    private fun signUpWithGoogle() {
        val googleSignInClient = getGoogleSignInClient(this)

        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)

            registerWithGoogle(account!!) { success, errorMessage ->
                if (success) {
                    print("Google yeh")
                } else {
                    print("Google waaah $errorMessage")
                }
            }
        } catch (e: ApiException) {
            e.printStackTrace()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else {
            false
        }
    }
}