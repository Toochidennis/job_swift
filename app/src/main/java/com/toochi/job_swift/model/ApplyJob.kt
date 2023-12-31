package com.toochi.job_swift.model

import com.google.firebase.firestore.FieldValue

data class ApplyJob(
    val applicationId: String = "",
    val userId: String = "",
    val ownerId: String = "",
    val jobId: String = "",
    val status: String = "Pending",
    var cvURl: String = "",
    val applicationDate: FieldValue = FieldValue.serverTimestamp()
)
