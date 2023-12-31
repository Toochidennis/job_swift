package com.toochi.job_swift.util

class Constants {

    companion object {
        const val SCOPES = "https://www.googleapis.com/auth/firebase.messaging"
        const val BASE_URL = "https://fcm.googleapis.com/v1/projects/jobswift-d3781/messages:send"
        const val CONTENT_TYPE = "application/json"
        const val CHANNEL_ID = "swift_job"
        const val CHANNEL_NAME = "swift_job"
        const val SELECTED = "select"
        const val DESELECTED = "deselect"
        const val ADMIN = "admin"
        const val ADMIN_PASSWORD = "Admin@admin123"
        const val EMPLOYER = "employer"
        const val EMPLOYEE = "employee"
        const val PRESENT = "Present"
        const val PREF_NAME = "loginDetail"
        const val USER_ID_KEY = "user_id"
        const val EMAIL = "email"
        const val PHONE_NUMBER = "phone number"
        const val SIGN_OUT = "Sign out"
    }
}