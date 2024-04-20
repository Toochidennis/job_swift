package com.toochi.job_swift.backend

import com.toochi.job_swift.backend.AuthenticationManager.usersCollection
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.util.Constants.Companion.ACCEPTED
import com.toochi.job_swift.util.Constants.Companion.REJECTED
import com.toochi.job_swift.util.Constants.Companion.REPORT

object NotificationsManager {

    fun createNotifications(
        notification: Notification,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val ownerDocument = when (notification.type) {
            ACCEPTED, REJECTED -> usersCollection.document(notification.userId)
            REPORT -> usersCollection.document(notification.adminId)
            else -> usersCollection.document(notification.employerId)
        }

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
        val currentUser = AuthenticationManager.auth.currentUser
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
                    } else {
                        onComplete(null, "No notifications yet")
                    }
                }
                .addOnFailureListener { error ->
                    onComplete(null, error.message)
                }
        }
    }
}