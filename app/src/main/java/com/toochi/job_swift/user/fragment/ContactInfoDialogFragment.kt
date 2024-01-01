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
import com.toochi.job_swift.backend.AuthenticationManager.getUserPersonalDetails
import com.toochi.job_swift.backend.AuthenticationManager.updateExistingUser
import com.toochi.job_swift.common.dialogs.DatePickerDialog
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentContactInfoDialogBinding
import com.toochi.job_swift.model.User
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME


class ContactInfoDialogFragment : DialogFragment() {

    private var _binding: FragmentContactInfoDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: LoadingDialog

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
        loadingDialog = LoadingDialog(requireContext())

        initData()
        handleViewClicks()
    }

    private fun initData() {
        loadingDialog.show()
        getUserPersonalDetails { user, exception ->
            if (user != null) {
                binding.emailTextView.text = user.email
                binding.phoneNumberTextField.setText(user.phoneNumber)
                binding.addressTextField.setText(user.address)
                binding.birthdayTextField.setText(user.dateOfBirth)
            } else {
                Toast.makeText(requireContext(), exception.toString(), Toast.LENGTH_SHORT).show()
            }

            loadingDialog.dismiss()
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
            loadingDialog.show()

            updateExistingUser(
                profileId = profileId,
                hashMapOf(
                    "phoneNumber" to user.phoneNumber,
                    "address" to user.address,
                    "dateOfBirth" to user.dateOfBirth
                )
            ) { success, error ->
                if (success) {
                    updateSharedPreference(user)
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_SHORT).show()
                }

                loadingDialog.dismiss()
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

    private fun updateSharedPreference(user: User) {
        sharedPreferences.edit().run {
            putString("phone_number", user.phoneNumber)
            putString("dob", user.dateOfBirth)
            putString("address", user.address)
            apply()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}