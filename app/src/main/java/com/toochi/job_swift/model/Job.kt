package com.toochi.job_swift.model

data class Job(
    var jobId: String = "",
    val userId: String = "",
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val workplaceType: String = "",
    val jobType: String = "",
    var description: String = "",
    var jobEmail: String = "",
    var isProvideCV: Boolean = false,
    var deadline: String = "",
    var salary: String = "",
    var salaryRate: String = ""
)