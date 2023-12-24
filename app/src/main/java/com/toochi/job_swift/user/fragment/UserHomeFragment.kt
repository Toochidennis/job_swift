package com.toochi.job_swift.user.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import com.toochi.job_swift.databinding.FragmentUserHomeBinding
import com.toochi.job_swift.user.activity.PersonalInformationActivity


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpProfile()
    }

    private fun setUpProfile() {
        with(requireActivity().getSharedPreferences("loginDetail", MODE_PRIVATE)) {
            val firstname = getString("firstname", "")
            val lastname = getString("lastname", "")
            val photoUrl = getString("photo_url", "")

            val fullName = "$firstname $lastname"

            binding.userNameTextView.text = fullName

            Picasso.get().load(photoUrl).into(binding.userImageView)
        }

        binding.profileButton.setOnClickListener {
            startActivity(Intent(requireActivity(), PersonalInformationActivity::class.java))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}