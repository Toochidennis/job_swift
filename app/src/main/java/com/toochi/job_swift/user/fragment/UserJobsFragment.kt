package com.toochi.job_swift.user.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.toochi.job_swift.databinding.FragmentUserJobsBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class UserJobsFragment : Fragment() {

    private lateinit var binding: FragmentUserJobsBinding

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
        binding = FragmentUserJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.postJobButton.setOnClickListener {
            JobIntroScreenDialogFragment().show(parentFragmentManager, "Intro")
        }
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