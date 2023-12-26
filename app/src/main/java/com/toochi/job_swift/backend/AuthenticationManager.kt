package com.toochi.job_swift.backend

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.toochi.job_swift.R
import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.model.Company
import com.toochi.job_swift.model.Education
import com.toochi.job_swift.model.Experience
import com.toochi.job_swift.model.Job
import com.toochi.job_swift.model.User
import java.util.UUID

object AuthenticationManager {
    private val auth = UserAuth.instance

    private val currentUser = auth.currentUser
    private val userId = currentUser?.uid
    private val usersDocument =
        userId?.let { FirestoreDB.instance.collection("users").document(it) }
    private val usersCollection = FirestoreDB.instance.collection("users")
    private val storageRef = FirebaseStorage.getInstance().reference

    fun registerWithEmailAndPassword(user: User, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateUserProfile(user, onComplete)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun registerWithGoogle(
        context: Context,
        account: GoogleSignInAccount,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let {
                        context.getSharedPreferences("loginDetail", MODE_PRIVATE).edit().apply {
                            putString("user_id", currentUser.uid)
                            putString("email", account.email ?: "")
                            putString("lastname", account.familyName ?: "")
                            putString("firstname", account.givenName ?: "")
                            putString("user_type", "employee")
                            putString("photo_url", account.photoUrl?.toString())
                        }.apply()

                        onComplete(true, null)
                    } ?: run {
                        onComplete(false, task.exception?.message)
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun loginWithEmailAndPassword(
        email: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    private fun updateUserProfile(
        userData: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("${userData.firstname} ${userData.middleName} ${userData.lastname}")
            .setPhotoUri(Uri.parse(userData.profilePhotoUrl))
            .build()

        currentUser?.let {
            it.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        createPersonalDetails(userData)
                        onComplete(true, null)
                    } else {
                        onComplete(false, task.exception?.message)
                    }
                }
        }
    }

    private fun createPersonalDetails(user: User) {
        usersDocument?.collection("personalDetails")?.add(user)
    }

    fun createCompany(company: Company, onComplete: (Boolean, String?) -> Unit) {
        usersDocument?.let {
            it.collection("companyDetails")
                .add(company)
                .addOnSuccessListener {
                    onComplete(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete(false, error.message)
                }
        }
    }

    fun getCompanyByUserId(userId: String, onComplete: (MutableList<Company>?, String?) -> Unit) {
        usersCollection.document(userId)
            .collection("companyDetails")
            .get()
            .addOnSuccessListener { document ->
                println(document)
                val company = document.toObjects(Company::class.java)

                onComplete(company, null)
            }
            .addOnFailureListener {
                onComplete(null, it.message)
            }

    }

    fun postJob(job: Job, onComplete: (Boolean, String?) -> Unit) {
        usersDocument?.let {
            it.collection("postedJobs")
                .add(job)
                .addOnSuccessListener {
                    onComplete(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete(false, error.message)
                }
        }
    }

    fun getPostedJobs(onComplete: (MutableList<Job>?, String?) -> Unit) {
        val jobsPostedCollection = FirestoreDB.instance.collectionGroup("postedJobs")

        jobsPostedCollection.get()
            .addOnSuccessListener { documents ->
                val allJobs = mutableListOf<Job>()

                for (document in documents) {
                    val job = document.toObject(Job::class.java)
                    job.apply {
                        jobId = document.id
                    }
                    allJobs.add(job)
                }

                onComplete(allJobs, null)
            }

            .addOnFailureListener {
                onComplete(null, it.message)
            }
    }

    fun applyJob(
        applyJob: ApplyJob,
        cvUri: Uri?,
        cvName: String?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (cvUri != null) {
            val cvRef = storageRef.child("pdfs/$cvName")

            cvRef.putFile(cvUri)
                .addOnSuccessListener {
                    cvRef.downloadUrl.addOnSuccessListener { uri ->
                        applyJob.apply {
                            cvURl = uri.toString()
                        }

                        usersDocument?.let {
                            it.collection("jobsAppliedFor")
                                .add(applyJob)
                                .addOnSuccessListener {
                                    onComplete(true, null)
                                }
                                .addOnFailureListener { exception ->
                                    onComplete(false, exception.message)
                                }
                        }

                    }
                }
                .addOnFailureListener { exception ->
                    onComplete(false, exception.message)
                }
        } else {
            usersDocument?.let {
                it.collection("jobsAppliedFor")
                    .add(applyJob)
                    .addOnSuccessListener {
                        onComplete(true, null)
                    }
                    .addOnFailureListener { exception ->
                        onComplete(false, exception.message)
                    }

            }
        }

    }

    fun updateUserExperience(
        userId: String,
        experience: Experience,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Implement the logic to update user experience in Firestore
        // Use FirestoreDB.instance.collection("users").document(userId).collection("experience").document(experienceId).set(experience)
        TODO("Still coming up")
    }

    fun updateUserEducation(
        userId: String,
        education: Education,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Implement the logic to update user education in Firestore
        // Use FirestoreDB.instance.collection("users").document(userId).collection("education").document(educationId).set(education)
        TODO("Still coming up")
    }


    fun blockJob(
        userId: String,
        jobId: String,
        reason: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Implement the logic to block a job in Firestore
        // Use FirestoreDB.instance.collection("users").document(userId).collection("jobsBlocked").document(jobId).set(mapOf("reason" to reason))
        TODO("Still coming up")
    }

    fun likeJob(userId: String, jobId: String, onComplete: (Boolean, String?) -> Unit) {
        // Implement the logic to like a job in Firestore
        // Use FirestoreDB.instance.collection("users").document(userId).collection("jobsLiked").document(jobId).set(mapOf())
        TODO("Still coming up")
    }


}