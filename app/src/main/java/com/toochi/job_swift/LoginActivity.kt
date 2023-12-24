package com.toochi.job_swift

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.toochi.job_swift.admin.activity.AdminDashboardActivity
import com.toochi.job_swift.backend.AuthenticationManager.getGoogleSignInClient
import com.toochi.job_swift.backend.AuthenticationManager.loginWithEmailAndPassword
import com.toochi.job_swift.backend.AuthenticationManager.registerWithEmailAndPassword
import com.toochi.job_swift.backend.AuthenticationManager.registerWithGoogle
import com.toochi.job_swift.databinding.ActivityLoginBinding
import com.toochi.job_swift.model.User
import com.toochi.job_swift.user.activity.UserDashboardActivity
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.apply {
            title = ""
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        viewClicks()

    }

    private fun viewClicks() {
        binding.signUpInsteadButton.setOnClickListener {
            updateLayoutVisibility(isSignIn = false, isSignUp = true)
        }

        binding.signInInsteadButton.setOnClickListener {
            updateLayoutVisibility(isSignIn = true, isSignUp = false)
        }

        binding.googleSignUpButton.setOnClickListener {
            signUpWithGoogle()
        }

        binding.googleSignInButton.setOnClickListener {
            signUpWithGoogle()
        }

        signInWithEmailAndPassword()

        signUpWithEmailAndPassword()

    }

    private fun signUpWithEmailAndPassword() {
        binding.signUpButton.setOnClickListener {
            val firstname = binding.firstnameTextInput.editText?.text.toString().trim()
            val lastname = binding.lastnameTextInput.editText?.text.toString().trim()
            val password = binding.passwordTextInput.editText?.text.toString().trim()
            val email = binding.emailTextInput.editText?.text.toString().trim()
            val phoneNumber = binding.phoneNumberTextInput.editText?.text.toString().trim()

            editTextWatcher(binding.emailTextInput, "email")
            editTextWatcher(binding.phoneNumberTextInput, "phone_number")


            if (isValidSignUpForm()) {
                registerWithEmailAndPassword(
                    User().apply {
                        this.email = email
                        this.password = password
                        this.firstname = firstname
                        this.lastname = lastname
                        this.userType = "employee"
                        this.phoneNumber = phoneNumber
                    }
                ) { success, errorMessage ->
                    if (success) {
                        launchDashboardActivity("user")
                    } else {
                        showToast(errorMessage.toString())
                    }
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
        val googleSignInClient = getGoogleSignInClient(this)

        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)

        /* googleSignInClient.signOut().addOnCompleteListener {
             val signInIntent = googleSignInClient.signInIntent
             googleSignInLauncher.launch(signInIntent)
         }*/
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)

            registerWithGoogle(this, account!!) { success, errorMessage ->
                if (success) {
                    launchDashboardActivity("user")
                    println("Yeah")
                } else {
                    showToast(errorMessage.toString())
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

            editTextWatcher(binding.signInEmail, "email")

            if (isValidSignInForm()) {
                loginWithEmailAndPassword(email, password) { success, errorMessage ->
                    if (success) {
                        launchDashboardActivity("user")
                    } else {
                        showToast(errorMessage.toString())
                    }
                }
            }
        }
    }

    private fun isValidSignUpForm(): Boolean {
        return when {
            binding.firstnameTextInput.editText?.text.isNullOrBlank() -> {
                showToast("Please provide first name")
                false
            }

            binding.lastnameTextInput.editText?.text.isNullOrBlank() -> {
                showToast("Please provide lastname")
                false
            }

            binding.phoneNumberTextInput.editText?.text.isNullOrBlank() -> {
                showToast("Please provide phone number")
                false
            }

            binding.emailTextInput.editText?.text.isNullOrBlank() -> {
                showToast("Please provide email")
                false
            }

            binding.passwordTextInput.editText?.text.isNullOrBlank() -> {
                showToast("Please provide password")
                false
            }

            else -> true
        }
    }

    private fun isValidSignInForm(): Boolean {
        return when {
            binding.signInEmail.editText?.text.isNullOrBlank() -> {
                showToast("Please provide email")
                false
            }

            binding.signInPassword.editText?.text.isNullOrBlank() -> {
                showToast("Please provide password")
                false
            }

            else -> true
        }
    }

    private fun editTextWatcher(
        textInputLayout: TextInputLayout,
        from: String
    ) {
        textInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()

                if (isValidEmailOrPhoneNumber(email, from)) {
                    textInputLayout.error = null
                } else {
                    textInputLayout.error = if (from == "email") {
                        "Invalid email"
                    } else {
                        "Invalid phone number"
                    }
                }
            }
        })
    }

    private fun isValidEmailOrPhoneNumber(text: String, from: String): Boolean {
        return if (from == "email") {
            Patterns.EMAIL_ADDRESS.matcher(text).matches()
        } else {
            val phonePattern = Pattern.compile(
                "^(\\+?234|0)?([789]\\d{9})\$"
            )
            phonePattern.matcher(text).matches()
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

    private fun showToast(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
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