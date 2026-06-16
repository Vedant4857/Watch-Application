package com.school.erp.watch.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "DeviceTokenRegistrar"

object DeviceTokenRegistrar {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = SchoolDataRepository()

    fun register(token: String) {
        val trimmed = token.trim()
        if (trimmed.isEmpty()) {
            Log.w(TAG, "Skipping empty FCM token")
            return
        }

        scope.launch {
            try {
                repository.registerDeviceToken(trimmed)
                Log.d(TAG, "Token registered with backend")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register token with backend", e)
            }
        }
    }
}
