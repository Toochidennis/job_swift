package com.toochi.job_swift.model

import com.google.firebase.firestore.FieldValue

data class User(
    var profileId: String = "",
    var email: String = "",
    var password: String = "",
    var firstname: String = "",
    var middleName: String = "",
    var lastname: String = "",
    var phoneNumber: String = "",
    var headline: String = "",
    var about: String = "",
    var skillsId: String = "",
    var skills: String = "",
    var country: String = "",
    var address: String = "",
    var dateOfBirth: String = "",
    var city: String = "",
    var userType: String = "",
    var token: String = "",
    var profilePhotoUrl: String = "",
    val regDate: FieldValue = FieldValue.serverTimestamp()
)
