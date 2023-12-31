package com.toochi.job_swift.model

import com.google.firebase.firestore.FieldValue

data class Notification(
    val notificationId: String = "",
    val token: String = "",
    val title: String = "",
    val body: String = "",
    val userId: String = "",
    val ownerId: String = "",
    val jobId: String = "",
    val notificationDate: FieldValue = FieldValue.serverTimestamp()
)
