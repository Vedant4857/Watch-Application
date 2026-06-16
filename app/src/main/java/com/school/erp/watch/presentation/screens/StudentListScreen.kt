package com.school.erp.watch.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.material.*
import com.school.erp.watch.data.StudentInfo
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState

@Composable
fun StudentListScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
) {
    val pagingItems = viewModel.studentList.collectAsLazyPagingItems()

    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        timeText = {
            TimeText(
                modifier = Modifier.padding(top = 4.dp),
                timeTextStyle = TimeTextDefaults.timeTextStyle(
                    color = CyanAccent,
                    fontSize = 11.sp
                )
            )
        }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Black),
            contentPadding = PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 28.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎒 Student Directory",
                        color = GoldenYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            when (val loadState = pagingItems.loadState.refresh) {
                is LoadState.Loading -> {
                    item { LoadingScreen() }
                }
                is LoadState.Error -> {
                    item {
                        ErrorScreen(message = loadState.error.message ?: "Unknown error") {
                            pagingItems.retry()
                        }
                    }
                }
                else -> {
                    items(pagingItems.itemCount) { index ->
                        pagingItems[index]?.let { student ->
                            StudentDirectoryRow(info = student)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDirectoryRow(info: StudentInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(grey)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(GoldenYellow.copy(alpha = 0.2f))
        ) {
            Text(
                text = info.name.take(1).uppercase(),
                color = GoldenYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = info.name,
                color = SoftWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = info.className,
                    color = MutedGray,
                    fontSize = 9.sp
                )
                Text(
                    text = " • ${info.rollNumber}",
                    color = MutedGray.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            }
        }
    }
}
