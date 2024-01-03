package com.toochi.job_swift.backend

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask
import com.toochi.job_swift.backend.AuthenticationManager.auth
import com.toochi.job_swift.backend.AuthenticationManager.storageRef
import com.toochi.job_swift.backend.AuthenticationManager.usersCollection
import com.toochi.job_swift.model.User
import com.toochi.job_swift.util.Constants
import com.toochi.job_swift.util.Constants.Companion.ADMIN
import com.toochi.job_swift.util.Constants.Companion.NOT_AVAILABLE
import com.toochi.job_swift.util.Constants.Companion.PERSONAL_DETAILS

/**
 * The `PersonalDetailsManager` module provides functions for managing user personal details,
 * interacting with Firebase Firestore, and handling user authentication.
 *
 * @property usersCollection Reference to the Firestore collection storing user data.
 */

object PersonalDetailsManager {

    /**
     * Creates, retrieves, and updates user personal details in the Firestore database.
     * Manages user authentication-related tasks and interactions with Firebase Storage.
     *
     * @property usersCollection Reference to the Firestore collection storing user data.
     */

    fun createNewUser(
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


    fun checkIfUserExists(userId: String, onComplete: (Boolean, String?) -> Unit) {
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

    fun getUserPersonalDetails(userId: String = "", onComplete: (User?, String?) -> Unit) {
        // Get the current user's ID
        val currentId = auth.currentUser?.uid

        // Reference to the user's personal details collection
        val userPersonalDetailsCollection = if (userId.isEmpty()) {
            currentId?.let {
                usersCollection.document(it)
                    .collection(PERSONAL_DETAILS)
            }
        } else {
            usersCollection.document(userId).collection(PERSONAL_DETAILS)
        }

        // Retrieve personal details
        userPersonalDetailsCollection?.get()
            ?.addOnSuccessListener { querySnapshot ->
                handleUserDetailsQuerySnapshot(querySnapshot, onComplete)
            }
            ?.addOnFailureListener { error ->
                onComplete.invoke(null, error.message)
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

    fun updateProfileImage(
        profileId: String,
        imageUri: Uri,
        imageName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val imageRef = storageRef.child("${Constants.IMAGE_STORAGE_PATH}/$imageName")

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
            onComplete.invoke(null, NOT_AVAILABLE)
        }
    }

    fun getAdminToken(onComplete: (String?, String?, String?) -> Unit) {
        usersCollection
            .whereEqualTo("userType", ADMIN)
            .get()
            .addOnSuccessListener {
                handleAdminTokenQuerySnapshot(it, onComplete)
            }
            .addOnFailureListener { error ->
                onComplete.invoke(null, null, error.message)
            }
    }

    private fun handleAdminTokenQuerySnapshot(
        querySnapshot: QuerySnapshot,
        onComplete: (String?, String?, String?) -> Unit
    ) {
        if (!querySnapshot.isEmpty) {
            val adminId = querySnapshot.documents.firstOrNull()?.id

            val adminPersonalCollection =
                adminId?.let { usersCollection.document(it).collection(PERSONAL_DETAILS) }

            adminPersonalCollection
                ?.get()
                ?.addOnSuccessListener {
                    val token = querySnapshot.documents.firstOrNull()?.getString("token")
                    onComplete.invoke(token, adminId, null)
                }
                ?.addOnFailureListener { exception ->
                    onComplete.invoke(null, null, exception.message)
                }

        } else {
            onComplete.invoke(null, null, NOT_AVAILABLE)
        }
    }

    fun getAllUsers(onComplete: (MutableList<User>?, String?) -> Unit) {
        usersCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onComplete.invoke(null, NOT_AVAILABLE)
                } else {
                    handleAllUsersQuerySnapShot(querySnapshot, onComplete)
                }
            }
            .addOnFailureListener { exception ->
                onComplete.invoke(null, exception.message)
            }
    }

    private fun handleAllUsersQuerySnapShot(
        querySnapshot: QuerySnapshot,
        onComplete: (MutableList<User>?, String?) -> Unit
    ) {
        val usersList = mutableListOf<User>()
        val tasks = mutableListOf<Task<QuerySnapshot>>()

        querySnapshot.documents.forEach { documentSnapshot ->
            val userId = documentSnapshot.id
            val userPersonalDetailCollection =
                usersCollection.document(userId).collection(PERSONAL_DETAILS)

            val task = userPersonalDetailCollection.get()
            tasks.add(task)

            // Add an onCompleteListener to each inner task to process the result
            task.addOnCompleteListener { innerTask ->
                if (innerTask.isSuccessful) {
                    val innerQuerySnapshot = innerTask.result
                    innerQuerySnapshot?.documents?.forEach { innerDocumentSnapshot ->
                        val user = innerDocumentSnapshot.toObject(User::class.java)
                        if (user != null && user.userType != ADMIN) {
                            user.profileId = innerDocumentSnapshot.id
                            usersList.add(user)
                        }
                    }
                } else {
                    onComplete.invoke(null, innerTask.exception?.message)
                }
            }
        }

        // Wait for all inner tasks to complete before invoking the outer onComplete
        Tasks.whenAllSuccess<QuerySnapshot>(tasks)
            .addOnSuccessListener {
                onComplete(usersList, null)
            }
            .addOnFailureListener { exception ->
                onComplete(null, exception.message)
            }
    }

}