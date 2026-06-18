package com.school.erp.watch.presentation.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import coil.compose.AsyncImage
import com.school.erp.watch.data.StaffInfo
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState


@Composable
fun StaffListScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.staffList.collectAsStateWithLifecycle()

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error   -> ErrorScreen(message = s.message) { viewModel.refresh() }
        is UiState.Success -> StaffListContent(
            staffList = s.data,
            onBack    = onBack
        )
    }
}

@Composable
private fun StaffListContent(
    staffList: List<StaffInfo>,
    onBack: () -> Unit,
) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "👨‍🏫 Staff Directory",
                        color = StaffBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${staffList.size} members",
                        color = MutedGray,
                        fontSize = 10.sp
                    )
                }
            }

            items(staffList.size) { index ->
                StaffDirectoryRow(info = staffList[index])
            }
        }
    }
}

@Composable
fun StaffDirectoryRow(info: StaffInfo) {
    val context = LocalContext.current

    // Remember the number to call after permission is granted
    var pendingNumber by remember { mutableStateOf("") }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingNumber.isNotEmpty()) {
            placeCall(context, pendingNumber)
            pendingNumber = ""
        }
    }

    fun onCallClick(number: String) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            placeCall(context, number)
        } else {
            pendingNumber = number
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25.dp))
            .background(grey)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(StaffBlue.copy(alpha = 0.2f))
            ) {
                if (info.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = info.photoUrl,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        text = info.name.take(1).uppercase(),
                        color = StaffBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.name,
                    color = SoftWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = info.role,
                    color = StaffBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
//                Text(text = "🏢 ", fontSize = 10.sp)
                Text(
                    text = info.department,
                    color = MutedGray,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            // Direct call button
            if (info.contactNumber.isNotEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1DB954).copy(alpha = 0.4f))
                        .clickable { onCallClick(info.contactNumber) }
                ) {
                    Text(
                        text = "📞",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun placeCall(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    } catch (e: SecurityException) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            context.startActivity(intent)
        } catch (ex: Exception) {
            // Dialer not found
        }
    }
}
