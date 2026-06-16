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
import com.school.erp.watch.data.AttendanceRecord
import com.school.erp.watch.data.StaffAttendanceData
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState

// ─────────────────────────────────────────────────────────────────────────────
// ENTRY-POINT SCREEN
// ─────────────────────────────────────────────────────────────────────────────

/**
 * StaffAttendanceScreen
 *
 * This is the top-level composable for the Staff Attendance page.
 * It watches the ViewModel's [staffAttendance] StateFlow and shows
 * a different UI depending on whether data is loading, failed, or ready.
 *
 * @param viewModel  Shared ViewModel that holds all dashboard data.
 * @param onBack     Lambda called when the user taps "Back to Dashboard".
 */
@Composable
fun StaffAttendanceScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
) {
    // Collect the latest UiState from the ViewModel.
    // collectAsStateWithLifecycle() safely stops collecting when the
    // composable leaves the screen (lifecycle-aware).
    val state by viewModel.staffAttendance.collectAsStateWithLifecycle()

    // Route to the correct composable based on the current state.
    when (val s = state) {
        is UiState.Loading -> LoadingScreen()                                          // Show spinner
        is UiState.Error   -> ErrorScreen(message = s.message) { viewModel.refresh() } // Show error + retry
        is UiState.Success -> StaffAttendanceContent(                                  // Show real data
            data      = s.data,
            onBack    = onBack,
            onRefresh = { viewModel.refresh() }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MAIN CONTENT (only shown on UiState.Success)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * StaffAttendanceContent
 *
 * The actual scrollable list rendered when data is available.
 * Uses [ScalingLazyColumn] — the Wear OS equivalent of LazyColumn —
 * which scales items near the edges of the round display.
 *
 * Layout order:
 *   1. Header summary (title + Present/Absent pills + percentage)
 *   2. Animated progress bar
 *   3. "Staff Details" section label
 *   4. One [StaffRow] per staff member
 *   5. Back button chip
 *
 * @param data       Fully loaded [StaffAttendanceData] from the ViewModel.
 * @param onBack     Navigates back to the dashboard.
 * @param onRefresh  Triggers a data reload in the ViewModel.
 */
@Composable
private fun StaffAttendanceContent(
    data: StaffAttendanceData,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    // State object that tracks scroll position;
    // also fed into PositionIndicator so the side-arc reflects scroll.
    val listState = rememberScalingLazyListState()

    Scaffold(
        // Side-arc scroll indicator (standard Wear OS pattern)
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },

        // Soft gradient fade at top & bottom edges — improves readability
        // on round displays where corners are cut off.
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },

        // Clock shown at the very top of the watch face
        timeText = {
            TimeText(
                modifier      = Modifier.padding(top = 4.dp),
                timeTextStyle = TimeTextDefaults.timeTextStyle(
                    color    = CyanAccent,
                    fontSize = 11.sp
                )
            )
        }
    ) {
        ScalingLazyColumn(
            state   = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Black),
            contentPadding = PaddingValues(
                start   = 10.dp,
                end     = 10.dp,
                top     = 28.dp,  // Extra top space so content clears the TimeText
                bottom  = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // ── 1. HEADER SUMMARY ──────────────────────────────────────────
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    // Screen title with staff emoji
                    Text(
                        text       = "👨‍🏫 Staff Attendance",
                        color      = StaffBlue,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Side-by-side Present / Absent count pills
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryPill(
                            label = "Present",
                            value = data.presentCount.toString(),
                            color = AttendanceGreen
                        )
                        SummaryPill(
                            label = "Absent",
                            value = data.absentCount.toString(),
                            color = CoralRed
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Overall attendance percentage (e.g. "87% Attendance")
                    Text(
                        text       = "%.0f%% Attendance".format(data.attendancePercentage),
                        color      = GoldenYellow,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── 2. PROGRESS BAR ───────────────────────────────────────────
            // Thin animated bar; fills proportionally to attendancePercentage.
            item {
                AttendanceProgressBar(
                    progress = data.attendancePercentage / 100f,  // Convert % → 0–1 range
                    color    = StaffBlue
                )
            }

            // ── 3. SECTION LABEL ──────────────────────────────────────────
            item {
                Text(
                    text     = "Staff Details",
                    color    = MutedGray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // ── 4. STAFF ROWS ─────────────────────────────────────────────
            // One row per staff member in the records list.
            // Using index-based items() so we can access data.records[index].
            items(data.records.size) { index ->
                StaffRow(record = data.records[index])
            }

            // ── 5. BACK BUTTON ────────────────────────────────────────────
//            item {
//                Chip(
//                    onClick  = onBack,
//                    modifier = Modifier.fillMaxWidth(),
//                    colors   = ChipDefaults.chipColors(backgroundColor = CardBorder),
//                    label    = {
//                        Text(
//                            text      = "← Back to Dashboard",
//                            color     = CyanAccent,
//                            fontSize  = 11.sp,
//                            textAlign = TextAlign.Center,
//                            modifier  = Modifier.fillMaxWidth()
//                        )
//                    }
//                )
//            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REUSABLE COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * StaffRow
 *
 * A single row showing one staff member's attendance status.
 *
 * Layout (left → right):
 *   [Status circle icon]  [Name & Role column]  [Check-in time OR "Absent"]
 *
 * Color logic:
 *   - Present → green  ✓
 *   - Absent  → red    ✗
 *
 * @param record  Data for one staff member (name, role, isPresent, checkInTime).
 */
@Composable
fun StaffRow(record: AttendanceRecord) {
    // Choose color and icon symbol based on attendance status
    val statusColor = if (record.isPresent) AttendanceGreen else CoralRed
    val statusIcon  = if (record.isPresent) "✓" else "✗"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(grey)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Status icon circle (✓ or ✗) ──────────────────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.2f))  // Soft tinted background
        ) {
            Text(
                text       = statusIcon,
                color      = statusColor,
                fontSize   = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // ── Name & Role ───────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {  // weight(1f) makes this stretch & push time to the right
            Text(
                text       = record.name,
                color      = SoftWhite,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis  // Long names get "..." instead of wrapping
            )
            Text(
                text     = record.role,
                color    = MutedGray,
                fontSize = 9.sp
            )
        }

        // ── Right-side status text ────────────────────────────────────────
        // Show check-in time if present, or "Absent" label if not.
        if (record.isPresent && record.checkInTime.isNotEmpty()) {
            Text(
                text     = record.checkInTime,
                color    = AttendanceGreen.copy(alpha = 0.8f),
                fontSize = 8.sp
            )
        } else if (!record.isPresent) {
            Text(
                text     = "Absent",
                color    = CoralRed.copy(alpha = 0.8f),
                fontSize = 8.sp
            )
        }
        // Edge case: present but checkInTime is empty → show nothing on the right
    }
}

/**
 * SummaryPill
 *
 * A small rounded badge showing a count with a label.
 * Example: [ 18  Present ]  or  [ 3  Absent ]
 *
 * @param label  Text shown after the number (e.g. "Present").
 * @param value  The number to display (e.g. "18").
 * @param color  Accent color applied to the text and a tinted background.
 */
@Composable
fun SummaryPill(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))  // Very subtle tint so it doesn't overpower
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Large bold number
        Text(
            text       = value,
            color      = color,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Smaller muted label
        Text(
            text     = label,
            color    = color.copy(alpha = 0.8f),
            fontSize = 9.sp
        )
    }
}

/**
 * AttendanceProgressBar
 *
 * A thin horizontal bar that fills from left to right based on [progress].
 * The fill animates smoothly over 800ms when the value changes.
 *
 * @param progress  Value between 0f (0%) and 1f (100%).
 * @param color     Fill color of the active portion.
 */
@Composable
fun AttendanceProgressBar(
    progress: Float,
    color: androidx.compose.ui.graphics.Color,
) {
    // Animate the progress value so the bar slides smoothly instead of jumping
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue    = progress,
        animationSpec  = androidx.compose.animation.core.tween(durationMillis = 800),
        label          = "bar"
    )

    // Outer container — full width, dark background (the "empty" track)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(50))   // Fully rounded pill shape
            .background(CardBorder)
    ) {
        // Inner fill — width is driven by animatedProgress (0f–1f = 0%–100%)
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}
