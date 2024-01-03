package com.toochi.job_swift.common.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.sendPasswordResetEmail
import com.toochi.job_swift.common.dialogs.AlertDialog
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentPasswordResetDialogBinding
import com.toochi.job_swift.util.Utils.isValidEmailOrPhoneNumber

class PasswordResetDialogFragment : DialogFragment() {

    private var _binding: FragmentPasswordResetDialogBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordResetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        actionBar?.apply {
            title = "Password reset"
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.toolbar.setNavigationOnClickListener {
            dismiss()
        }


        binding.sendButton.setOnClickListener {
            processForm()
        }
    }

    private fun processForm() {
        binding.emailTextField.run {
            if (text.isEmpty()) {
                error = getString(R.string.please_provide_email)
            } else if (!isValidEmailOrPhoneNumber(text.toString(), "email")) {
                error = getString(R.string.invalid_email)
            } else {
                resetPassword(text.toString().trim())
            }
        }
    }

    private fun resetPassword(email: String) {
        val loadingDialog = LoadingDialog(requireContext())

        try {
            sendPasswordResetEmail(email) { sent, exception ->
                if (sent) {
                    loadingDialog.dismiss()
                    showSuccessMessage()
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            Toast.makeText(requireContext(), e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessMessage() {
        AlertDialog.Builder(requireContext()).run {
            title = "Sent successfully!"
            body = "Please check your mail app to reset your password"
            isPositiveVisible = true
            isNegativeVisible = false
            positiveMessage = "Ok"
            positiveClickListener = {
                dismiss()
            }

            build().show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}