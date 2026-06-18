package com.school.erp.watch.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.school.erp.watch.data.StaffLeave
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState

@Composable
fun StaffLeavesScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val leavesState by viewModel.staffLeaves.collectAsStateWithLifecycle()

    when (val state = leavesState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = state.message, onRetry = { /* refresh handled by DashboardViewModel */ })
        is UiState.Success -> {
            val list = state.data
            val scalingLazyListState = rememberScalingLazyListState()

            Scaffold(
                positionIndicator = { PositionIndicator(scalingLazyListState = scalingLazyListState) },
                vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
                timeText = {
                    TimeText(
                        timeTextStyle = TimeTextDefaults.timeTextStyle(color = LeaveOrange)
                    )
                }
            ) {
                ScalingLazyColumn(
                    state = scalingLazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Black),
                    contentPadding = PaddingValues(top = 30.dp, bottom = 20.dp, start = 10.dp, end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Staff Leaves",
                            color = LeaveOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (list.isEmpty()) {
                        item {
                            Text(
                                text = "No leave requests found.",
                                color = MutedGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp)
                            )
                        }
                    } else {
                        items(list.size) { index ->
                            StaffLeaveItem(
                                leave = list[index],
                                onApprove = { viewModel.handleStaffLeaveStatus(it.id, "approved") },
                                onDisapprove = { viewModel.handleStaffLeaveStatus(it.id, "disapproved") }
                            )
                        }
                    }

                    item {
                        Chip(
                            onClick = onBack,
                            colors = ChipDefaults.chipColors(backgroundColor = DarkCard),
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
fun StaffLeaveItem(
    leave: StaffLeave,
    onApprove: (StaffLeave) -> Unit,
    onDisapprove: (StaffLeave) -> Unit
) {
    Card(
        onClick = {},
        enabled = false, // Not clickable as a whole, buttons inside handle actions
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = DarkCard,
            endBackgroundColor = DarkCard
        ),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = leave.staffName,
                color = SoftWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = leave.leaveType,
                color = LeaveOrange,
                fontSize = 12.sp
            )
            Text(
                text = leave.leaveDate,
                color = MutedGray,
                fontSize = 12.sp
            )
            Text(
                text = leave.reason,
                color = MutedGray,
                fontSize = 12.sp
            )
            
            if (leave.status == "pending") {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onDisapprove(leave) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = CoralRed),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(text = "👎", fontSize = 14.sp)
                    }
                    
                    Button(
                        onClick = { onApprove(leave) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = TealGreen),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(text = "👍", fontSize = 14.sp)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (leave.status == "approved") "✅ Approved" else "❌ Disapproved",
                    color = if (leave.status == "approved") TealGreen else CoralRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
