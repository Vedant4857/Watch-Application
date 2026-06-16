package com.school.erp.watch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.school.erp.watch.presentation.screens.*
import com.school.erp.watch.presentation.theme.SchoolERPWatchTheme
import com.school.erp.watch.viewmodel.DashboardViewModel
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.school.erp.watch.data.DeviceTokenRegistrar

// ─── Navigation Routes ────────────────────────────────────────────────────────
object Routes {
    const val DASHBOARD          = "dashboard"
    const val STAFF_ATTENDANCE   = "staff_attendance"
    const val STUDENT_ATTENDANCE = "student_attendance"
    const val FEES               = "fees"
    const val ADMISSIONS         = "admissions"
    const val STAFF_LIST         = "staff_list"
    const val STUDENT_LIST       = "student_list"
}

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d("FCM", "Notification permission granted: $granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        com.school.erp.watch.data.SchoolDataRepository.initDatabase(this)
        registerFcmToken()
        checkNotificationPermission()

        setContent {
            SchoolERPWatchTheme {
                val navController = rememberSwipeDismissableNavController()
                val viewModel: DashboardViewModel = viewModel()

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = Routes.DASHBOARD
                ) {
                    composable(Routes.DASHBOARD) {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToStaffAttendance   = { navController.navigate(Routes.STAFF_ATTENDANCE) },
                            onNavigateToStudentAttendance = { navController.navigate(Routes.STUDENT_ATTENDANCE) },
                            onNavigateToFees              = { navController.navigate(Routes.FEES) },
                            onNavigateToAdmissions        = { navController.navigate(Routes.ADMISSIONS) },
                            onNavigateToStaffList         = { navController.navigate(Routes.STAFF_LIST) },
                            onNavigateToStudentList       = { navController.navigate(Routes.STUDENT_LIST) }
                        )
                    }

                    composable(Routes.STAFF_ATTENDANCE) {
                        StaffAttendanceScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.STUDENT_ATTENDANCE) {
                        StudentAttendanceScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.FEES) {
                        FeesScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.ADMISSIONS) {
                        AdmissionsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.STAFF_LIST) {
                        StaffListScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.STUDENT_LIST) {
                        StudentListScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(permission)
            }
        }
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM", "TOKEN = $token")
                DeviceTokenRegistrar.register(token)
            }
            .addOnFailureListener {
                Log.e("FCM", "Failed to get FCM token — is Google Play Services available?", it)
            }
    }
}
