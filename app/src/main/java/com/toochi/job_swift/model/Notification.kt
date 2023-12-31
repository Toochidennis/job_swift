package com.toochi.job_swift.model

data class Notification(
    val notificationId: String = "",
    val token: String = "",
    val title: String = "",
    val body: String = "",
    val userId: String = "",
    val ownerId: String = "",
    val jobId: String = ""
)
