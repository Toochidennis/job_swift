package com.toochi.job_swift.common.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.toochi.job_swift.BR
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.NotificationsManager.getAllNotifications
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentNotificationsBinding
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.user.activity.ApplicationReviewActivity
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.util.Constants.Companion.ACCEPTED
import com.toochi.job_swift.util.Constants.Companion.JOB_APPLICATION
import com.toochi.job_swift.util.Constants.Companion.REJECTED


class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply {
            title = "Notifications"
        }

        fetchNotifications()
        refreshData()
    }

    private fun fetchNotifications() {
        val loadingDialog = LoadingDialog(requireContext())

        try {
            loadingDialog.show()

            getAllNotifications { notifications, error ->
                if (notifications != null) {
                    notifications.forEach {
                        it.userId = it.extractTime()
                    }
                    binding.errorImageView.isVisible = false
                    binding.errorTextView.isVisible = false

                    setUpAdapter(notifications)
                } else if (error == "No notifications yet") {
                    binding.errorImageView.isVisible = true
                    binding.errorTextView.isVisible = true
                } else {
                    binding.errorImageView.isVisible = true
                    binding.errorTextView.isVisible = false
                    Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_SHORT).show()
                }

                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "And error occurred.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpAdapter(notificationList: MutableList<Notification>) {
        val notificationAdapter = GenericAdapter(
            itemList = notificationList,
            itemResLayout = R.layout.item_notifications,
            bindItem = { binding, model ->
                binding.setVariable(BR.notification, model)
                binding.executePendingBindings()
            }
        ) { position ->
            val selectedPosition = notificationList[position]

            if (selectedPosition.type == JOB_APPLICATION) {
                launchReviewActivity(selectedPosition)
            } else if (selectedPosition.type == ACCEPTED || selectedPosition.type == REJECTED) {
                ApplicationResponseDialogFragment(selectedPosition).show(
                    parentFragmentManager,
                    "response"
                )
            }

        }

        binding.notificationRecyclerView.apply {
            hasFixedSize()
            adapter = notificationAdapter
        }
    }

    private fun launchReviewActivity(notification: Notification) {
        startActivity(Intent(requireActivity(), ApplicationReviewActivity::class.java).apply {
            putExtra("user_id", notification.userId)
            putExtra("job_id", notification.jobId)
        })
    }

    private fun refreshData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchNotifications()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}