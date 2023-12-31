package com.toochi.job_swift.model

import com.google.firebase.firestore.FieldValue

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
    var datePosted: FieldValue = FieldValue.serverTimestamp()
)