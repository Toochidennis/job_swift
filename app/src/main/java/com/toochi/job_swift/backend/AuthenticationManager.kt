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
import com.toochi.job_swift.R
import com.toochi.job_swift.model.Company
import com.toochi.job_swift.model.Education
import com.toochi.job_swift.model.Experience
import com.toochi.job_swift.model.Job
import com.toochi.job_swift.model.User

object AuthenticationManager {
    private val auth = UserAuth.instance

    private val currentUser = auth.currentUser
    private val userId = currentUser?.uid
    private val userDocument = userId?.let { FirestoreDB.instance.collection("users").document(it) }

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
        userDocument?.collection("personalDetails")?.add(user)
    }

    fun createCompany(company: Company, onComplete: (Boolean, String?) -> Unit) {
        userDocument?.collection("companyDetails")
            ?.add(company)
            ?.addOnSuccessListener {
                onComplete(true, null)
            }
            ?.addOnFailureListener { error ->
                onComplete(false, error.message)
            }
    }

    fun updateCompany(company: Company, onComplete: (Boolean, String?) -> Unit) {

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

// Similar functions for companies, personal details, and other user-related information

    fun postJob(userId: String, job: Job, onComplete: (Boolean, String?) -> Unit) {
        // Implement the logic to post a job in Firestore
        // Use FirestoreDB.instance.collection("users").document(userId).collection("jobsPosted").add(job)
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