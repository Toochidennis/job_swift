package com.toochi.job_swift.backend

import com.toochi.job_swift.model.Education

object EducationManager {

    fun createUserEducation(education: Education, onComplete: (Boolean, String?) -> Unit) {
        val currentUser = AuthenticationManager.auth.currentUser
        val usersDocument = currentUser?.let {
            AuthenticationManager.usersCollection.document(it.uid)
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
        val currentUser = AuthenticationManager.auth.currentUser
        val usersDocument = currentUser?.let {
            AuthenticationManager.usersCollection.document(it.uid)
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
        val currentUser = AuthenticationManager.auth.currentUser
        val usersDocument = currentUser?.let {
            AuthenticationManager.usersCollection.document(it.uid)
        }

        val educationDocument = if (userId.isEmpty()) {
            usersDocument
        } else {
            AuthenticationManager.usersCollection.document(userId)
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

}