package com.toochi.job_swift.user.fragment

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.updateUserDetails
import com.toochi.job_swift.common.dialogs.DatePickerDialog
import com.toochi.job_swift.databinding.FragmentContactInfoDialogBinding
import com.toochi.job_swift.model.User
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME


class ContactInfoDialogFragment : DialogFragment() {

    private var _binding: FragmentContactInfoDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentContactInfoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        initData()
        handleViewClicks()
    }

    private fun initData() {
        with(sharedPreferences) {
            val address = getString("address", "")
            val dateOfBirth = getString("dob", "")
            val email = getString("email", "")
            val phone = getString("phone_number", "")

            binding.emailTextView.text = email ?: ""
            binding.phoneNumberTextField.setText(phone ?: "")
            binding.addressTextField.setText(address ?: "")
            binding.birthdayTextField.setText(dateOfBirth ?: "")
        }
    }

    private fun handleViewClicks() {
        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.birthdayTextField.setOnClickListener {
            DatePickerDialog(requireContext()) { date ->
                binding.birthdayTextField.setText(date)
            }.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        binding.saveButton.setOnClickListener {
            processForm()
        }
    }

    private fun processForm() {
        val user = getDataFromForm()

        val profileId = sharedPreferences.getString("profile_id", "")

        if (profileId != null) {
            updateUserDetails(
                profileId = profileId,
                hashMapOf(
                    "phoneNumber" to user.phoneNumber,
                    "address" to user.address,
                    "dateOfBirth" to user.dateOfBirth
                )
            ) { success, error ->
                if (success) {
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun getDataFromForm(): User {
        return User(
            phoneNumber = binding.phoneNumberTextField.text.toString().trim(),
            address = binding.addressTextField.text.toString().trim(),
            dateOfBirth = binding.birthdayTextField.text.toString().trim()
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}