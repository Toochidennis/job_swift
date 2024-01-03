package com.toochi.job_swift.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Notification(
    val notificationId: String = "",
    var token: String = "",
    val title: String = "",
    val body: String = "",
    var userId: String = "",
    val employerId: String = "",
    val jobId: String = "",
    val type: String = "",
    val comments: String = "",
    val adminId: String = "",
    @ServerTimestamp
    val notificationDate: Date = Date()
) {
    fun extractTime(): String {
        // Format the notificationDate to display only the time
        val timeFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
        return timeFormat.format(notificationDate)
    }
}
