package com.toochi.job_swift.model

data class User(
    val email: String,
    val password: String?,
    val firstname: String,
    val lastname: String,
    val userType: String,
    val profilePhotoUrl: String? =null
)
