package com.toochi.job_swift.common.fragments

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.common.dialogs.DatePickerDialogFragment
import com.toochi.job_swift.databinding.FragmentJobDescriptionDialogBinding
import com.toochi.job_swift.user.fragment.DescriptionDialogFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class JobDescriptionDialogFragment : DialogFragment() {

    private var _binding: FragmentJobDescriptionDialogBinding? = null

    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null
    private var descriptionText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog2)

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
        _binding = FragmentJobDescriptionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.descriptionButton.setOnClickListener {
            DescriptionDialogFragment({
                descriptionText = it
                binding.descriptionTxt.text = Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
            }, descriptionText.toString()).show(parentFragmentManager, "")
        }

        binding.continueButton.setOnClickListener {
            PayDialogFragment().show(parentFragmentManager, "Pay")
        }

        binding.deadlineEditText.setOnClickListener {
            DatePickerDialogFragment().show(parentFragmentManager, "deadline")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JobDescriptionDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}