package com.toochi.job_swift.admin.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.squareup.picasso.Picasso
import com.toochi.job_swift.BR
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.PersonalDetailsManager.getAllUsers
import com.toochi.job_swift.common.dialogs.LoadingDialog
import com.toochi.job_swift.databinding.FragmentAdminInsightsBinding
import com.toochi.job_swift.model.User
import com.toochi.job_swift.user.adapters.GenericAdapter
import com.toochi.job_swift.util.Constants.Companion.NOT_AVAILABLE


class AdminInsightsFragment : Fragment() {

    private var _binding: FragmentAdminInsightsBinding? = null

    private val binding get() = _binding!!

    private var picasso = Picasso.get()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdminInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar

        actionBar?.apply {
            title = "Insights"
        }

        getUsers()

        refreshData()
    }


    private fun getUsers() {
        val loadingDialog = LoadingDialog(requireContext())
        try {
            loadingDialog.show()
            getAllUsers { users, exception ->
                if (users != null) {
                    users.forEach { user ->
                        val name = "${user.firstname} ${user.lastname}"
                        user.firstname = name
                    }

                    setUpAdapter(users)
                    loadingDialog.dismiss()
                } else if (exception == NOT_AVAILABLE) {
                    loadingDialog.show()
                    binding.errorTextView.isVisible = true
                    binding.noDataImageView.isVisible = true
                } else {
                    loadingDialog.dismiss()
                    showToast(exception.toString())
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            loadingDialog.dismiss()
            showToast(e.message.toString())
        }
    }

    private fun setUpAdapter(userList: MutableList<User>) {
        val userAdapter = GenericAdapter(
            itemList = userList,
            itemResLayout = R.layout.item_admin_insights,
            bindItem = { binding, model ->
                binding.setVariable(BR.user, model)
                binding.executePendingBindings()

                val imageView: ImageView = binding.root.findViewById(R.id.userImageView)

                if (model.profilePhotoUrl.isNotEmpty()) {
                    picasso.load(model.profilePhotoUrl).into(imageView)
                }

            }
        ) {}

        binding.employeeRecyclerView.apply {
            hasFixedSize()
            adapter = userAdapter
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    private fun refreshData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getUsers()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}