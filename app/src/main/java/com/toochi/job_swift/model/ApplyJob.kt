package com.toochi.job_swift.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ApplyJob(
    val applicationId: String = "",
    val userId: String = "",
    val ownerId: String = "",
    val jobId: String = "",
    val status: String = "Pending",
    var cvURl: String = "",
    @ServerTimestamp
    val applicationDate: Date = Date()
) {
    fun calculateDaysAgo(): String {
        val currentDate = Date()

        // Calculate the difference in milliseconds
        val diffMillis = currentDate.time - applicationDate.time

        // Calculate days
        val days = diffMillis / (24 * 60 * 60 * 1000)

        // Format the result based on the number of days
        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7L -> "$days days ago"
            else -> "$days days ago" // You can customize this further if needed
        }
    }
}
