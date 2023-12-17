package com.toochi.job_swift.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.common.fragments.CreateEmployerAccountDialogFragment
import com.toochi.job_swift.databinding.FragmentJobIntroScreenDialogBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class JobIntroScreenDialogFragment : DialogFragment() {

    private lateinit var binding:FragmentJobIntroScreenDialogBinding

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentJobIntroScreenDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueButton.setOnClickListener {
            CreateEmployerAccountDialogFragment().show(parentFragmentManager, "create")
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JobIntroScreenDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}