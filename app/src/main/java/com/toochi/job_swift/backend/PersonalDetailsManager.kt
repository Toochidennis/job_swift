package com.toochi.job_swift.backend

import android.net.Uri
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
        user: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        usersCollection.document(user.userId).let {
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
                    val profileId = querySnapShot.documents.firstOrNull()?.id

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
            onComplete.invoke(null, NOT_AVAILABLE)
        }
    }

    fun updateExistingUser(
        profileId: String,
        hashMap: HashMap<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.currentUser?.uid?.let {
            usersCollection.document(it).let { personalDocument ->
                personalDocument.collection(PERSONAL_DETAILS)
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
            usersCollection.document(userId).collection(PERSONAL_DETAILS)

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
        val usersCollectionGroup = FirestoreDB.instance.collectionGroup(PERSONAL_DETAILS)
        usersCollectionGroup
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
            querySnapshot.documents.map { documentSnapshot ->
                val userType = documentSnapshot.getString("userType")

                if (userType == ADMIN) {
                    val adminId = documentSnapshot.getString("userId")
                    val token = documentSnapshot.getString("token")
                    println("Admin Id: $adminId   token $token")
                    onComplete.invoke(token, adminId, null)
                }
            }
        } else {
            onComplete.invoke(null, null, NOT_AVAILABLE)
        }
    }

    fun getAllUsers(onComplete: (MutableList<User>?, String?) -> Unit) {
        val usersList = mutableListOf<User>()

        FirestoreDB.instance
            .collectionGroup(PERSONAL_DETAILS)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val user = document.toObject(User::class.java)
                    if (user.userType != ADMIN) {
                        user.profileId = document.id
                        usersList.add(user)
                    }
                }
                onComplete(usersList, null)
            }
            .addOnFailureListener { exception ->
                onComplete(null, exception.message)
            }
    }

}