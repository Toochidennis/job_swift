package com.toochi.job_swift.user.fragment

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.PersonalDetailsManager.updateExistingUser
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentEditPersonalInfoBinding
import com.toochi.job_swift.model.User
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME


class EditPersonalInfoDialogFragment(
    private val user: User? = null,
    private val onSave: () -> Unit
) : DialogFragment() {

    private var _binding: FragmentEditPersonalInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEditPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillFieldsWithData()

        binding.saveButton.setOnClickListener {
            processForm()
        }

        binding.navigateUp.setOnClickListener {
            dismiss()
        }
    }


    private fun fillFieldsWithData() {
        if (user != null) {
            binding.firstNameTextField.setText(user.firstname)
            binding.lastNameTextField.setText(user.lastname)
            binding.additionalNameTextField.setText(user.middleName)
            binding.countryTextField.setText(user.country)
            binding.cityTextField.setText(user.city)
            binding.headlineTextField.setText(user.headline)
        }
    }

    private fun isValidForm(user: User): Boolean {
        return when {
            user.firstname.isEmpty() -> {
                showToast(getString(R.string.please_provide_first_name))
                false
            }

            user.lastname.isEmpty() -> {
                showToast(getString(R.string.please_provide_lastname))
                false
            }

            user.country.isEmpty() -> {
                showToast(getString(R.string.please_provide_country))
                false
            }

            user.city.isEmpty() -> {
                showToast(getString(R.string.please_provide_city))
                false
            }

            else -> true
        }
    }

    private fun processForm() {
        val loadingDialog = LoadingDialog(requireContext())

        try {
            val sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            val profileId = sharedPreferences.getString("profile_id", "")

            val user = getDataFromForm()

            if (isValidForm(user)) {
                loadingDialog.show()

                if (profileId != null) {
                    updateExistingUser(
                        profileId = profileId,
                        hashMapOf(
                            "firstname" to user.firstname,
                            "lastname" to user.lastname,
                            "middleName" to user.middleName,
                            "headline" to user.headline,
                            "country" to user.country,
                            "city" to user.city
                        )
                    ) { success, error ->
                        if (success) {
                            onSave.invoke()
                            dismiss()
                        } else {
                            showToast(error.toString())
                        }

                        loadingDialog.dismiss()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("An error occurred.")
        }
    }

    private fun getDataFromForm(): User {
        return User(
            firstname = binding.firstNameTextField.text.toString().trim(),
            lastname = binding.lastNameTextField.text.toString().trim(),
            middleName = binding.additionalNameTextField.text.toString().trim(),
            headline = binding.headlineTextField.text.toString().trim(),
            country = binding.countryTextField.text.toString().trim(),
            city = binding.cityTextField.text.toString().trim()
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}