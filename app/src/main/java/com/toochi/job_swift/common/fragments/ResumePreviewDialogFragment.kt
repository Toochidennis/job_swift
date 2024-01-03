package com.toochi.job_swift.common.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.FragmentResumePreviewDialogBinding


class ResumePreviewDialogFragment(private val pdfUrl: String) : DialogFragment() {

    private var _binding: FragmentResumePreviewDialogBinding? = null

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
        _binding = FragmentResumePreviewDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        actionBar?.apply {
            title = "CV preview"
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        binding.pdfView.initWithUrl(
            url = pdfUrl,
            lifecycleCoroutineScope = lifecycleScope,
            lifecycle = lifecycle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}