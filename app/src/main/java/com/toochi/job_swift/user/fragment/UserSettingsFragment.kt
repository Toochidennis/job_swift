package com.toochi.job_swift.user.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.toochi.job_swift.databinding.FragmentUserSettingsBinding
import com.toochi.job_swift.user.activity.PersonalInformationActivity


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class UserSettingsFragment : Fragment() {

    private lateinit var binding: FragmentUserSettingsBinding

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

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
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserSettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}