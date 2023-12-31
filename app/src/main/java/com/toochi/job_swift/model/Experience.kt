package com.toochi.job_swift.model

data class Experience(
    var experienceId: String = "",
    val jobTitle: String = "",
    val jobType: String = "",
    var companyName: String = "",
    var location: String = "",
    val workplace: String = "",
    var startDate: String = "",
    val endDate: String = ""
)