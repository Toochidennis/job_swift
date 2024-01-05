package com.toochi.job_swift.common.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toochi.job_swift.common.fragments.PostedJobDetailsDialogFragment
import com.toochi.job_swift.databinding.DialogOnDeleteJobBottomBinding
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.util.Constants.Companion.DELETE_JOB
import com.toochi.job_swift.util.Constants.Companion.POSTED_JOBS

class OnDeleteJobBottomDialog(private val postJob: PostJob) :
    BottomSheetDialogFragment() {

    private var _binding: DialogOnDeleteJobBottomBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogOnDeleteJobBottomBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewJobTextView.setOnClickListener {
            PostedJobDetailsDialogFragment(postJob)
                .show(
                    parentFragmentManager,
                    POSTED_JOBS
                )
            dismiss()
        }

        binding.deleteJobTextView.setOnClickListener {
            DeleteJobDialogFragment(postJob).show(
                parentFragmentManager,
                DELETE_JOB
            )
            dismiss()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}