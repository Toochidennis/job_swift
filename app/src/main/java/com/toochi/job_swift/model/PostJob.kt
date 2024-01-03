package com.toochi.job_swift.model

import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class PostJob(
    var jobId: String = "",
    val userId: String = "",
    val title: String = "",
    val company: String = "",
    var location: String = "",
    val workplaceType: String = "",
    var jobType: String = "",
    var description: String = "",
    var jobEmail: String = "",
    var isProvideCV: Boolean = false,
    var deadline: String = "",
    var salary: String = "",
    var salaryRate: String = "",
    @ServerTimestamp
    var datePosted: Date = Date()
) {
    fun calculateDaysAgo(): String {
        val currentDate = Date()

        // Calculate the difference in milliseconds
        val diffMillis = currentDate.time - datePosted.time

        // Calculate days
        val days = diffMillis / (24 * 60 * 60 * 1000)

        // Format the result based on the number of days
        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7L -> "$days days ago"
            else -> {
                val calendar = Calendar.getInstance()
                calendar.time = datePosted

                // If more than 7 days ago, show the actual date
                val dateFormat = SimpleDateFormat("d MMM, yy", Locale.getDefault())
                dateFormat.format(datePosted)
            }
        }
    }
}
