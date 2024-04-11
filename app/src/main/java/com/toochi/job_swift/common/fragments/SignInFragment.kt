package com.toochi.job_swift.common.fragments

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.toochi.job_swift.R
import com.toochi.job_swift.admin.activity.AdminDashboardActivity
import com.toochi.job_swift.backend.AuthenticationManager
import com.toochi.job_swift.backend.AuthenticationManager.registerWithGoogleAndLogin
import com.toochi.job_swift.backend.PersonalDetailsManager.getUserPersonalDetails
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentSignInBinding
import com.toochi.job_swift.model.User
import com.toochi.job_swift.user.activity.UserDashboardActivity
import com.toochi.job_swift.util.Constants
import com.toochi.job_swift.util.Constants.Companion.EMAIL
import com.toochi.job_swift.util.Constants.Companion.NOT_AVAILABLE
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Utils.isValidEmailOrPhoneNumber
import com.toochi.job_swift.util.Utils.updateSharedPreferences


class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        loadingDialog = LoadingDialog(requireContext())

        handleViewsClick()
    }

    private fun handleViewsClick() {
        binding.signUpInsteadButton.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.googleSignInButton.setOnClickListener {
            signUpWithGoogle()
        }

        binding.signInButton.setOnClickListener {
            signInWithEmailAndPassword()
        }


        binding.forgotPasswordButton.setOnClickListener {
            PasswordResetDialogFragment().show(parentFragmentManager, "")
        }

    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result?.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            }else{
                showToast("Login failed. Please try using another method")
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


    private fun signInWithEmailAndPassword() {
        val email = binding.signInEmail.editText?.text.toString().trim()
        val password = binding.signInPassword.editText?.text.toString().trim()

        emailEditTextWatcher()

        try {
            if (isValidSignInForm(email, password)) {
                loadingDialog.show()

                AuthenticationManager.loginWithEmailAndPassword(
                    email,
                    password
                ) { success, errorMessage ->
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


    private fun handleAuthenticationResult(errorMessage: String?, user: User?) {
        when {
            user != null -> {
                updateSharedPreferences(user, sharedPreferences)
                loadingDialog.dismiss()
                launchDashboardActivity(user.userType)
            }
            errorMessage == NOT_AVAILABLE -> showToastAndDismiss("User details not available")
            else -> showToastAndDismiss(errorMessage.toString())
        }
    }

    private fun showToastAndDismiss(message: String) {
        loadingDialog.dismiss()
        showToast(message)
    }


    private fun emailEditTextWatcher() {
        binding.signInEmail.run {
            editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    val email = s.toString().trim()

                    error = if (isValidEmailOrPhoneNumber(email, EMAIL)) {
                        null
                    } else {
                        getString(R.string.invalid_email)
                    }
                }
            })
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


    private fun launchDashboardActivity(who: String) {
        val intent = if (who == Constants.ADMIN) {
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