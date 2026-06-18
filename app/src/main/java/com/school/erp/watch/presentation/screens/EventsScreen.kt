package com.school.erp.watch.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import com.school.erp.watch.domain.model.Event
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState

@Composable
fun EventsScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.upcomingEvents.collectAsStateWithLifecycle()

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = s.message, onRetry = { viewModel.refresh() })
        is UiState.Success -> {
            val events = s.data
            val listState = rememberScalingLazyListState()

            Scaffold(
                positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
                vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
            ) {
                ScalingLazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().background(Black),
                    contentPadding = PaddingValues(top = 30.dp, bottom = 20.dp, start = 10.dp, end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "📅 Upcoming Events",
                            color = CyanAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (events.isEmpty()) {
                        item {
                            Text(
                                text = "No upcoming events",
                                color = SoftWhite,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                            )
                        }
                    } else {
                        items(events.size) { i ->
                            EventCard(event = events[i])
                        }
                    }

                    item {
                        Chip(
                            onClick = onBack,
                            colors = ChipDefaults.chipColors(backgroundColor = DarkGrey),
                            label = { Text("← Back", color = SoftWhite) },
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event) {
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = DarkGrey,
            endBackgroundColor = DarkGrey
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.date,
                    color = MutedGray,
                    fontSize = 10.sp
                )
                if (event.startTime != null) {
                    Text(
                        text = event.startTime,
                        color = MutedGray,
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.title,
                color = SoftWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = event.category,
                color = CyanAccent,
                fontSize = 9.sp
            )
        }
    }
}
