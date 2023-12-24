package com.toochi.job_swift.util

import android.animation.ObjectAnimator
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

object Utils {

    fun loadFragment(activity: AppCompatActivity, fragment: Fragment, container: Int) {
        activity.supportFragmentManager.commit {
            replace(container, fragment)
        }
    }


}