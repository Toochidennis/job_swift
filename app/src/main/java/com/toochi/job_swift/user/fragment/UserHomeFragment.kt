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
import com.toochi.job_swift.backend.AuthenticationManager.getPostedJobs
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.common.fragments.JobDetailsDialogFragment
import com.toochi.job_swift.databinding.FragmentUserHomeBinding
import com.toochi.job_swift.model.Job
import com.toochi.job_swift.user.activity.PersonalInformationActivity
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.util.Utils.getAccessToken
import timber.log.Timber


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpProfile()

        getAllJobs()

        getAccessToken(requireContext()) { token, errorMessage ->
            Timber.tag("token").d(token)
            println("$token, $errorMessage")
        }
    }

    private fun setUpProfile() {
        with(requireActivity().getSharedPreferences("loginDetail", MODE_PRIVATE)) {
            val firstname = getString("firstname", "")
            val lastname = getString("lastname", "")
            val photoUrl = getString("photo_url", "")

            val fullName = "$firstname $lastname"

            binding.userNameTextView.text = fullName

            Picasso.get().load(photoUrl).into(binding.userImageView)
        }

        binding.profileButton.setOnClickListener {
            startActivity(Intent(requireActivity(), PersonalInformationActivity::class.java))
        }
    }

    private fun getAllJobs() {
        val loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show()

        getPostedJobs { jobs, errorMessage ->
            if (jobs != null) {
                setUpAdapter(jobs)
            } else {
                showToast(errorMessage.toString())
            }

            loadingDialog.dismiss()
        }
    }

    private fun setUpAdapter(jobList: MutableList<Job>) {
        val jobAdapter = GenericAdapter(
            jobList,
            R.layout.item_user_home,
            bindItem = { binding, model ->
                binding.setVariable(BR.job, model)
                binding.executePendingBindings()
            }
        ) { position ->
            val selectedPosition = jobList[position]
            JobDetailsDialogFragment(selectedPosition).show(parentFragmentManager, "job details")
        }

        binding.jobRecyclerView.apply {
            hasFixedSize()
            adapter = jobAdapter
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}