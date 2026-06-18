package com.school.erp.watch.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import com.school.erp.watch.data.AdmissionRecord
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState

@Composable
fun AdmissionsScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.admissions.collectAsStateWithLifecycle()

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error   -> ErrorScreen(message = s.message) { viewModel.refresh() }
        is UiState.Success -> AdmissionsContent(admissions = s.data, onBack = onBack)
    }
}

@Composable
private fun AdmissionsContent(admissions: List<AdmissionRecord>, onBack: () -> Unit) {
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
                        text = "✨ New Admissions",
                        color = AdmissionPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AdmissionPurple.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = admissions.size.toString(),
                            color = AdmissionPurple,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Students enrolled today",
                        color = MutedGray,
                        fontSize = 9.sp
                    )
                }
            }

            // Admission cards
            items(admissions.size) { index ->
                AdmissionCard(record = admissions[index], number = index + 1)
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
fun AdmissionCard(record: AdmissionRecord, number: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(grey)
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(AdmissionPurple.copy(alpha = 0.25f))
            ) {
                Text(
                    text = number.toString(),
                    color = AdmissionPurple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = record.studentName,
                    color = SoftWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = record.className,
                    color = AdmissionPurple.copy(0.9f),
                    fontSize = 9.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(CardBorder)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Enrollment No.", color = MutedGray, fontSize = 8.sp)
                Text(
                    text = record.enrollmentNo,
                    color = CyanAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Time", color = MutedGray, fontSize = 8.sp)
                Text(
                    text = record.time,
                    color = GoldenYellow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Parent: ${record.parentName}", color = MutedGray, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
