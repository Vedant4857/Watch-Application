package com.school.erp.watch.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import com.school.erp.watch.data.StudentAttendanceData
import com.school.erp.watch.data.ClassAttendance
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState

@Composable
fun StudentAttendanceScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {

    val state by viewModel.studentAttendance.collectAsStateWithLifecycle()

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error   -> ErrorScreen(message = s.message) { viewModel.refresh() }
        is UiState.Success -> StudentAttendanceContent(data = s.data, onBack = onBack)
    }
}

@Composable
private fun StudentAttendanceContent(data: StudentAttendanceData, onBack: () -> Unit) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = {
            TimeText(
                modifier = Modifier.padding(top = 4.dp),
                timeTextStyle = TimeTextDefaults.timeTextStyle(color = CyanAccent, fontSize = 11.sp)
            )
        }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Black),
            contentPadding = PaddingValues(
                start = 10.dp, end = 10.dp, top = 28.dp, bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎒 Student Attendance",
                        color = GoldenYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SummaryPill(
                            label = "Present",
                            value = data.totalPresent.toString(),
                            color = MoneyGreen
                        )
                        SummaryPill(
                            label = "Absent",
                            value = data.totalAbsent.toString(),
                            color = CoralRed
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%.1f%% Overall".format(data.attendancePercentage),
                        color = GoldenYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Overall Progress
            item {
                AttendanceProgressBar(
                    progress = data.attendancePercentage / 100f,
                    color = MoneyGreen
                )
            }

            // Class-wise Header
            item {
                Text(
                    text = "Class-wise Breakdown",
                    color = MutedGray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Class rows
            items(data.classWise.size) { index ->
                ClassAttendanceRow(record = data.classWise[index])
            }

            // Back Button
//            item {
//                Chip(
//                    onClick = onBack,
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ChipDefaults.chipColors(backgroundColor = CardBorder),
//                    label = {
//                        Text(
//                            "← Back to Dashboard",
//                            color = CyanAccent,
//                            fontSize = 11.sp,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//                )
//            }
        }
    }
}

@Composable
fun ClassAttendanceRow(record: ClassAttendance) {
    val pct = record.present.toFloat() / record.total
    val barColor = when {
        pct >= 0.90f -> MoneyGreen
        pct >= 0.75f -> GoldenYellow
        else         -> CoralRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(grey)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = record.className,
                color = SoftWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${record.present}/${record.total}",
                color = barColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(50))
                .background(CardBorder)
        ) {
            val animatedPct by androidx.compose.animation.core.animateFloatAsState(
                targetValue = pct,
                animationSpec = androidx.compose.animation.core.tween(600),
                label = "pct"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPct)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
    }
}