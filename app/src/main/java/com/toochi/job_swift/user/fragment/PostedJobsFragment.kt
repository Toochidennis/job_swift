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
import com.toochi.job_swift.backend.PostJobManager.getPostedJobsByUserId
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentPostedJobsBinding
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.util.Utils.currencyFormatter
import java.util.Locale


class PostedJobsFragment : Fragment() {

    private var _binding: FragmentPostedJobsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPostedJobsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getPostedJobs()

        refreshData()
    }

    private fun getPostedJobs() {
        val loadingDialog = LoadingDialog(requireContext())

        try {
            loadingDialog.show()

            getPostedJobsByUserId { postJobs, error ->
                binding.imageView.isVisible = postJobs.isNullOrEmpty()
                binding.messageTextView.isVisible = postJobs.isNullOrEmpty()

                if (postJobs != null) {
                    loadingDialog.dismiss()

                    postJobs.forEach { item ->
                        val currentAmount = item.salary
                        val currentRate = item.salaryRate

                        val formattedAmount = formatAmount(currentAmount, currentRate)

                        item.salary = formattedAmount
                    }

                    setUpAdapter(postJobs)
                } else if (error == "empty") {
                    loadingDialog.dismiss()
                    binding.imageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_no_data
                        )
                    )
                    binding.messageTextView.text =
                        requireActivity().getString(R.string.have_not_posted_job)
                } else {
                    loadingDialog.dismiss()
                    binding.imageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.no_internet
                        )
                    )
                    binding.messageTextView.text = requireActivity().getString(R.string.no_internet)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
           Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_SHORT).show()
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
            R.layout.item_posted_jobs,
            bindItem = { binding, model ->
                binding.setVariable(BR.postJob, model)
                binding.executePendingBindings()
            }
        ) {}

        binding.postedJobsRecyclerView.apply {
            hasFixedSize()
            adapter = jobsAdapter
        }
    }

    private fun refreshData(){
        binding.swipeRefreshLayout.setOnRefreshListener {
            getPostedJobs()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}