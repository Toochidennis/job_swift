package com.toochi.job_swift.user.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.toochi.job_swift.common.activities.LoginActivity
import com.toochi.job_swift.databinding.FragmentUserSettingsBinding
import com.toochi.job_swift.user.activity.PersonalInformationActivity
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME


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
            requireContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
    }

}