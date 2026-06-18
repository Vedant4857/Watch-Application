package com.school.erp.watch.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import com.school.erp.watch.data.Notification
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState

@Composable
fun NotificationsScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.notifications.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = s.message, onRetry = { viewModel.refresh() })
        is UiState.Success -> NotificationsContent(notifications = s.data)
    }
}

@Composable
private fun NotificationsContent(notifications: List<Notification>) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Black),
            contentPadding = PaddingValues(
                top = 28.dp,
                bottom = 20.dp,
                start = 10.dp,
                end = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "🔔 Notifications",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val unreadCount = notifications.count { !it.isRead }
                    if (unreadCount > 0) {
                        Text(
                            text = "$unreadCount New",
                            color = CyanAccent,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "All caught up",
                            color = MutedGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (notifications.isEmpty()) {
                item {
                    Text(
                        text = "No notifications yet",
                        color = SoftWhite,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                    )
                }
            }

            items(notifications.size) { index ->
                NotificationCard(notification = notifications[index])
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: Notification) {
    val (icon, color) = when (notification.type) {
        "URGENT" -> "🔴" to CoralRed
        "EVENT" -> "📅" to GoldenYellow
        "SYSTEM" -> "⚙️" to ElectricBlue
        else -> "💬" to SoftWhite
    }

    val backgroundColor = if (!notification.isRead) grey else Black
    val borderColor = if (!notification.isRead) color.copy(alpha = 0.5f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = notification.title,
                color = color,
                fontSize = 12.sp,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = notification.message,
            color = if (!notification.isRead) SoftWhite else MutedGray,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    }
}
