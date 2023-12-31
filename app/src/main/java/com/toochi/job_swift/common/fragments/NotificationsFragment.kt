package com.toochi.job_swift.common.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.toochi.job_swift.BR
import com.toochi.job_swift.R
import com.toochi.job_swift.databinding.FragmentNotificationsBinding
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.user.adapters.GenericAdapter


class NotificationsFragment : Fragment() {

    private lateinit var binding:FragmentNotificationsBinding


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

    }


    private fun setUpAdapter(notificationList: MutableList<Notification>){
        val notificationAdapter = GenericAdapter(
           itemList =  notificationList,
            itemResLayout = R.layout.item_notifications,
            bindItem = {binding, model ->
                //binding.setVariable(BR.notification, model)
            }
        ){

        }

    }

}