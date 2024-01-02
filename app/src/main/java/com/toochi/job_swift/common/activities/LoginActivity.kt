package com.toochi.job_swift.common.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.toochi.job_swift.R
import com.toochi.job_swift.admin.activity.AdminDashboardActivity
import com.toochi.job_swift.backend.AuthenticationManager.getGoogleSignInClient
import com.toochi.job_swift.backend.AuthenticationManager.loginWithEmailAndPassword
import com.toochi.job_swift.backend.AuthenticationManager.registerWithEmailAndPassword
import com.toochi.job_swift.backend.AuthenticationManager.registerWithGoogleAndLogin
import com.toochi.job_swift.backend.PersonalDetailsManager.getUserPersonalDetails
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.ActivityLoginBinding
import com.toochi.job_swift.model.User
import com.toochi.job_swift.user.activity.UserDashboardActivity
import com.toochi.job_swift.util.Constants.Companion.ADMIN
import com.toochi.job_swift.util.Constants.Companion.ADMIN_PASSWORD
import com.toochi.job_swift.util.Constants.Companion.EMAIL
import com.toochi.job_swift.util.Constants.Companion.EMPLOYEE
import com.toochi.job_swift.util.Constants.Companion.PHONE_NUMBER
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Utils.isValidEmailOrPhoneNumber
import com.toochi.job_swift.util.Utils.updateSharedPreferences

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: LoadingDialog


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

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        loadingDialog = LoadingDialog(this)

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
            val user = getUserFromForm()

            editTextWatcher(binding.emailTextInput, EMAIL)
            editTextWatcher(binding.phoneNumberTextInput, PHONE_NUMBER)

            try {
                if (isValidSignUpForm(user)) {
                    loadingDialog.show()
                    registerWithEmailAndPassword(user) { success, errorMessage ->
                        handleAuthenticationResult(success, errorMessage, user)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("An error occurred.")
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
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)

            account?.let {
                loadingDialog.show()

                registerWithGoogleAndLogin(it) { success, errorMessage ->
                    if (success) {
                        getUserPersonalDetails { user, error ->
                            handleAuthenticationResult(success, error, user)
                        }
                    } else {
                        showToast(errorMessage.toString())
                    }
                    loadingDialog.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("An error occurred.")
        }
    }

    private fun signInWithEmailAndPassword() {
        binding.signInButton.setOnClickListener {
            val email = binding.signInEmail.editText?.text.toString().trim()
            val password = binding.signInPassword.editText?.text.toString().trim()

            editTextWatcher(binding.signInEmail, EMAIL)
            try {
                if (isValidSignInForm(email, password)) {
                    loadingDialog.show()

                    loginWithEmailAndPassword(email, password) { success, errorMessage ->
                        if (success) {
                            getUserPersonalDetails { user, error ->
                                handleAuthenticationResult(success, error, user)
                            }
                        } else {
                            showToast(errorMessage.toString())
                        }

                        loadingDialog.dismiss()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                showToast("An error occurred.")
            }
        }

    }

    private fun getUserFromForm(): User {
        return User(
            email = binding.emailTextInput.editText?.text.toString().trim(),
            password = binding.passwordTextInput.editText?.text.toString().trim(),
            firstname = binding.firstnameTextInput.editText?.text.toString().trim(),
            lastname = binding.lastnameTextInput.editText?.text.toString().trim(),
            userType = if (binding.passwordTextInput.editText?.text.toString()
                    .trim() == ADMIN_PASSWORD
            ) ADMIN else EMPLOYEE,
            phoneNumber = binding.phoneNumberTextInput.editText?.text.toString().trim(),
        )
    }

    private fun isValidSignUpForm(user: User): Boolean {
        return when {
            user.firstname.isBlank() -> {
                showToast(getString(R.string.please_provide_first_name))
                false
            }

            user.lastname.isBlank() -> {
                showToast(getString(R.string.please_provide_lastname))
                false
            }

            user.phoneNumber.isBlank() -> {
                showToast(getString(R.string.please_provide_phone_number))
                false
            }

            user.email.isBlank() -> {
                showToast(getString(R.string.please_provide_email))
                false
            }

            user.password.isBlank() -> {
                showToast(getString(R.string.please_provide_password))
                false
            }

            else -> true
        }
    }

    private fun isValidSignInForm(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                showToast(getString(R.string.please_provide_email))
                false
            }

            password.isBlank() -> {
                showToast(getString(R.string.please_provide_password))
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
                    textInputLayout.error = if (from == EMAIL) {
                        getString(R.string.invalid_email)
                    } else {
                        getString(R.string.invalid_phone_number)
                    }
                }
            }
        })
    }

    private fun handleAuthenticationResult(success: Boolean, errorMessage: String?, user: User?) {
        if (success) {
            user?.let {
                updateSharedPreferences(it, sharedPreferences)
                launchDashboardActivity(it.userType)
            }
        } else {
            showToast(errorMessage.toString())
        }

        loadingDialog.dismiss()
    }

    private fun launchDashboardActivity(who: String) {
        val intent = if (who == ADMIN) {
            Intent(this, AdminDashboardActivity::class.java)
        } else {
            Intent(this, UserDashboardActivity::class.java)
        }

        startActivity(intent)
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