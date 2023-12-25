package com.toochi.job_swift.common.fragments

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.FragmentRequirementsBinding


private const val ARG_PARAM1 = "param1"


class RequirementsFragment : Fragment() {

    private var _binding: FragmentRequirementsBinding? = null

    private val binding get() = _binding!!

    private var requirementText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            requirementText = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRequirementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.requirementTxt.text = Html.fromHtml(requirementText, Html.FROM_HTML_MODE_COMPACT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(requirement: String) =
            RequirementsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, requirement)
                }
            }
    }
}