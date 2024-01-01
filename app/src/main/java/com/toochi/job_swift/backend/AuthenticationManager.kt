/**
 * This package contains the backend functionalities of the Job Swift application.
 *
 * It includes authentication management, user-related operations, job posting,
 * job application, and notifications handling. The code is organized into several
 * modules and functions to provide a structured approach to backend tasks.
 *
 * @see AuthenticationManager For managing user authentication and registration.
 * @see com.toochi.job_swift.model Contains data classes representing entities in the application.
 * @see com.toochi.job_swift.util.Constants.Companion Contains constant values used across the application.
 */

package com.toochi.job_swift.backend


import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.toochi.job_swift.R
import com.toochi.job_swift.model.ApplyJob
import com.toochi.job_swift.model.Company
import com.toochi.job_swift.model.Education
import com.toochi.job_swift.model.Experience
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.model.PostJob
import com.toochi.job_swift.model.User
import com.toochi.job_swift.util.Constants.Companion.IMAGE_STORAGE_PATH
import com.toochi.job_swift.util.Constants.Companion.PDF_STORAGE_PATH

object AuthenticationManager {

    val auth = UserAuth.instance
    private val usersCollection = FirestoreDB.instance.collection("users")
    private val storageRef = FirebaseStorage.getInstance().reference
    private var completedCount = 0

    fun registerWithEmailAndPassword(user: User, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnSuccessListener {
                handleRegistrationSuccess(user, onComplete)
            }
            .addOnFailureListener { error ->
                onComplete.invoke(false, error.message)
            }
    }

