package com.toochi.job_swift.common.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.toochi.job_swift.R
import com.toochi.job_swift.common.activities.LoginActivity


class ThirdScreenFragment : Fragment(R.layout.fragment_third_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ExtendedFloatingActionButton>(R.id.finishButton).setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
           requireActivity().finish()
        }
    }
}