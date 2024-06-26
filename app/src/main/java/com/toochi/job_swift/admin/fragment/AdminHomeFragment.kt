package com.toochi.job_swift.admin.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import com.toochi.job_swift.BR
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.AuthenticationManager.getGoogleSignInClient
import com.toochi.job_swift.backend.PostJobManager
import com.toochi.job_swift.common.activities.LoginActivity
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.dialogs.OnDeleteJobBottomDialog
import com.toochi.job_swift.databinding.FragmentAdminHomeBinding
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.util.Constants
import com.toochi.job_swift.util.Constants.Companion.DELETE_JOB
import com.toochi.job_swift.util.Constants.Companion.PREF_NAME
import com.toochi.job_swift.util.Constants.Companion.SIGN_OUT
import com.toochi.job_swift.util.Utils
import java.util.Calendar


class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null

    private val binding get() = _binding!!
    private val picasso = Picasso.get()

    private var jobList = mutableListOf<PostJob>()

    private lateinit var sharedPreferences: SharedPreferences
    private var userId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
    }

    private fun initData() {
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        setUpProfile()

        getAllJobs()

        refreshData()

        signOut()
    }

    private fun signOut() {
        binding.profileButton.setOnClickListener {
            PopupMenu(requireContext(), it, Gravity.START).run {
                inflate(R.menu.menu_admin_sign_out)

                setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.signOutButton) {
                        sharedPreferences.edit().clear().apply()
                        sharedPreferences.edit().putString("user_type", SIGN_OUT).apply()

                        getGoogleSignInClient(requireContext()).signOut()

                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finish()
                    }
                    false
                }

                show()
            }
        }
    }

    private fun setUpProfile() {
        with(sharedPreferences) {
            val firstname = getString("firstname", "")
            val lastname = getString("lastname", "")
            val photoUrl = getString("photo_url", "")
            userId = getString(Constants.USER_ID_KEY, "")

            val fullName = "$firstname $lastname"

            binding.userNameTextView.text = fullName
            binding.greetingsTextView.text = getGreetingMessage()

            if (!photoUrl.isNullOrEmpty()) {
                picasso.load(photoUrl).into(binding.userImageView)
            }
        }
    }

    private fun getAllJobs() {
        val loadingDialog = LoadingDialog(requireContext())

        try {
            loadingDialog.show()
            jobList.clear()

            PostJobManager.getAllPostedJobs { jobs, errorMessage ->
                if (jobs != null) {
                    jobList.addAll(jobs)

                    val copiedJobs = jobs.map { job ->
                        val location =
                            "${job.location} . ${formatAmount(job.salary, job.salaryRate)}"
                        val jobType = "${job.jobType} . ${job.workplaceType}"

                        PostJob(
                            userId = job.userId,
                            title = job.title,
                            location = location,
                            jobType = jobType,
                            jobEmail = job.calculateDaysAgo(),
                            companyPhotoUrl = job.companyPhotoUrl
                        )
                    }

                    loadingDialog.dismiss()
                    binding.errorTextView.isVisible = false
                    binding.noDataImageView.isVisible = false
                    setUpAdapter(copiedJobs.toMutableList())
                } else if (errorMessage == Constants.NOT_AVAILABLE) {
                    loadingDialog.dismiss()
                    binding.errorTextView.isVisible = true
                    binding.noDataImageView.isVisible = true
                } else {
                    loadingDialog.dismiss()
                    binding.noDataImageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.no_internet
                        )
                    )
                    binding.noDataImageView.isVisible = true
                    binding.errorTextView.isVisible = false
                    showToast(errorMessage.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast("An error occurred.")
        }
    }

    private fun formatAmount(amount: String, rate: String): String {
        val formatAmount =
            "${getString(R.string.naira)} ${Utils.currencyFormatter(amount.toDouble())}"
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

                val saveButton: ImageView = binding.root.findViewById(R.id.saveButton)
                saveButton.isVisible = model.userId != userId

                val imageView: ImageView = binding.root.findViewById(R.id.companyImageView)

                if (model.companyPhotoUrl.isNotEmpty()) {
                    picasso.load(model.companyPhotoUrl).into(imageView)
                }

            }
        ) { position ->
            OnDeleteJobBottomDialog(jobList[position])
                .show(parentFragmentManager, DELETE_JOB)
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

    private fun refreshData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getAllJobs()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}