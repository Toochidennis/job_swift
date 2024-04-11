package com.toochi.job_swift.common.fragments

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.toochi.job_swift.R
import com.toochi.job_swift.admin.activity.AdminDashboardActivity
import com.toochi.job_swift.backend.AuthenticationManager
import com.toochi.job_swift.backend.AuthenticationManager.registerWithEmailAndPassword
import com.toochi.job_swift.backend.AuthenticationManager.registerWithGoogleAndLogin
import com.toochi.job_swift.backend.PersonalDetailsManager.getUserPersonalDetails
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentSignUpBinding
import com.toochi.job_swift.model.User
import com.toochi.job_swift.user.activity.UserDashboardActivity
import com.toochi.job_swift.util.Constants
import com.toochi.job_swift.util.Constants.Companion.ADMIN
import com.toochi.job_swift.util.Constants.Companion.ADMIN_PASSWORD
import com.toochi.job_swift.util.Constants.Companion.EMAIL
import com.toochi.job_swift.util.Constants.Companion.EMPLOYEE
import com.toochi.job_swift.util.Constants.Companion.PHONE_NUMBER
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Utils.isValidEmailOrPhoneNumber
import com.toochi.job_swift.util.Utils.updateSharedPreferences

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        loadingDialog = LoadingDialog(requireContext())

        handleViewsClick()
    }

    private fun handleViewsClick() {
        binding.signInButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.googleSignUpButton.setOnClickListener {
            signUpWithGoogle()
        }

        binding.signUpButton.setOnClickListener {
            signUpWithEmailAndPassword()
        }
    }


    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result?.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            }else{
                showToast("Login failed. Please use another method to create account")
            }
        }

    private fun signUpWithGoogle() {
        val googleSignInClient = AuthenticationManager.getGoogleSignInClient(requireContext())
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
                            handleAuthenticationResult(error, user)
                        }
                    } else {
                        loadingDialog.dismiss()
                        showToast(errorMessage.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }


    private fun signUpWithEmailAndPassword() {
        editTextWatcher(binding.emailTextInput, EMAIL)
        editTextWatcher(binding.phoneNumberTextInput, PHONE_NUMBER)

        try {
            val user = getUserFromForm()

            if (isValidSignUpForm(user)) {
                loadingDialog.show()
                registerWithEmailAndPassword(user) { success, errorMessage ->
                    if (success) {
                        getUserPersonalDetails { result, error ->
                            handleAuthenticationResult(error, result)
                        }
                    } else {
                        loadingDialog.dismiss()
                        showToast(errorMessage.toString())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }

    private fun handleAuthenticationResult(errorMessage: String?, user: User?) {
        when {
            user != null -> {
                updateSharedPreferences(user, sharedPreferences)
                loadingDialog.dismiss()
                launchDashboardActivity(user.userType)
            }

            errorMessage == Constants.NOT_AVAILABLE -> showToastAndDismiss("User details not available")
            else -> showToastAndDismiss(errorMessage.toString())
        }
    }

    private fun showToastAndDismiss(message: String) {
        loadingDialog.dismiss()
        showToast(message)
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

    private fun launchDashboardActivity(who: String) {
        val intent = if (who == ADMIN) {
            Intent(requireContext(), AdminDashboardActivity::class.java)
        } else {
            Intent(requireContext(), UserDashboardActivity::class.java)
        }

        startActivity(intent)
        requireActivity().finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}