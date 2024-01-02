package com.toochi.job_swift.backend


import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.storage.FirebaseStorage
import com.toochi.job_swift.R
import com.toochi.job_swift.backend.PersonalDetailsManager.checkIfUserExists
import com.toochi.job_swift.backend.PersonalDetailsManager.createNewUser
import com.toochi.job_swift.backend.PersonalDetailsManager.updateExistingUser
import com.toochi.job_swift.model.User

object AuthenticationManager {

    val auth = UserAuth.instance
    val usersCollection = FirestoreDB.instance.collection("users")
    val storageRef = FirebaseStorage.getInstance().reference

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

}