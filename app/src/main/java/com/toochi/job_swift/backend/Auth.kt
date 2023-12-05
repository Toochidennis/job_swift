package com.toochi.job_swift.backend

import com.google.firebase.auth.FirebaseAuth

object Auth {
    val instance: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
}