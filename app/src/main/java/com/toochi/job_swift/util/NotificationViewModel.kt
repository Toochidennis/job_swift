package com.toochi.job_swift.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class NotificationViewModel : ViewModel() {
    val newNotification: LiveData<Unit> = NotificationRepository.newNotification
}
