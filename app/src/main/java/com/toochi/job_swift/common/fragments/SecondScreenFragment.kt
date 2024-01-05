package com.toochi.job_swift.common.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.toochi.job_swift.R


class SecondScreenFragment : Fragment(R.layout.fragment_second_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ExtendedFloatingActionButton>(R.id.nextButton).setOnClickListener {
            findNavController().navigate(R.id.action_secondScreenFragment_to_thirdScreenFragment)
        }
    }

}