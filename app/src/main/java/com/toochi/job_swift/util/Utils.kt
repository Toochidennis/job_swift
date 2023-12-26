package com.toochi.job_swift.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.auth.oauth2.GoogleCredentials
import com.toochi.job_swift.util.Constants.Companion.SCOPES
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

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

    @OptIn(DelicateCoroutinesApi::class)
    fun getAccessToken(context: Context, onComplete: (String?, String?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("service-account.json")

                val googleCredentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(listOf(SCOPES))

                val token = googleCredentials.refreshAccessToken()
                val accessToken = token.tokenValue

                withContext(Dispatchers.Main) {
                    onComplete(accessToken, null)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    onComplete(null, e.message)
                }

            }
        }
    }

}