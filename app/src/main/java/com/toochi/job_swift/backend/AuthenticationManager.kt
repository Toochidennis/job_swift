package com.toochi.job_swift.backend

import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.toochi.job_swift.R
import com.toochi.job_swift.model.User

object AuthenticationManager {
    private val auth = Auth.instance

    fun registerUser(user: User, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, user.password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let {
                        updateUserProfile(it, user, onComplete)
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun registerWithGoogle(
        account: GoogleSignInAccount,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let {
                        updateUserProfile(
                            it,
                            User(
                                account.email ?: "",
                                "",
                                account.givenName ?: "",
                                account.familyName ?: "",
                                "",
                                account.photoUrl?.toString()
                            ),
                            onComplete
                        )
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

    fun loginWithGoogle(account: GoogleSignInAccount, onComplete: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
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

    fun updateDisplayName(
        user: FirebaseUser,
        newDisplayName: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newDisplayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun updatePhotoUrl(
        user: FirebaseUser,
        newPhotoUrl: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(newPhotoUrl))
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }


    fun updateEmailWithoutReauthentication(
        user: FirebaseUser,
        newEmail: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        user.updateEmail(newEmail)
            .addOnCompleteListener { updateEmailTask ->
                if (updateEmailTask.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, updateEmailTask.exception?.message)
                }
            }
    }


    fun updatePassword(
        user: FirebaseUser,
        newPassword: String,
        oldPassword: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // You need to re-authenticate before updating password
        val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Re-authentication successful, now update the password
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updatePasswordTask ->
                            if (updatePasswordTask.isSuccessful) {
                                onComplete(true, null)
                            } else {
                                onComplete(false, updatePasswordTask.exception?.message)
                            }
                        }
                } else {
                    onComplete(false, reauthTask.exception?.message)
                }
            }
    }


    private fun updateUserProfile(
        user: FirebaseUser,
        userData: User,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("${userData.firstName} ${userData.lastName}")
            .setPhotoUri(
                if (!userData.profilePhotoUrl.isNullOrBlank())
                    Uri.parse(userData.profilePhotoUrl)
                else Uri.parse("")
            )
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storeUserData(user.uid, userData)
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    private fun storeUserData(
        userId: String?,
        user: User,
        signInMethod: String = "email/password"
    ) {
        val hashMapData = hashMapOf(
            "email" to user.email,
            "firstname" to user.firstName,
            "lastname" to user.lastName,
            "profilePhotoUrl" to user.profilePhotoUrl,
            "userType" to user.userType
        )

        userId?.let {
            FirestoreDB.instance.collection("users")
                .document(userId)
                .set(if (signInMethod == "google") hashMapData else user)

        }
    }

}