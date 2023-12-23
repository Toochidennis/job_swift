package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.DialogFragment
import com.google.android.material.textview.MaterialTextView
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.FragmentJobTitleDialogBinding
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.user.model.JobTitleModel


class JobTitleDialogFragment(
    private val onSelected: (String) -> Unit
) : DialogFragment() {

    private var _binding: FragmentJobTitleDialogBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentJobTitleDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        actionBar?.apply {
            title = "Job title"
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        onMenuClicked()

        setUpAdapter()
    }

    private fun setUpAdapter() {
        val jobTitleArray = resources.getStringArray(R.array.job_title)
        val jobTitleList = jobTitleArray.map { JobTitleModel(it) }.sortedBy { it.jobTitle }

        val adapter = GenericAdapter(
            itemList = jobTitleList.toMutableList(),
            itemResLayout = R.layout.item_job_title,
            bindItem = { itemView, model ->
                val jobNameTxt: MaterialTextView = itemView.findViewById(R.id.jobNameTxt)
                jobNameTxt.text = model.jobTitle
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

    private fun onMenuClicked() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return if (menuItem.itemId == android.R.id.home) {
                    dismiss()
                    true
                } else {
                    false
                }
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}