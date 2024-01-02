package com.toochi.job_swift.backend

import com.toochi.job_swift.model.Experience

object ExperienceManager {

    fun createUserExperience(experience: Experience, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = AuthenticationManager.auth.currentUser
        val usersDocument = currentUser?.let {
            AuthenticationManager.usersCollection.document(it.uid)
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
        val currentUser = AuthenticationManager.auth.currentUser
        val usersDocument = currentUser?.let {
            AuthenticationManager.usersCollection.document(it.uid)
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
        val currentUser = AuthenticationManager.auth.currentUser
        val usersDocument = currentUser?.let {
            AuthenticationManager.usersCollection.document(it.uid)
        }

        val experienceDocument = if (userId.isEmpty()) {
            usersDocument
        } else {
            AuthenticationManager.usersCollection.document(userId)
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

}