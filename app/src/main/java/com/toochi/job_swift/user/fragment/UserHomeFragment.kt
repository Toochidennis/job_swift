package com.toochi.job_swift.user.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import com.toochi.job_swift.BR
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.getAllPostedJobs
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.fragments.JobDetailsDialogFragment
import com.toochi.job_swift.databinding.FragmentUserHomeBinding
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.user.activity.PersonalInformationActivity
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Utils.currencyFormatter
import java.util.Calendar


class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null

    private val binding get() = _binding!!

    private var jobList = mutableListOf<PostJob>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpProfile()

        getAllJobs()
    }

    private fun setUpProfile() {
        with(requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)) {
            val firstname = getString("firstname", "")
            val lastname = getString("lastname", "")
            val photoUrl = getString("photo_url", "")

            val fullName = "$firstname $lastname"

            binding.userNameTextView.text = fullName
            binding.greetingsTextView.text = getGreetingMessage()

            if (!photoUrl.isNullOrEmpty()) {
                Picasso.get().load(photoUrl).into(binding.userImageView)
            }
        }

        binding.profileButton.setOnClickListener {
            startActivity(Intent(requireActivity(), PersonalInformationActivity::class.java))
        }
    }

    private fun getAllJobs() {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()

        getAllPostedJobs { jobs, errorMessage ->
            if (jobs != null) {
                jobList = jobs

                jobs.forEach { job ->
                    val location = "${job.location} . ${formatAmount(job.salary, job.salaryRate)}"
                    val jobType = "${job.jobType} . ${job.workplaceType}"

                    job.location = location
                    job.jobType = jobType
                }

                setUpAdapter(jobs)
            } else {
                showToast(errorMessage.toString())
            }

            loadingDialog.dismiss()
        }
    }

    private fun formatAmount(amount: String, rate: String): String {
        val formatAmount = "${getString(R.string.naira)} ${currencyFormatter(amount.toDouble())}"
        return when (rate) {
            "Per month" -> "$formatAmount/m"
            else -> "$formatAmount/yr"
        }
    }

    private fun setUpAdapter(postJobList: MutableList<PostJob>) {
        val jobAdapter = GenericAdapter(
            postJobList,
            R.layout.item_user_home,
            bindItem = { binding, model ->
                binding.setVariable(BR.job, model)
                binding.executePendingBindings()
            }
        ) { position ->
            JobDetailsDialogFragment(jobList[position]).show(parentFragmentManager, "job details")
        }

        binding.jobRecyclerView.apply {
            hasFixedSize()
            adapter = jobAdapter
        }
    }

    private fun getGreetingMessage(): String {
        val calender = Calendar.getInstance()
        val hourOfDay = calender[Calendar.HOUR_OF_DAY]

        return when {
            hourOfDay < 12 -> getString(R.string.good_morning)
            hourOfDay < 18 -> getString(R.string.good_afternoon)
            else -> getString(R.string.good_evening)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}