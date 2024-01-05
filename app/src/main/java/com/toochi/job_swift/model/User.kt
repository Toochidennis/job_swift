package com.toochi.job_swift.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    var profileId: String = "",
    var userId:String ="",
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
    @ServerTimestamp
    val regDate: Date = Date()
)
