package com.toochi.job_swift.backend

import com.google.firebase.firestore.QuerySnapshot
import com.toochi.job_swift.model.Company

object CompanyManager {

    fun createCompany(company: Company, onComplete: (Boolean, String?) -> Unit) {
        // Get the current user
        val currentUser = AuthenticationManager.auth.currentUser

        // Check if the current user is not null
        currentUser?.let {
            // Reference to the user's document in the "users" collection
            val userDocument = AuthenticationManager.usersCollection.document(it.uid)

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
        val currentUser = AuthenticationManager.auth.currentUser

        // Check if the current user is not null
        currentUser?.let {
            // Reference to the user's document in the "users" collection
            val userDocument = AuthenticationManager.usersCollection.document(it.uid)

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
        val currentUser = AuthenticationManager.auth.currentUser

        // Reference to the "users" collection
        val usersDocument = currentUser?.let { AuthenticationManager.usersCollection.document(it.uid) }

        // Reference to the "companyDetails" collection based on user or specified userId
        val companyDetailsCollection =
            if (userId.isEmpty()) usersDocument else AuthenticationManager.usersCollection.document(userId)

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

}