    private fun handleRegistrationSuccess(user: User, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        if (userId != null) {
            createNewUser(userId, user, onComplete)
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
                    handleGoogleSignInSuccess(account, onComplete)
                } else {
                    onComplete.invoke(false, task.exception?.message)
                }
            }
    }

    private fun handleGoogleSignInSuccess(
        account: GoogleSignInAccount,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            checkIfUserExists(userId) { exists, profileId ->
                if (exists) {
                    if (profileId != null) {
                        updateExistingUser(
                            profileId,
                            hashMapOf(
                                "firstname" to account.givenName.toString(),
                                "lastname" to account.familyName.toString(),
                                "profilePhotoUrl" to account.photoUrl.toString()
                            ), onComplete
                        )
                    }
                } else {
                    createNewUser(
                        userId,
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

    private fun createNewUser(
        userId: String,
        user: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        usersCollection.document(userId).let {
            it.collection("personalDetails")
                .add(user).addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(false, error.message)
                }
        }
    }


    private fun checkIfUserExists(userId: String, onComplete: (Boolean, String?) -> Unit) {
        usersCollection
            .document(userId)
            .collection("personalDetails")
            .get()
            .addOnSuccessListener { querySnapShot ->
                if (!querySnapShot.isEmpty) {
                    val profileDocument = querySnapShot.documents[0]
                    val profileId = profileDocument.id

                    onComplete.invoke(true, profileId)
                } else {
                    onComplete.invoke(false, null)
                }
            }
            .addOnFailureListener { error ->
                onComplete.invoke(false, error.message)
            }
    }

    fun getUserPersonalDetails(onComplete: (User?, String?) -> Unit) {
        // Get the current user's ID
        auth.currentUser?.uid?.let { userId ->
            // Reference to the user's personal details collection
            val userPersonalDetailsCollection = usersCollection.document(userId)
                .collection("personalDetails")

            // Retrieve personal details
            userPersonalDetailsCollection.get()
                .addOnSuccessListener { querySnapshot ->
                    handleUserDetailsQuerySnapshot(querySnapshot, onComplete)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(null, error.message)
                }
        }
    }

    private fun handleUserDetailsQuerySnapshot(
        querySnapshot: QuerySnapshot,
        onComplete: (User?, String?) -> Unit
    ) {
        if (!querySnapshot.isEmpty) {
            // Extract user details from the query snapshot
            val user: User? = querySnapshot.documents.firstOrNull()?.toObject(User::class.java)

            // Set the profile ID in the user object
            user?.profileId = querySnapshot.documents.firstOrNull()?.id.toString()

            onComplete.invoke(user, null)
        } else {
            onComplete.invoke(null, "User details not available")
        }
    }

    fun updateExistingUser(
        profileId: String,
        hashMap: HashMap<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.currentUser?.uid?.let {
            usersCollection.document(it).let { personalDocument ->
                personalDocument.collection("personalDetails")
                    .document(profileId)
                    .update(hashMap)
                    .addOnSuccessListener {
                        onComplete.invoke(true, null)
                    }.addOnFailureListener { error ->
                        onComplete.invoke(false, error.toString())
                    }
            }
        }

    }


    fun createCompany(company: Company, onComplete: (Boolean, String?) -> Unit) {
        // Get the current user
        val currentUser = auth.currentUser

        // Check if the current user is not null
        currentUser?.let {
            // Reference to the user's document in the "users" collection
            val userDocument = usersCollection.document(it.uid)

            // Reference to the "companyDetails" collection under the user's document
            val companyDetailsCollection = userDocument.collection("companyDetails")

            // Add the company details to the collection
            companyDetailsCollection.add(company)
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
        // Get the current user
        val currentUser = auth.currentUser

        // Check if the current user is not null
        currentUser?.let {
            // Reference to the user's document in the "users" collection
            val userDocument = usersCollection.document(it.uid)

            // Reference to the "companyDetails" collection under the user's document
            val companyDetailsCollection = userDocument.collection("companyDetails")

            // Update the specified company document with the provided data
            companyDetailsCollection.document(companyId)
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
        // Get the current user
        val currentUser = auth.currentUser

        // Reference to the "users" collection
        val usersDocument = currentUser?.let { usersCollection.document(it.uid) }

        // Reference to the "companyDetails" collection based on user or specified userId
        val companyDetailsCollection =
            if (userId.isEmpty()) usersDocument else usersCollection.document(userId)

        companyDetailsCollection?.collection("companyDetails")
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                handleCompanyDetailsQuerySnapshot(querySnapshot, onComplete)
            }
            ?.addOnFailureListener { error ->
                onComplete.invoke(null, error.message)
            }
    }

    private fun handleCompanyDetailsQuerySnapshot(
        querySnapshot: QuerySnapshot,
        onComplete: (Company?, String?) -> Unit
    ) {
        if (!querySnapshot.isEmpty) {
            // Extract company details from the query snapshot
            val company: Company? =
                querySnapshot.documents.firstOrNull()?.toObject(Company::class.java)

            // Set the companyId in the company object
            company?.companyId = querySnapshot.documents.firstOrNull()?.id.toString()

            onComplete.invoke(company, null)
        } else {
            onComplete.invoke(null, "Company details not available.")
        }
    }


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
                handlePostedJobsByUserIdQuerySnapshot(querySnapshot, onComplete)
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

    fun checkIfHaveAppliedJob(jobId: String, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser

        // Reference to the "users" collection
        val usersDocument = currentUser?.let { usersCollection.document(it.uid) }

        // Reference to the "postedJobs" collection under the user's document
        val postedJobsCollection = usersDocument?.collection("jobsAppliedFor")

        postedJobsCollection?.whereEqualTo("jobId", jobId)
            ?.get()
            ?.addOnSuccessListener {
                if (!it.isEmpty){
                    onComplete(true, null)
                }else{
                    onComplete(false, null)
                }
            }

            ?.addOnFailureListener {
                onComplete(false, it.message)
            }

    }


    fun updateProfileImage(
        profileId: String,
        imageUri: Uri,
        imageName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val imageRef = storageRef.child("$IMAGE_STORAGE_PATH/$imageName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { uploadTask ->
                handleImageUploadSuccess(uploadTask, profileId, onComplete)
            }
            .addOnFailureListener { error ->
                onComplete.invoke(false, error.message)
            }
    }

    private fun handleImageUploadSuccess(
        uploadTask: UploadTask.TaskSnapshot,
        profileId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        uploadTask.storage.downloadUrl.addOnSuccessListener { uri ->
            updateExistingUser(
                profileId,
                hashMapOf("profilePhotoUrl" to uri.toString())
            ) { success, error ->
                if (success) {
                    onComplete.invoke(true, null)
                } else {
                    onComplete.invoke(false, error)
                }
            }
        }.addOnFailureListener { error ->
            onComplete.invoke(false, error.message)
        }
    }


    fun getUserToken(userId: String, onComplete: (String?, String?) -> Unit) {
        val personalDetailsCollection =
            usersCollection.document(userId).collection("personalDetails")

        personalDetailsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                handleUserTokenQuerySnapshot(querySnapshot, onComplete)
            }
            .addOnFailureListener { error ->
                onComplete.invoke(null, error.message)
            }
    }

    private fun handleUserTokenQuerySnapshot(
        querySnapshot: QuerySnapshot,
        onComplete: (String?, String?) -> Unit
    ) {
        if (!querySnapshot.isEmpty) {
            val token = querySnapshot.documents[0].getString("token")
            onComplete.invoke(token, null)
        } else {
            onComplete.invoke(null, "Token not found")
        }
    }


    fun applyJob(
        applyJob: ApplyJob,
        cvUri: Uri?,
        cvName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

        cvUri?.let { uri ->
            uploadCVAndApplyJob(usersDocument, applyJob, uri, cvName, onComplete)
        } ?: applyJobWithoutCV(usersDocument, applyJob, onComplete)
    }

    private fun uploadCVAndApplyJob(
        usersDocument: DocumentReference?,
        applyJob: ApplyJob,
        cvUri: Uri,
        cvName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val cvRef = storageRef.child("$PDF_STORAGE_PATH/$cvName")

        cvRef.putFile(cvUri)
            .addOnSuccessListener {
                handleCVUploadSuccess(cvRef, applyJob, usersDocument, onComplete)
            }
            .addOnFailureListener { exception ->
                onComplete.invoke(false, exception.message)
            }
    }

    private fun handleCVUploadSuccess(
        cvRef: StorageReference,
        applyJob: ApplyJob,
        usersDocument: DocumentReference?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        cvRef.downloadUrl.addOnSuccessListener { uri ->
            applyJob.cvURl = uri.toString()

            applyJob(usersDocument, applyJob, onComplete)
        }.addOnFailureListener { error ->
            onComplete.invoke(false, error.message)
        }
    }

    private fun applyJob(
        usersDocument: DocumentReference?,
        applyJob: ApplyJob,
        onComplete: (Boolean, String?) -> Unit
    ) {
        usersDocument?.collection("jobsAppliedFor")
            ?.add(applyJob)
            ?.addOnSuccessListener {
                onComplete.invoke(true, null)
            }
            ?.addOnFailureListener { exception ->
                onComplete.invoke(false, exception.message)
            }
    }

    private fun applyJobWithoutCV(
        usersDocument: DocumentReference?,
        applyJob: ApplyJob,
        onComplete: (Boolean, String?) -> Unit
    ) {
        applyJob(usersDocument, applyJob, onComplete)
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
                val userId = document.getString("ownerId")

                if (jobId != null && userId != null) {
                    fetchPostedJob(jobId, userId, allPostJobs, allAppliedJobs, onComplete)
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


    fun createUserExperience(experience: Experience, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

        usersDocument?.let { userDoc ->
            userDoc.collection("experienceDetails")
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
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

        usersDocument?.let { userDoc ->
            userDoc.collection("experienceDetails")
                .document(experienceId)
                .update(data)
                .addOnSuccessListener {
                    onComplete.invoke(true, null)
                }
                .addOnFailureListener { error ->
                    onComplete.invoke(false, error.message)
                }
        }
    }


    fun getUserExperienceDetails(
        userId: String = "",
        onComplete: (MutableList<Experience>?, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

        val experienceDocument = if (userId.isEmpty()) {
            usersDocument
        } else {
            usersCollection.document(userId)
        }

        experienceDocument?.let { userDoc ->
            userDoc.collection("experienceDetails")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val experienceList = mutableListOf<Experience>()

                        querySnapshot.documents.map { document ->
                            val experience = document.toObject(Experience::class.java)

                            experience?.apply {
                                experienceId = document.id
                                experienceList.add(this)
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
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

        usersDocument?.let { userDoc ->
            userDoc.collection("educationDetails")
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
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

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
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            usersCollection.document(it.uid)
        }

        val educationDocument = if (userId.isEmpty()) {
            usersDocument
        } else {
            usersCollection.document(userId)
        }

        educationDocument?.let { document ->
            document.collection("educationDetails")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val educationList = mutableListOf<Education>()

                        querySnapshot.documents.map { doc ->
                            val education = doc.toObject(Education::class.java)

                            education?.apply {
                                educationId = doc.id
                                educationList.add(this)
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
        val ownerDocument = usersCollection.document(notification.ownerId)

        ownerDocument.collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { error ->
                onComplete(false, error.message)
            }
    }


    fun getAllNotifications(onComplete: (MutableList<Notification>?, String?) -> Unit) {
        val currentUser = auth.currentUser
        val usersDocument = currentUser?.let {
            FirestoreDB.instance.collection("users").document(it.uid)
        }

        usersDocument?.let { document ->
            document.collection("notifications")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val notifications = querySnapshot.toObjects(Notification::class.java)
                        onComplete(notifications, null)
                    }
                }
                .addOnFailureListener { error ->
                    onComplete(null, error.message)
                }
        }
    }


}