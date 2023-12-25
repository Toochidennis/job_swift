package com.toochi.job_swift.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {

    fun loadFragment(activity: AppCompatActivity, fragment: Fragment, container: Int) {
        activity.supportFragmentManager.commit {
            replace(container, fragment)
        }
    }

    fun currencyFormatter(amount: Double): String {
        return DecimalFormat("#,###.##").format(amount)
    }

    fun dateFormatter(date: String, format: String = "default"): String {
        return try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parseDate = simpleDateFormat.parse(date)!!

            val sdf = when (format) {
                "default" -> SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                else -> SimpleDateFormat("dd, MMM", Locale.getDefault())
            }

            sdf.format(parseDate)

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


}