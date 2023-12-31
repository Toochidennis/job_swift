package com.toochi.job_swift.backend

import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.storage.FirebaseStorage
import com.toochi.job_swift.R
import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.model.Company
import com.toochi.job_swift.model.Education
import com.toochi.job_swift.model.Experience
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.model.User

object AuthenticationManager {
    val auth = UserAuth.instance

    private val currentUser = auth.currentUser
    private var userId = currentUser?.uid
    val usersDocument =
        userId?.let { FirestoreDB.instance.collection("users").document(it) }
    private val usersCollection = FirestoreDB.instance.collection("users")
    private val storageRef = FirebaseStorage.getInstance().reference
    private var completedCount = 0

    fun registerWithEmailAndPassword(user: User, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnSuccessListener {
                createPersonalDetails(user, onComplete)
            }
            .addOnFailureListener { error ->
                onComplete.invoke(false, error.message)
            }
    }


    fun registerWithGoogleAndLogin(
        account: GoogleSignInAccount,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ifUserExist { success, error ->
                        if (success != null) {
                            updateUserDetails(
                                profileId = success,
                                hashMapOf(
                                    "firstname" to account.givenName.toString(),
                                    "lastname" to account.familyName.toString(),
                                    "profilePhotoUrl" to account.photoUrl.toString()
                                ),
                                onComplete
                            )
                        } else if (error == null) {
                            createPersonalDetails(
                                User(
                                    email = account.email ?: "",
                                    firstname = account.givenName ?: "",
                                    lastname = account.familyName ?: "",
                                    profilePhotoUrl = account.photoUrl.toString(),
                                    userType = "employee"
                                ),
                                onComplete
                            )
                        }
                    }
                } else {
                    onComplete.invoke(false, task.exception?.message)
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
                    onComplete.invoke(true, null)
                } else {
                    onComplete.invoke(false, task.exception?.message)
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

    private fun createPersonalDetails(user: User, onComplete: (Boolean, String?) -> Unit) {
        usersDocument?.let {
            it.collection("personalDetails")
                .add(user).addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(false, error.message)
                }
        }
    }


    private fun ifUserExist(isExist: (String?, String?) -> Unit) {
        usersDocument?.let {
            it.collection("personalDetails")
                .get()
                .addOnSuccessListener { querySnapShot ->
                    if (!querySnapShot.isEmpty) {
                        val document = querySnapShot.documents[0]
                        isExist.invoke(document.id, null)
                    } else {
                        isExist.invoke(null, null)
                    }
                }
                .addOnFailureListener { error ->
                    isExist.invoke(null, error.message)
                }
        }
    }


    fun getUserPersonalDetails(onComplete: (User?, String?) -> Unit) {
        usersDocument?.let {
            it.collection("personalDetails")
                .get()
                .addOnSuccessListener { querySnapShot ->
                    if (!querySnapShot.isEmpty) {
                        var user: User? = null

                        querySnapShot.documents.map { document ->
                            user = document.toObject(User::class.java)
                            user?.profileId = document.id
                        }
                        onComplete.invoke(user, null)

                    } else {
                        onComplete.invoke(null, null)
                    }
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(null, error.message)
                }
        }
    }

    fun updateUserDetails(
        profileId: String,
        hashMap: HashMap<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        usersDocument?.let {
            it.collection("personalDetails")
                .document(profileId)
                .update(hashMap)
                .addOnSuccessListener {
                    onComplete.invoke(true, null)
                }.addOnFailureListener { error ->
                    onComplete.invoke(false, error.toString())
                }
        }

    }


