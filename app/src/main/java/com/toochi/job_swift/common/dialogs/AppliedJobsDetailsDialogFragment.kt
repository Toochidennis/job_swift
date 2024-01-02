package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.FragmentAppliedJobsDetailsBinding
import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.util.Constants.Companion.ACCEPTED
import com.toochi.job_swift.util.Constants.Companion.PENDING
import com.toochi.job_swift.util.Constants.Companion.REJECTED

class AppliedJobsDetailsDialogFragment(private val applyJob: ApplyJob) :
    BottomSheetDialogFragment() {

    private var _binding: FragmentAppliedJobsDetailsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppliedJobsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.statusTextView.text = when (applyJob.status) {
            ACCEPTED -> ACCEPTED
            REJECTED -> REJECTED
            else -> PENDING
        }

        setDrawable()
    }

    private fun setDrawable() {
        binding.statusTextView
            .setCompoundDrawablesRelativeWithIntrinsicBounds(
                when (applyJob.status) {
                    ACCEPTED -> R.drawable.ic_checkmark
                    REJECTED -> R.drawable.ic_block
                    else -> R.drawable.ic_pending_actions
                }.let {
                    ContextCompat.getDrawable(requireContext(), it)
                },
                null,
                null,
                null
            )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}