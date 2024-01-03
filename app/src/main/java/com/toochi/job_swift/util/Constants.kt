package com.toochi.job_swift.util

class Constants {

    companion object {
        const val SCOPES = "https://www.googleapis.com/auth/firebase.messaging"
        const val BASE_URL = "https://fcm.googleapis.com/v1/projects/jobswift-d3781/messages:send"
        const val CONTENT_TYPE = "application/json"
        const val FILE_TYPE = "application/pdf"
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
        const val PDF_STORAGE_PATH = "pdfs"
        const val IMAGE_STORAGE_PATH = "images"
        const val PERMISSION_REQUESTED_CODE = 100
        const val JOB_APPLICATION = "Job application"
        const val REPORT = "Report"
        const val JOBS_APPLIED_FOR = "jobsAppliedFor"
        const val JOBS_APPLIED_BY_OTHERS = "jobsAppliedByOthers"
        const val PERSONAL_DETAILS = "personalDetails"
        const val PENDING = "Pending"
        const val REJECTED = "Rejected"
        const val ACCEPTED = "Accepted"
        const val NOT_AVAILABLE = "Not available"
        const val POSTED_JOBS = "postedJobs"
        const val VIEW_JOB = "View job"
        const val DELETE_JOB = "Delete job"
    }
}