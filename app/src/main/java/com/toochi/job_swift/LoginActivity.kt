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
import com.toochi.job_swift.backend.AuthenticationManager.loginWithEmailAndPassword
import com.toochi.job_swift.backend.AuthenticationManager.registerUser
import com.toochi.job_swift.backend.AuthenticationManager.registerWithGoogle
import com.toochi.job_swift.databinding.ActivityLoginBinding
import com.toochi.job_swift.model.User
import com.toochi.job_swift.user.activity.UserDashboardActivity

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

        val from = getSharedPreferences("login", MODE_PRIVATE).getString("from", "")

        if (from == "sign up") {
            updateLayoutVisibility(isSignIn = false, isSignUp = true)
        } else {
            updateLayoutVisibility(isSignIn = true, isSignUp = false)
        }

        binding.signUpInsteadButton.setOnClickListener {
            updateLayoutVisibility(isSignIn = false, isSignUp = true)
        }

        binding.signInInsteadButton.setOnClickListener {
            updateLayoutVisibility(isSignIn = true, isSignUp = false)
        }


        signInWithEmailAndPassword()

        signUpWithEmailAndPassword()
        signUpWithGoogle()

    }

    private fun signUpWithEmailAndPassword() {
        binding.signUpButton.setOnClickListener {
            val firstname = binding.firstnameTextInput.editText?.text.toString().trim()
            val lastname = binding.lastnameTextInput.editText?.text.toString().trim()
            val password = binding.passwordTextInput.editText?.text.toString().trim()
            val email = binding.emailTextInput.editText?.text.toString().trim()

            registerUser(
                User(email, password, firstname, lastname, "admin")
            ) { success, errorMessage ->
                if (success) {
                    launchDashboardActivity("user")
                } else {
                    println(errorMessage)
                }
            }
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
        binding.googleSignUpButton.setOnClickListener {
            val googleSignInClient = getGoogleSignInClient(this)

            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)

            /* googleSignInClient.signOut().addOnCompleteListener {
                 val signInIntent = googleSignInClient.signInIntent
                 googleSignInLauncher.launch(signInIntent)
             }*/
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)

            registerWithGoogle(account!!) { success, errorMessage ->
                if (success) {
                    launchDashboardActivity("user")
                } else {
                    print("$errorMessage")
                }
            }
        } catch (e: ApiException) {
            e.printStackTrace()
        }

    }

    private fun signInWithEmailAndPassword() {
        binding.signInButton.setOnClickListener {
            val email = binding.signInEmail.editText?.text.toString().trim()
            val password = binding.signInPassword.editText?.text.toString().trim()

            loginWithEmailAndPassword(email, password) { success, errorMessage ->
                if (success) {
                    launchDashboardActivity("admin")
                } else {
                    print(errorMessage)
                }

            }
        }
    }

    private fun launchDashboardActivity(who: String) {
        if (who == "admin") {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, UserDashboardActivity::class.java))
        }

        finish()
    }

    private fun updateLayoutVisibility(isSignIn: Boolean, isSignUp: Boolean) {
        binding.signInLayout.isVisible = isSignIn
        binding.signUpLayout.isVisible = isSignUp
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