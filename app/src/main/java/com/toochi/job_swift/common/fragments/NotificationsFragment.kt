package com.toochi.job_swift.common.fragments

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
import com.toochi.job_swift.backend.AuthenticationManager.getAllNotifications
import com.toochi.job_swift.databinding.FragmentNotificationsBinding
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.user.adapters.GenericAdapter


class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private var notificationsList = mutableListOf<Notification>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar =  (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply {
            title = "Notifications"
        }

    }

    private fun getNotifications() {
        getAllNotifications { notifications, error ->
            if (notifications != null) {
                notificationsList = notifications

                notifications.forEach {
                    it.userId = it.extractTime()
                }

                setUpAdapter(notifications)
            } else {
                binding.errorImageView.isVisible = true
                Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_SHORT).show()
            }
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

        }

        binding.notificationRecyclerView.apply {
            hasFixedSize()
            adapter = notificationAdapter
        }
    }

}