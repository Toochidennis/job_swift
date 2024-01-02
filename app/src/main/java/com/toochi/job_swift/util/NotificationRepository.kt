package com.toochi.job_swift.util

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NotificationRepository {
    private val _newNotification = MutableLiveData<Unit>()
    val newNotification: LiveData<Unit> get() = _newNotification

    fun notifyNewNotification() {
        Handler(Looper.getMainLooper()).post {
            _newNotification.value = Unit
        }
    }
}
