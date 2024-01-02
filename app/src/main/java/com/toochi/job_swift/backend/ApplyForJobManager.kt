package com.toochi.job_swift.backend

import android.net.Uri
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.StorageReference
import com.toochi.job_swift.backend.AuthenticationManager.auth
import com.toochi.job_swift.backend.AuthenticationManager.storageRef
import com.toochi.job_swift.backend.AuthenticationManager.usersCollection
import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.util.Constants

object ApplyForJobManager {

    private var completedCount = 0

    fun applyJob(
        userId: String = "",
        applyJob: ApplyJob,
        cvUri: Uri?,
        cvName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (userId.isEmpty()) {
            val currentUser = auth.currentUser
            val usersDocument = currentUser?.let {
                usersCollection.document(it.uid)
            }

            cvUri?.let { uri ->
                uploadCVAndApplyJob(
                    Constants.JOBS_APPLIED_FOR,
                    usersDocument,
                    applyJob,
                    uri,
                    cvName,
                    onComplete
                )
            } ?: applyJobWithoutCV(Constants.JOBS_APPLIED_FOR, usersDocument, applyJob, onComplete)
        } else {
            val usersDocument = usersCollection.document(userId)
            cvUri?.let { uri ->
                uploadCVAndApplyJob(
                    Constants.JOBS_APPLIED_BY_OTHERS,
                    usersDocument,
                    applyJob,
                    uri,
                    cvName,
                    onComplete
                )
            } ?: applyJobWithoutCV(Constants.JOBS_APPLIED_BY_OTHERS, usersDocument, applyJob, onComplete)
        }
    }


    private fun uploadCVAndApplyJob(
        collectionPath: String,
        usersDocument: DocumentReference?,
        applyJob: ApplyJob,
        cvUri: Uri,
        cvName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val cvRef = storageRef.child("${Constants.PDF_STORAGE_PATH}/$cvName")

        cvRef.putFile(cvUri)
            .addOnSuccessListener {
                handleCVUploadSuccess(collectionPath, cvRef, applyJob, usersDocument, onComplete)
            }
            .addOnFailureListener { exception ->
                onComplete.invoke(false, exception.message)
            }
    }

    private fun handleCVUploadSuccess(
        collectionPath: String,
        cvRef: StorageReference,
        applyJob: ApplyJob,
        usersDocument: DocumentReference?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        cvRef.downloadUrl.addOnSuccessListener { uri ->
            applyJob.cvURl = uri.toString()

            applyJob(collectionPath, usersDocument, applyJob, onComplete)
        }.addOnFailureListener { error ->
            onComplete.invoke(false, error.message)
        }
    }

    private fun applyJob(
        collectionPath: String,
        usersDocument: DocumentReference?,
        applyJob: ApplyJob,
        onComplete: (Boolean, String?) -> Unit
    ) {
        usersDocument?.collection(collectionPath)
            ?.add(applyJob)
            ?.addOnSuccessListener {
                onComplete.invoke(true, null)
            }
            ?.addOnFailureListener { exception ->
                onComplete.invoke(false, exception.message)
            }
    }

    private fun applyJobWithoutCV(
        collectionPath: String,
        usersDocument: DocumentReference?,
        applyJob: ApplyJob,
        onComplete: (Boolean, String?) -> Unit
    ) {
        applyJob(collectionPath, usersDocument, applyJob, onComplete)
    }

    fun checkIfHaveAppliedJob(jobId: String, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser

        // Reference to the "users" collection
        val usersDocument = currentUser?.let { usersCollection.document(it.uid) }

        // Reference to the "postedJobs" collection under the user's document
        val postedJobsCollection = usersDocument?.collection("jobsAppliedFor")

        postedJobsCollection?.whereEqualTo("jobId", jobId)
            ?.get()
            ?.addOnSuccessListener {
                if (!it.isEmpty) {
                    onComplete(true, null)
                } else {
                    onComplete(false, null)
                }
            }

            ?.addOnFailureListener {
                onComplete(false, it.message)
            }

    }


    fun updateJobsAppliedFor(
        userId: String = "",
        applicationId: String,
        data: HashMap<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

        val appliedJobsDocument = if (userId.isEmpty()) {
            usersDocument?.collection(Constants.JOBS_APPLIED_FOR)?.document(applicationId)
        } else {
            usersCollection.document(userId).collection(Constants.JOBS_APPLIED_FOR)
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

    fun getJobsAppliedFor(onComplete: (MutableList<PostJob>?, MutableList<ApplyJob>?, String?) -> Unit) {
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

        usersDocument?.let { userDoc ->
            userDoc.collection("jobsAppliedFor")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    handleAppliedJobsQuerySnapshot(querySnapshot, onComplete)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(null, null, error.message)
                }
        }
    }

    private fun handleAppliedJobsQuerySnapshot(
        querySnapshot: QuerySnapshot,
        onComplete: (MutableList<PostJob>?, MutableList<ApplyJob>?, String?) -> Unit
    ) {
        if (!querySnapshot.isEmpty) {
            val allPostJobs = mutableListOf<PostJob>()
            val allAppliedJobs = querySnapshot.toObjects(ApplyJob::class.java)

            querySnapshot.documents.forEach { document ->
                val jobId = document.getString("jobId")
                val employerId = document.getString("employerId")

                if (jobId != null && employerId != null) {
                    fetchPostedJob(jobId, employerId, allPostJobs, allAppliedJobs, onComplete)
                }
            }
        } else {
            onComplete.invoke(null, null, "empty")
        }
    }


    private fun fetchPostedJob(
        jobId: String,
        userId: String,
        allPostJobs: MutableList<PostJob>,
        allAppliedJobs: MutableList<ApplyJob>,
        onComplete: (MutableList<PostJob>?, MutableList<ApplyJob>?, String?) -> Unit
    ) {
        usersCollection.document(userId)
            .collection("postedJobs")
            .document(jobId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                handlePostedJobQuerySnapshot(
                    documentSnapshot,
                    allPostJobs,
                    allAppliedJobs,
                    onComplete
                )
            }
            .addOnFailureListener { error ->
                onComplete.invoke(null, null, error.message)
            }
    }

    private fun handlePostedJobQuerySnapshot(
        documentSnapshot: DocumentSnapshot,
        allPostJobs: MutableList<PostJob>,
        allAppliedJobs: MutableList<ApplyJob>,
        onComplete: (MutableList<PostJob>?, MutableList<ApplyJob>?, String?) -> Unit
    ) {
        val postJob = documentSnapshot.toObject(PostJob::class.java)

        if (postJob != null) {
            postJob.jobId = documentSnapshot.id
            allPostJobs.add(postJob)
        }

        completedCount++

        if (completedCount == allAppliedJobs.size) {
            onComplete.invoke(allPostJobs, allAppliedJobs, null)
        }
    }

    fun getJobsAppliedForById(
        jobId: String,
        employerId: String,
        onComplete: (ApplyJob?, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

        usersDocument?.let { userDoc ->
            userDoc.collection("jobsAppliedFor")
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("employerId", employerId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val applyJob =
                        querySnapshot.documents.firstOrNull()?.toObject(ApplyJob::class.java)
                    onComplete(applyJob, null)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(null, error.message)
                }
        }
    }

}