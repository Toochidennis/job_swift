package com.toochi.job_swift.backend

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreDB {
   val instance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
}