package com.toochi.job_swift.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.toochi.job_swift.databinding.FragmentUserJobsBinding
import com.toochi.job_swift.user.adapters.FragmentAdapter


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class UserJobsFragment : Fragment() {

    private var _binding: FragmentUserJobsBinding? = null

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
        _binding = FragmentUserJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply {
            title = "My jobs"
        }

        binding.postJobButton.setOnClickListener {
            JobIntroScreenDialogFragment().show(parentFragmentManager, "Intro")
        }

        setUpViewPager()
    }

    private fun setUpViewPager() {
        val fragmentTitles = listOf("Posted jobs", "Applied jobs", "Saved jobs")

        val fragmentAdapter = FragmentAdapter(requireActivity()).apply {
            addFragment(PostedJobsFragment())
            addFragment(AppliedJobsFragment())
            addFragment(SavedJobsFragment())
        }

        binding.viewPager.adapter = fragmentAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserJobsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}