package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.textview.MaterialTextView
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.FragmentCompanyPositionDialogBinding
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.user.model.JobTitleModel

class CompanyPositionDialogFragment(private val onSelected: (String) -> Unit) : DialogFragment() {

    private var _binding: FragmentCompanyPositionDialogBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompanyPositionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        actionBar?.apply {
            title = "Position"
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.toolbar.setNavigationOnClickListener { dismiss() }

        setUpAdapter()
    }


    private fun setUpAdapter() {
        val jobTitleArray = resources.getStringArray(R.array.company_position)
        val jobTitleList = jobTitleArray.map { JobTitleModel(it) }.sortedBy { it.jobTitle }

        val adapter = GenericAdapter(
            itemList = jobTitleList.toMutableList(),
            itemResLayout = R.layout.item_company_position,
            bindItem = { itemView, model ->
                val positionNameTxt: MaterialTextView = itemView.findViewById(R.id.positionNameTxt)
                positionNameTxt.text = model.jobTitle
            }
        ) { position ->
            val selectedPosition = jobTitleList[position]

            onSelected.invoke(selectedPosition.jobTitle)
            dismiss()
        }

        binding.jobRecyclerView.apply {
            hasFixedSize()
            this.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}