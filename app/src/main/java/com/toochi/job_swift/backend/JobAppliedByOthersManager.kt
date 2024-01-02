package com.toochi.job_swift.backend

import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.util.Constants

object JobAppliedByOthersManager {


    fun getJobsAppliedByOthers(
        jobId: String,
        userId: String,
        onComplete: (ApplyJob?, String?) -> Unit
    ) {
        val currentUser = AuthenticationManager.auth.currentUser
        val usersDocument = currentUser?.let {
            AuthenticationManager.usersCollection.document(it.uid)
        }

        val jobsDocument = usersDocument?.collection(Constants.JOBS_APPLIED_BY_OTHERS)

        jobsDocument?.let { collectionReference ->
            collectionReference
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapShot ->
                    val appliedJobs =
                        querySnapShot.documents.firstOrNull()?.toObject(ApplyJob::class.java)
                    appliedJobs?.applicationId =
                        querySnapShot.documents.firstOrNull()?.id.toString()

                    onComplete.invoke(appliedJobs, null)
                }
                .addOnFailureListener {
                    onComplete.invoke(null, it.message)
                }
        }
    }

    fun updateJobsAppliedByOthers(
        userId: String = "",
        applicationId: String,
        data: HashMap<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val currentUser = AuthenticationManager.auth.currentUser
        val usersDocument = currentUser?.let {
            AuthenticationManager.usersCollection.document(it.uid)
        }

        val appliedJobsDocument = if (userId.isEmpty()) {
            usersDocument?.collection(Constants.JOBS_APPLIED_BY_OTHERS)?.document(applicationId)
        } else {
            AuthenticationManager.usersCollection.document(userId).collection(Constants.JOBS_APPLIED_BY_OTHERS)
                .document(applicationId)
        }

        appliedJobsDocument
            ?.update(data)
            ?.addOnSuccessListener {
                onComplete.invoke(true, null)
            }
            ?.addOnFailureListener {
                onComplete.invoke(false, it.message)
            }
    }

}