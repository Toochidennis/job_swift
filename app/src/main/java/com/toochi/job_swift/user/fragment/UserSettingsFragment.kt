package com.toochi.job_swift.user.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.toochi.job_swift.backend.AuthenticationManager.getGoogleSignInClient
import com.toochi.job_swift.common.activities.LoginActivity
import com.toochi.job_swift.databinding.FragmentUserSettingsBinding
import com.toochi.job_swift.user.activity.PersonalInformationActivity
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Constants.Companion.SIGN_OUT


class UserSettingsFragment : Fragment() {

    private lateinit var binding: FragmentUserSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply {
            title = "Settings"
        }

        binding.personalInfoButton.setOnClickListener {
            startActivity(Intent(requireActivity(), PersonalInformationActivity::class.java))
        }

        binding.contactInfoButton.setOnClickListener {
            ContactInfoDialogFragment().show(parentFragmentManager, "contact")
        }

        binding.changePasswordButton.setOnClickListener {
            PasswordDialogFragment().show(parentFragmentManager, "password")
        }

        binding.companyInfoButton.setOnClickListener {
            CompanyInfoDialogFragment().show(parentFragmentManager, "company")
        }

        binding.signOutButton.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            sharedPreferences.edit().putString("user_type", SIGN_OUT).apply()

            getGoogleSignInClient(requireContext()).signOut()

            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

}