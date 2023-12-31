package com.toochi.job_swift.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.auth.oauth2.GoogleCredentials
import com.toochi.job_swift.backend.AuthenticationManager.auth
import com.toochi.job_swift.model.Notification
import com.toochi.job_swift.model.User
import com.toochi.job_swift.util.Constants.Companion.BASE_URL
import com.toochi.job_swift.util.Constants.Companion.CONTENT_TYPE
import com.toochi.job_swift.util.Constants.Companion.SCOPES
import com.toochi.job_swift.util.Constants.Companion.USER_ID_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object Utils {

    fun loadFragment(activity: AppCompatActivity, fragment: Fragment, container: Int) {
        activity.supportFragmentManager.commit {
            replace(container, fragment)
        }
    }

    fun currencyFormatter(amount: Double): String {
        return DecimalFormat("#,###.##").format(amount)
    }

    fun dateFormatter(date: String, format: String = "default"): String {
        return try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parseDate = simpleDateFormat.parse(date)!!

            val sdf = when (format) {
                "default" -> SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                else -> SimpleDateFormat("dd, MMM", Locale.getDefault())
            }

            sdf.format(parseDate)

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun isValidEmailOrPhoneNumber(text: String, from: String): Boolean {
        return if (from == "email") {
            Patterns.EMAIL_ADDRESS.matcher(text).matches()
        } else {
            val phonePattern = Pattern.compile(
                "^(\\+?234|0)?([789]\\d{9})\$"
            )
            phonePattern.matcher(text).matches()
        }
    }

    private fun getAccessToken(context: Context): String? {
        return runBlocking {
            try {
                val inputStream = context.assets.open("service-account.json")

                val googleCredentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(listOf(SCOPES))

                val token = googleCredentials.refreshAccessToken()
                token.tokenValue

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    fun sendNotification(
        context: Context,
        notification: Notification,
        onComplete: (String?, String?) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = getAccessToken(context)

            val payload = """
                {
                   "message":{
                      "token": "${notification.token}",
                      "notification":{
                        "title":"${notification.title}",
                        "body":"${notification.body}"
                      },
                      "data": {
                        "userId": "${notification.userId}",
                        "ownerId": "${notification.ownerId}",
                        "jobId": "${notification.jobId}"
                      }
                   }
                }
            """.trimIndent()

            val request = Request.Builder()
                .url(BASE_URL)
                .post(payload.toRequestBody(CONTENT_TYPE.toMediaTypeOrNull()))
                .header("Content-Type", CONTENT_TYPE)
                .header("Authorization", "Bearer $token")
                .build()

            val response = OkHttpClient().newCall(request).execute()

            if (response.isSuccessful) {
                onComplete(response.message, null)
            } else {
                onComplete(null, response.code.toString())
            }

            response.close()

        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(null, e.message)
        }
    }


    fun updateSharedPreferences(user: User, sharedPreferences: SharedPreferences) {
        sharedPreferences.edit().run {
            putString(USER_ID_KEY, auth.uid)
            putString("profile_id", user.profileId)
            putString("lastname", user.lastname)
            putString("firstname", user.firstname)
            putString("middle_name", user.middleName)
            putString("user_type", user.userType)
            putString("photo_url", user.profilePhotoUrl)
            putString("phone_number", user.phoneNumber)
            putString("dob", user.dateOfBirth)
            putString("address", user.address)
            apply()
        }
    }

    fun getDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR].toString()
        val month = (calendar[Calendar.MONTH] + 1).toString()
        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH].toString()
        return "$year-$month-$dayOfMonth"
    }

}