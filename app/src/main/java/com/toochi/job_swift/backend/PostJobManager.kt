package com.toochi.job_swift.backend

import com.google.firebase.firestore.QuerySnapshot
import com.toochi.job_swift.backend.AuthenticationManager.auth
import com.toochi.job_swift.backend.AuthenticationManager.usersCollection
import com.toochi.job_swift.model.PostJob

object PostJobManager {

    fun postJob(postJob: PostJob, onComplete: (Boolean, String?) -> Unit) {
        // Get the current user
        val currentUser = auth.currentUser

        // Reference to the "users" collection
        val usersDocument = currentUser?.let { usersCollection.document(it.uid) }

        // Reference to the "postedJobs" collection under the user's document
        val postedJobsCollection = usersDocument?.collection("postedJobs")

        // Add the postJob details to the collection
        postedJobsCollection?.add(postJob)
            ?.addOnSuccessListener {
                onComplete.invoke(true, null)
            }
            ?.addOnFailureListener { error ->
                onComplete.invoke(false, error.message)
            }
    }


    fun getAllPostedJobs(onComplete: (MutableList<PostJob>?, String?) -> Unit) {
        // Reference to the "postedJobs" collection across all users
        val postedJobsCollection = FirestoreDB.instance.collectionGroup("postedJobs")

        postedJobsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                handlePostedJobsQuerySnapshot(querySnapshot, onComplete)
            }
            .addOnFailureListener { error ->
                onComplete.invoke(null, error.message)
            }
    }

    private fun handlePostedJobsQuerySnapshot(
        querySnapshot: QuerySnapshot,
        onComplete: (MutableList<PostJob>?, String?) -> Unit
    ) {
        val allPostJobs = mutableListOf<PostJob>()

        for (document in querySnapshot) {
            // Extract postJob details from the query snapshot
            val postJob = document.toObject(PostJob::class.java).apply {
                jobId = document.id
            }
            allPostJobs.add(postJob)
        }

        onComplete.invoke(allPostJobs, null)
    }

    fun getPostedJobsByUserId(onComplete: (MutableList<PostJob>?, String?) -> Unit) {
        // Get the current user
        val currentUser = auth.currentUser

        // Reference to the "users" collection
        val usersDocument = currentUser?.let { usersCollection.document(it.uid) }

        // Reference to the "postedJobs" collection under the user's document
        val postedJobsCollection = usersDocument?.collection("postedJobs")

        postedJobsCollection?.get()
            ?.addOnSuccessListener { querySnapshot ->
                handlePostedJobsByUserIdQuerySnapshot(
                    querySnapshot,
                    onComplete
                )
            }
            ?.addOnFailureListener { error ->
                onComplete.invoke(null, error.message)
            }
    }

    private fun handlePostedJobsByUserIdQuerySnapshot(
        querySnapshot: QuerySnapshot,
        onComplete: (MutableList<PostJob>?, String?) -> Unit
    ) {
        if (querySnapshot.isEmpty) {
            onComplete.invoke(null, "empty")
        } else {
            val allPostJobs = mutableListOf<PostJob>()

            for (document in querySnapshot.documents) {
                // Extract postJob details from the query snapshot
                val postJob = document.toObject(PostJob::class.java)

                if (postJob != null) {
                    postJob.jobId = document.id
                    allPostJobs.add(postJob)
                }
            }

            onComplete.invoke(allPostJobs, null)
        }
    }

    fun checkIfOwnerOfJob(jobId: String, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser

        // Reference to the "users" collection
        val usersDocument = currentUser?.let { usersCollection.document(it.uid) }

        // Reference to the "postedJobs" collection under the user's document
        val postedJobsCollection = usersDocument?.collection("postedJobs")?.document(jobId)

        postedJobsCollection?.get()
            ?.addOnSuccessListener {
                if (it.exists()) {
                    onComplete(true, null)
                } else {
                    onComplete(false, null)
                }
            }
            ?.addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }
}