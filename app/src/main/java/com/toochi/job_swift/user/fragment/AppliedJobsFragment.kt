package com.toochi.job_swift.user.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.toochi.job_swift.BR
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.ApplyForJobManager.getJobsAppliedFor
import com.toochi.job_swift.common.dialogs.AppliedJobsDetailsDialogFragment
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentAppliedJobsBinding
import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.util.Utils.currencyFormatter
import java.util.Locale

class AppliedJobsFragment : Fragment() {

    private var _binding: FragmentAppliedJobsBinding? = null
    private val binding get() = _binding!!

    private var appliedJobList = mutableListOf<ApplyJob>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAppliedJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getAllAppliedJobs()

        refreshData()
    }


    private fun getAllAppliedJobs() {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()

        try {

            getJobsAppliedFor { postJobs, appliedJobs, error ->
                binding.imageView.isVisible = postJobs.isNullOrEmpty()
                binding.messageTextView.isVisible = postJobs.isNullOrEmpty()

                if (postJobs != null && appliedJobs != null) {
                    appliedJobList = appliedJobs

                    postJobs.forEach { item ->
                        val currentAmount = item.salary
                        val currentRate = item.salaryRate

                        val formattedAmount = formatAmount(currentAmount, currentRate)

                        item.salary = formattedAmount
                    }

                    setUpAdapter(postJobs)
                } else if (error == "empty") {
                    binding.imageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_no_data
                        )
                    )
                    binding.messageTextView.text =
                        requireActivity().getString(R.string.have_not_applied_job)
                } else {
                    binding.imageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.no_internet
                        )
                    )
                    binding.messageTextView.text = requireActivity().getString(R.string.no_internet)
                }

                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "An error occurred.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun formatAmount(amount: String, amountRate: String): String {
        val rate = if (amountRate == "Per month") {
            "m"
        } else {
            "yr"
        }

        return String.format(
            Locale.getDefault(),
            "%s%s/%s",
            requireActivity().getString(R.string.naira),
            currencyFormatter(amount.toDouble()),
            rate
        )
    }


    private fun setUpAdapter(jobList: MutableList<PostJob>) {
        val jobsAdapter = GenericAdapter(
            jobList,
            R.layout.item_applied_jobs,
            bindItem = { binding, model ->
                binding.setVariable(BR.postJob, model)
                binding.executePendingBindings()
            }
        ) {
            AppliedJobsDetailsDialogFragment(appliedJobList[it]).show(
                parentFragmentManager,
                "details"
            )
        }

        binding.appliedJobsRecyclerView.apply {
            hasFixedSize()
            adapter = jobsAdapter
        }
    }

    private fun refreshData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getAllAppliedJobs()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}