    fun createCompany(company: Company, onComplete: (Boolean, String?) -> Unit) {
        usersDocument?.let {
            it.collection("companyDetails")
                .add(company)
                .addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(false, error.message)
                }
        }
    }

    fun updateCompany(
        companyId: String,
        data: HashMap<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        usersDocument?.let {
            it.collection("companyDetails")
                .document(companyId)
                .update(data)
                .addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(false, error.message)
                }
        }
    }

    fun getCompany(
        userId: String = "",
        onComplete: (Company?, String?) -> Unit
    ) {
        val companyDocument = if (userId.isEmpty()) {
            usersDocument
        } else {
            usersCollection.document(userId)
        }

        companyDocument?.let {
            it.collection("companyDetails")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        var company: Company? = null

                        querySnapshot.documents.map { document ->
                            company = document.toObject(Company::class.java)
                            company?.companyId = document.id
                        }

                        onComplete.invoke(company, null)
                    } else {
                        onComplete.invoke(null, null)
                    }
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(null, error.message)
                }
        }
    }

    fun postJob(postJob: PostJob, onComplete: (Boolean, String?) -> Unit) {
        usersDocument?.let {
            it.collection("postedJobs")
                .add(postJob)
                .addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(false, error.message)
                }
        }
    }

    fun getAllPostedJobs(onComplete: (MutableList<PostJob>?, String?) -> Unit) {
        val jobsPostedCollection = FirestoreDB.instance.collectionGroup("postedJobs")

        jobsPostedCollection.get()
            .addOnSuccessListener { documents ->
                val allPostJobs = mutableListOf<PostJob>()

                for (document in documents) {
                    val postJob = document.toObject(PostJob::class.java)
                    postJob.apply {
                        jobId = document.id
                    }
                    allPostJobs.add(postJob)
                }

                onComplete.invoke(allPostJobs, null)
            }

            .addOnFailureListener {
                onComplete.invoke(null, it.message)
            }
    }

    fun getPostedJobsByUserId(onComplete: (MutableList<PostJob>?, String?) -> Unit) {
        usersDocument?.let {
            it.collection("postedJobs")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        onComplete.invoke(null, "empty")
                    } else {
                        val allPostJobs = mutableListOf<PostJob>()

                        querySnapshot.documents.map { document ->
                            val postJob = document.toObject(PostJob::class.java)

                            if (postJob != null) {
                                postJob.jobId = document.id
                                allPostJobs.add(postJob)
                            }
                        }

                        onComplete.invoke(allPostJobs, null)
                    }
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(null, error.message)
                }
        }
    }

    fun updateProfileImage(
        profileId: String,
        imageUri: Uri,
        imageName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val imageRef = storageRef.child("images/$imageName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateUserDetails(
                        profileId = profileId,
                        hashMapOf("profilePhotoUrl" to uri.toString())
                    ) { success, error ->
                        if (success) {
                            onComplete.invoke(true, null)
                        } else {
                            onComplete.invoke(false, error)
                        }
                    }
                }
                    .addOnFailureListener { error ->
                        onComplete.invoke(false, error.message)
                    }
            }
            .addOnFailureListener { error ->
                onComplete.invoke(false, error.message)
            }
    }

    fun getUserToken(userId: String, onComplete: (String?, String?) -> Unit) {
        usersCollection.document(userId).collection("personalDetails").get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val token = it.documents[0].getString("token")
                    onComplete.invoke(token, null)
                }
            }
            .addOnFailureListener { error ->
                onComplete.invoke(null, error.message)
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
                                    onComplete.invoke(true, null)
                                }
                                .addOnFailureListener { exception ->
                                    onComplete.invoke(false, exception.message)
                                }
                        }

                    }
                }
                .addOnFailureListener { exception ->
                    onComplete.invoke(false, exception.message)
                }
        } else {
            usersDocument?.let {
                it.collection("jobsAppliedFor")
                    .add(applyJob)
                    .addOnSuccessListener {
                        onComplete.invoke(true, null)
                    }
                    .addOnFailureListener { exception ->
                        onComplete.invoke(false, exception.message)
                    }
            }
        }

    }

    fun getJobsAppliedFor(onComplete: (MutableList<PostJob>?, MutableList<ApplyJob>?, String?) -> Unit) {
        usersDocument?.let {
            it.collection("jobsAppliedFor")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val allPostJobs = mutableListOf<PostJob>()
                        val allAppliedJobs = querySnapshot.toObjects(ApplyJob::class.java)

                        querySnapshot.documents.map { document ->
                            val jobId = document.getString("jobId")
                            val userId = document.getString("ownerId")

                            if (jobId != null && userId != null) {
                                usersCollection.document(userId)
                                    .collection("postedJobs")
                                    .document(jobId)
                                    .get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val postJob = documentSnapshot.toObject(PostJob::class.java)

                                        if (postJob != null) {
                                            postJob.jobId = document.id
                                            allPostJobs.add(postJob)
                                        }

                                        completedCount++

                                        if (completedCount == querySnapshot.size()) {
                                            onComplete.invoke(allPostJobs, allAppliedJobs, null)
                                        }
                                    }
                                    .addOnFailureListener { error ->
                                        onComplete.invoke(null, null, error.message)
                                    }
                            }
                        }
                    } else {
                        onComplete.invoke(null, null, "empty")
                    }
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(null, null, error.message)
                }
        }
    }

    fun createUserExperience(experience: Experience, onComplete: (Boolean, String?) -> Unit) {
        usersDocument?.let {
            it.collection("experienceDetails")
                .add(experience)
                .addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(false, error.message)
                }
        }
    }

    fun updateUserExperience(
        experienceId: String,
        data: HashMap<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        usersDocument?.collection("experienceDetails")
            ?.document(experienceId)?.update(data)
            ?.addOnSuccessListener {
                onComplete.invoke(true, null)
            }?.addOnFailureListener { error ->
                onComplete.invoke(false, error.message)
            }
    }

    fun getUserExperienceDetails(
        userId: String = "",
        onComplete: (MutableList<Experience>?, String?) -> Unit
    ) {
        val experienceDocument = if (userId.isEmpty()) {
            usersDocument
        } else {
            usersCollection.document(userId)
        }

        experienceDocument?.let {
            it.collection("experienceDetails")
                .get()
                .addOnSuccessListener { querySnapShot ->
                    if (!querySnapShot.isEmpty) {
                        val experienceList = mutableListOf<Experience>()

                        querySnapShot.documents.map { document ->
                            val experience = document.toObject(Experience::class.java)

                            if (experience != null) {
                                experience.experienceId = document.id

                                experienceList.add(experience)
                            }
                        }

                        onComplete.invoke(experienceList, null)
                    } else {
                        onComplete.invoke(null, null)
                    }
                }

                .addOnFailureListener { error ->
                    onComplete.invoke(null, error.message)
                }
        }
    }


    fun createUserEducation(education: Education, onComplete: (Boolean, String?) -> Unit) {
        usersDocument?.let {
            it.collection("educationDetails")
                .add(education)
                .addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(false, error.message)
                }
        }
    }

    fun updateUserEducation(
        educationId: String,
        data: HashMap<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        usersDocument?.collection("educationDetails")
            ?.document(educationId)
            ?.update(data)
            ?.addOnSuccessListener {
                onComplete.invoke(true, null)
            }
            ?.addOnFailureListener { error ->
                onComplete.invoke(false, error.message)
            }
    }

    fun getUserEducationDetails(
        userId: String = "",
        onComplete: (MutableList<Education>?, String?) -> Unit
    ) {
        val educationDocument = if (userId.isEmpty()) {
            usersDocument
        } else {
            usersCollection.document(userId)
        }

        educationDocument?.let {
            it.collection("educationDetails")
                .get()
                .addOnSuccessListener { querySnapShot ->
                    if (!querySnapShot.isEmpty) {
                        val educationList = mutableListOf<Education>()

                        querySnapShot.documents.map { document ->
                            val education = document.toObject(Education::class.java)

                            if (education != null) {
                                education.educationId = document.id

                                educationList.add(education)
                            }
                        }

                        onComplete.invoke(educationList, null)
                    } else {
                        onComplete.invoke(null, null)
                    }
                }

                .addOnFailureListener { error ->
                    onComplete.invoke(null, error.message)
                }
        }
    }


    fun createNotifications(
        notification: Notification,
        onComplete: (Boolean, String?) -> Unit
    ) {
        usersCollection.document(notification.ownerId)
            .collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { error ->
                onComplete(false, error.message)
            }
    }

    fun getAllNotifications(onComplete: (MutableList<Notification>?, String?) -> Unit) {
        usersDocument?.also {
            it.collection("notifications")
                .get()
                .addOnSuccessListener { querySnapShot ->
                    if (!querySnapShot.isEmpty) {
                        val notifications = querySnapShot.toObjects(Notification::class.java)
                        onComplete(notifications, null)
                    }
                }
                .addOnFailureListener { error ->
                    onComplete(null, error.message)
                }
        }
    }


}