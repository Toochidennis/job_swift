package com.toochi.job_swift.common.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.common.dialogs.NumberOfEmployeesDialogFragment
import com.toochi.job_swift.databinding.FragmentCreateEmployerAccountDialogBinding
import com.toochi.job_swift.user.fragment.DescriptionDialogFragment


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateEmployerAccountDialogFragment : DialogFragment() {

    private var _binding: FragmentCreateEmployerAccountDialogBinding? = null

    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

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
        _binding = FragmentCreateEmployerAccountDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewClickListener()
    }


    private fun getDataFromViews() {
        val companyName = binding.companyNameEditText.text.toString().trim()
        val noOfEmployees = binding.numberOfEmployeesEditText.text.toString()
        val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
        val description = binding.descriptionTxt.text.toString()
        val regNo = binding.cacIdEditText.text.toString().trim()

        println("Name: $companyName No: $phoneNumber  employ: $noOfEmployees des: $description  reg: $regNo")
    }

    private fun viewClickListener() {
        binding.numberOfEmployeesEditText.setOnClickListener {
            NumberOfEmployeesDialogFragment { selectedText ->
                binding.numberOfEmployeesEditText.setText(selectedText)
            }.show(parentFragmentManager, "Number of employees")
        }

        binding.descriptionButton.setOnClickListener {
            DescriptionDialogFragment({ descriptionText ->
                binding.descriptionTxt.text = descriptionText
            }).show(parentFragmentManager, "description")
        }


        binding.navigateUp.setOnClickListener {
            dismiss()
        }

        binding.continueButton.setOnClickListener {
            getDataFromViews()
            AddJobBasicsDialogFragment().show(parentFragmentManager, "job basics")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateEmployerAccountDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}