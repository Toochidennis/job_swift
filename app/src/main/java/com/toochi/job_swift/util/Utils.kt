package com.toochi.job_swift.util

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationSet
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

object Utils {

    fun loadFragment(activity: AppCompatActivity, fragment: Fragment, container: Int) {
        activity.supportFragmentManager.commit {
            replace(container, fragment)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    fun View.applyElasticEffect(onAnimationEnd: (() -> Unit)? = null) {
        val animationSet = AnimationSet(true)

        // Scale animation with bounce interpolator
        val scaleAnimation = ScaleAnimation(
            1f, 0.8f, // Scale down
            1f, 0.8f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 400
        scaleAnimation.interpolator = BounceInterpolator()

        // Add the scale animation to the set
        animationSet.addAnimation(scaleAnimation)

        // Set listener to execute the provided function after the animation ends
        animationSet.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}

            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                // Invoke the provided function if not null
            }

            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })

        // Apply the animation set when the view is touched
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startAnimation(animationSet)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Handle release or cancel if needed
                    clearAnimation()
                    onAnimationEnd?.invoke()
                }
            }
            true
        }
    }
}