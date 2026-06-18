/**
 * DashboardScreen.kt
 *
 * All dashboard UI lives in this single file, organised into these sections:
 *
 *   1. DashboardScreen        — Entry point. Observes ViewModel state and routes to
 *                               Loading / Error / Success screens.
 *
 *   2. DashboardContent       — Full dashboard layout (Scaffold + scrollable card list).
 *
 *   3. DashboardHeader        — Principal name + date strip at the top of the list.
 *
 *   4. MetricCard             — A tappable KPI card. Used for every data row.
 *
 *   5. MetricIcon             — Left-side icon area inside MetricCard.
 *                               Renders either a progress arc or a plain circle.
 *
 *   6. MetricTextColumn       — Right-side label / value / sub-value stack inside MetricCard.
 *
 *   7. CircularProgressArc    — Canvas-drawn arc that visualises an attendance ratio.
 *
 *   8. LoadingScreen          — Full-screen spinner shown while data is being fetched.
 *
 *   9. ErrorScreen            — Full-screen error message + Retry button.
 *
 *  10. rememberGlowAlpha      — Animation helper that drives the MetricCard border pulse.
 *
 *  11. Extension helpers      — toIndianCurrency(), staffAttendanceRatio,
 *                               studentAttendanceRatio.
 */
package com.school.erp.watch.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import com.school.erp.watch.data.DashboardStats
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState
import java.text.NumberFormat
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Animation constants — MetricCard border glow
// ─────────────────────────────────────────────────────────────────────────────

/** Lowest opacity the glowing border reaches during its pulse cycle. */
private const val GLOW_MIN_ALPHA = 0.3f

/** Highest opacity the glowing border reaches during its pulse cycle. */
private const val GLOW_MAX_ALPHA = 0.7f

/** Duration (ms) of one half-cycle of the glow pulse (dim → bright or bright → dim). */
private const val GLOW_DURATION_MS = 1500

// ─────────────────────────────────────────────────────────────────────────────
// 1. DashboardScreen — entry point, state observation + routing
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Root composable for the Dashboard feature.
 *
 * Responsibilities (intentionally limited):
 *  - Triggers a data refresh when the screen first enters the composition.
 *  - Collects [DashboardViewModel.dashboardStats] as Compose state.
 *  - Routes to the correct child composable based on the current [UiState].
 *
 * No layout or styling logic lives here; all visual work is in child composables.
 *
 * @param viewModel                     Provides dashboard data and exposes [UiState].
 * @param onNavigateToStaffAttendance   Called when the user taps the Staff Attendance card.
 * @param onNavigateToStudentAttendance Called when the user taps the Student Attendance card.
 * @param onNavigateToFees              Called when the user taps the Fees card.
 * @param onNavigateToAdmissions        Called when the user taps the Admissions card.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToStaffAttendance: () -> Unit,
    onNavigateToStudentAttendance: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToAdmissions: () -> Unit,
    onNavigateToStaffList: () -> Unit,
    onNavigateToStudentList: () -> Unit,
    onNavigateToStaffLeaves: () -> Unit,
    onNavigateToStudentLeaves: () -> Unit
) {
    // Fetch fresh data every time this screen enters the composition.
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    // Collect the StateFlow as Compose state, respecting the lifecycle.
    val statsState by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val staffLeavesState by viewModel.staffLeaves.collectAsStateWithLifecycle()
    val studentLeavesState by viewModel.studentLeaves.collectAsStateWithLifecycle()

    val pendingStaffLeaves = (staffLeavesState as? UiState.Success)?.data?.count { it.status == "pending" } ?: 0
    val pendingStudentLeaves = (studentLeavesState as? UiState.Success)?.data?.count { it.status == "pending" } ?: 0

    // Route to the correct screen based on the current data-loading state.
    when (val state = statsState) {

        is UiState.Loading -> {
            // Data is being fetched — show a spinner.
            LoadingScreen()
        }

        is UiState.Error -> {
            // Something went wrong — show the error message and a retry button.
            ErrorScreen(
                message = state.message,
                onRetry = viewModel::refresh    // Method reference; no extra lambda needed.
            )
        }

        is UiState.Success -> {
            // Data is ready — render the full dashboard.
            DashboardContent(
                stats                         = state.data,
                pendingStaffLeaves            = pendingStaffLeaves,
                pendingStudentLeaves          = pendingStudentLeaves,
                onNavigateToStaffAttendance   = onNavigateToStaffAttendance,
                onNavigateToStudentAttendance = onNavigateToStudentAttendance,
                onNavigateToFees              = onNavigateToFees,
                onNavigateToAdmissions        = onNavigateToAdmissions,
                onNavigateToStaffList         = onNavigateToStaffList,
                onNavigateToStudentList       = onNavigateToStudentList,
                onNavigateToStaffLeaves       = onNavigateToStaffLeaves,
                onNavigateToStudentLeaves     = onNavigateToStudentLeaves
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. DashboardContent — Scaffold + scrollable card list
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen dashboard composable rendered on a successful data load.
 *
 * Wraps everything in a Wear OS [Scaffold] that provides:
 *  - A scroll-position indicator on the right edge of the round display.
 *  - Top/bottom vignette to soften content near the circular bezels.
 *  - The system clock arc in [CyanAccent] at the very top.
 *
 * List order inside the [ScalingLazyColumn]:
 *   1. DashboardHeader    — principal name + date
 *   2. Staff Attendance   — with circular progress arc
 *   3. Student Attendance — with circular progress arc
 *   4. Fees Collected     — plain icon (no ratio)
 *   5. New Admissions     — plain icon (no ratio)
 *   6. Footer hint        — "Tap card for details"
 */
@Composable
private fun DashboardContent(
    stats: DashboardStats,
    pendingStaffLeaves: Int,
    pendingStudentLeaves: Int,
    onNavigateToStaffAttendance: () -> Unit,
    onNavigateToStudentAttendance: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToAdmissions: () -> Unit,
    onNavigateToStaffList: () -> Unit,
    onNavigateToStudentList: () -> Unit,
    onNavigateToStaffLeaves: () -> Unit,
    onNavigateToStudentLeaves: () -> Unit
) {
    // Hoisted scroll state so the Scaffold's PositionIndicator can read it.
    val scalingLazyListState = rememberScalingLazyListState()

    Scaffold(
        // Thin scroll-progress bar on the right edge of the watch face.
        positionIndicator = {
            PositionIndicator(scalingLazyListState = scalingLazyListState)
        },

        // Fades content near the top and bottom circular edges.
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },

        // System clock displayed as an arc above the list content.
        timeText = {
            TimeText(
                modifier = Modifier.padding(top = 4.dp),
                timeTextStyle = TimeTextDefaults.timeTextStyle(
                    color    = CyanAccent,
                    fontSize = 13.sp
                )
            )
        }
    ) {
        // Wear OS equivalent of LazyColumn — handles scaling near the display edges.
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Black),
            state  = scalingLazyListState,
            contentPadding = PaddingValues(
                start  = 10.dp,
                end    = 10.dp,
                top    = 30.dp,   // Extra top padding so the time arc doesn't overlap item 1.
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── 1. Header ────────────────────────────────────────────────────
            item {
                DashboardHeader(stats = stats)
            }

            item {
                MetricCard(
                    icon        = "👩‍🏫",
                    label       = "Staff Directory",
                    value       = "All Staff (${stats.totalStaff})",
                    subValue    = "View complete list",
                    accentColor = StaffBlue,
                    percentage  = null,
                    onClick     = onNavigateToStaffList
                )
            }

            // ── 7. Student Directory ──────────────────────────────────────────
            item {
                MetricCard(
                    icon        = "👩‍🎓",
                    label       = "Student Directory",
                    value       = "All Students (${stats.totalStudents})",
                    subValue    = "View complete list",
                    accentColor = GoldenYellow,
                    percentage  = null,
                    onClick     = onNavigateToStudentList
                )
            }

            // ── 2. Staff Attendance ──────────────────────────────────────────
            // percentage is provided → MetricCard will render a circular arc.
            item {
                MetricCard(
                    icon        = "👨‍🏫",
                    label       = "Staff Attendance",
                    value       = "${stats.staffPresent}/${stats.totalStaff}",
                    subValue    = "${stats.staffAbsent} absent",
                    accentColor = StaffBlue,
                    percentage  = stats.staffAttendanceRatio,   // Safe 0f–1f ratio.
                    onClick     = onNavigateToStaffAttendance
                )
            }

            // ── 3. Student Attendance ────────────────────────────────────────
            // Golden-yellow arc visually differentiates it from the staff card.
            item {
                MetricCard(
                    icon        = "🎒",
                    label       = "Student Attendance",
                    value       = "${stats.studentsPresent}/${stats.totalStudents}",
                    subValue    = "${stats.studentsAbsent} absent",
                    accentColor = GoldenYellow,
                    percentage  = stats.studentAttendanceRatio, // Safe 0f–1f ratio.
                    onClick     = onNavigateToStudentAttendance
                )
            }

            // ── 4. Fees Collected ────────────────────────────────────────────
            // Not a ratio → percentage = null → plain circle icon is shown instead of arc.
            item {
                MetricCard(
                    icon        = "💰",
                    label       = "Fees Collected",
                    value       = stats.feesCollected.toIndianCurrency(),
                    subValue    = "${stats.feeTransactionCount} transactions",
                    accentColor = MoneyGreen,
                    percentage  = null,
                    onClick     = onNavigateToFees
                )
            }

            // ── 5. New Admissions ────────────────────────────────────────────
            // A simple daily count; no ratio, so no arc.
            item {
                MetricCard(
                    icon        = "✨",
                    label       = "New Admissions",
                    value       = stats.newAdmissions.toString(),
                    subValue    = "Today",
                    accentColor = AdmissionPurple,
                    percentage  = null,
                    onClick     = onNavigateToAdmissions
                )
            }

            // ── 6. Staff Directory ────────────────────────────────────────────


            // ── 8. Staff Leaves ───────────────────────────────────────────────
            item {
                MetricCard(
                    icon        = "🌴",
                    label       = "Staff Leaves",
                    value       = if (pendingStaffLeaves > 0) "$pendingStaffLeaves Pending" else "No Pending",
                    subValue    = "Review & Approve",
                    accentColor = LeaveOrange,
                    percentage  = null,
                    onClick     = onNavigateToStaffLeaves
                )
            }

            // ── 9. Student Leaves ─────────────────────────────────────────────
            item {
                MetricCard(
                    icon        = "📝",
                    label       = "Student Leaves",
                    value       = if (pendingStudentLeaves > 0) "$pendingStudentLeaves Pending" else "No Pending",
                    subValue    = "Review & Approve",
                    accentColor = LeaveOrange,
                    percentage  = null,
                    onClick     = onNavigateToStudentLeaves
                )
            }

            // ── 10. Footer hint ───────────────────────────────────────────────
            item {
                Text(
                    text      = "Tap specific card for details",
                    color     = MutedGray,
                    fontSize  = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. DashboardHeader — principal name + date strip
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Shows three stacked text lines at the top of the dashboard list:
 *  - "🏫 Principal" role label in amber.
 *  - The principal's name (truncated with ellipsis if too long for the watch face).
 *  - Today's date in muted grey.
 */
@Composable
private fun DashboardHeader(stats: DashboardStats) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.fillMaxWidth()
    ) {
        // Role label — amber colour signals context / authority at a glance.
        Text(
            text       = "🏫 Principal",
            color      = Color(0xFFFBBF24),
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Principal's name — truncated so it never wraps onto a second line.
        Text(
            text       = stats.principalName,
            color      = Color(0xFFF8FAFC),
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )

        // Today's date string, e.g. "Mon, 15 Jun 2026".
        Text(
            text     = stats.date,
            color    = MutedGray,
            fontSize = 10.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. MetricCard — tappable KPI card
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A full-width Wear OS [Chip] that displays one dashboard metric.
 *
 * Visual layout (left → right):
 *   ┌─────────────────────────────────────────────┐
 *   │  [Icon / Arc]   Label   (grey,   9 sp)      │
 *   │                 Value   (accent, 14 sp bold)│
 *   │                 SubVal  (white,  9 sp)      │
 *   └─────────────────────────────────────────────┘
 *
 * The card border pulses between [GLOW_MIN_ALPHA] and [GLOW_MAX_ALPHA]
 * using the card's own accent colour, making each card feel "alive".
 *
 * @param icon        Emoji for the left icon area (e.g. "👨‍🏫", "💰").
 * @param label       Short descriptor above the value (e.g. "Staff Attendance").
 * @param value       Primary metric, coloured with [accentColor] (e.g. "42/50").
 * @param subValue    Secondary detail below the value (e.g. "8 absent").
 * @param accentColor Drives the value text, progress arc, icon tint, and border glow.
 * @param percentage  0f–1f ratio for the arc. Pass null for a plain circle icon instead.
 * @param onClick     Fired when the user taps the card.
 */
@Composable
fun MetricCard(
    icon: String,
    label: String,
    value: String,
    subValue: String,
    accentColor: Color,
    percentage: Float?,
    onClick: () -> Unit
) {
    // Animated alpha that drives the border glow pulse.
    val glowAlpha by rememberGlowAlpha()

    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        onClick  = onClick,
        colors   = ChipDefaults.chipColors(
            backgroundColor = grey,       // Dark card background.
            contentColor    = SoftWhite   // Default tint for text/icons inside the chip.
        ),
        border = ChipDefaults.chipBorder(
            // Border alpha oscillates between GLOW_MIN_ALPHA and GLOW_MAX_ALPHA.
            borderStroke = BorderStroke(1.dp, accentColor.copy(alpha = glowAlpha))
        ),
        shape = RoundedCornerShape(20.dp),
        label = {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: arc+icon OR plain icon circle, depending on whether percentage is set.
                MetricIcon(
                    icon        = icon,
                    accentColor = accentColor,
                    percentage  = percentage
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Right: three stacked text lines.
                MetricTextColumn(
                    label       = label,
                    value       = value,
                    subValue    = subValue,
                    accentColor = accentColor
                )
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. MetricIcon — left icon area inside MetricCard
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Renders the left section of a [MetricCard] in one of two styles:
 *
 * Arc variant   (percentage != null) — emoji centred inside a [CircularProgressArc].
 *                                      42 dp box to give the arc stroke enough room.
 *
 * Circle variant (percentage == null) — emoji inside a small tinted circle.
 *                                       38 dp box; used for non-ratio metrics.
 */
@Composable
private fun MetricIcon(
    icon: String,
    accentColor: Color,
    percentage: Float?
) {
    if (percentage != null) {
        // Arc variant — progress ring drawn behind the emoji.
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier.size(42.dp)
        ) {
            CircularProgressArc(
                progress = percentage,
                color    = accentColor,
                size     = 42
            )
            Text(text = icon, fontSize = 14.sp)
        }
    } else {
        // Plain circle variant — semi-transparent tinted background behind the emoji.
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f))
        ) {
            Text(text = icon, fontSize = 16.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. MetricTextColumn — right-side text stack inside MetricCard
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Three vertically stacked text lines shown on the right side of a [MetricCard]:
 *
 *  [label]    — 9 sp, muted grey   — category name, e.g. "Staff Attendance"
 *  [value]    — 14 sp, accent bold — primary metric, e.g. "42/50"
 *  [subValue] — 9 sp, soft white   — secondary detail, e.g. "8 absent"
 *
 * The value line is capped at one line with ellipsis overflow — important for
 * currency strings that can be long on small watch displays.
 */
@Composable
private fun MetricTextColumn(
    label: String,
    value: String,
    subValue: String,
    accentColor: Color
) {
    Column {
        // Category label — small and muted so it doesn't compete with the value.
        Text(
            text       = label,
            color      = MutedGray,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium
        )

        // Primary metric — largest text in the card; accent colour draws the eye here first.
        Text(
            text       = value,
            color      = accentColor,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines   = 1,                      // Never wraps; truncates instead.
            overflow   = TextOverflow.Ellipsis
        )

        // Secondary detail — dimmed so it reads as supporting info, not a headline.
        Text(
            text     = subValue,
            color    = SoftWhite.copy(alpha = 0.6f),
            fontSize = 9.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. CircularProgressArc — Canvas-drawn attendance ring
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Draws a circular arc on a [Canvas] to visualise a 0–1 [progress] ratio.
 *
 * Layers (bottom → top):
 *  • Track — full 360° ring in a faint tint (15% opacity) of [color].
 *            Always visible, giving context for the total capacity.
 *  • Fill  — shorter arc sweeping clockwise from 12 o'clock (−90°),
 *            proportional to [progress]. Animates smoothly on value changes.
 *
 * Both arcs share the same stroke width and rounded end-caps so the fill
 * sits cleanly over the track without gaps.
 *
 * @param progress 0f (empty) to 1f (full). Callers should guard against values outside this range.
 * @param color    Colour for the filled arc; track is this colour at 15% opacity.
 * @param size     Width and height of the composable in dp (always square).
 */
@Composable
fun CircularProgressArc(
    progress: Float,
    color: Color,
    size: Int
) {
    // Smoothly animate any change in progress over 1 second with a deceleration curve.
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label         = "arcProgress"
    )

    Canvas(modifier = Modifier.size(size.dp)) {

        // ── Geometry setup ───────────────────────────────────────────────────

        val strokeWidth = 3.dp.toPx()
        val diameter    = this.size.minDimension
        // Inset the radius by half the stroke width so the arc doesn't clip at the edges.
        val radius      = (diameter - strokeWidth) / 2f
        val center      = Offset(this.size.width / 2f, this.size.height / 2f)

        // Bounding box for both arcs — a square centred on the canvas.
        val arcTopLeft = Offset(center.x - radius, center.y - radius)
        val arcSize    = Size(radius * 2, radius * 2)

        // Rounded caps give a pill-end look where the arc starts and stops.
        val arcStroke  = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        // ── Track (faint full ring) ──────────────────────────────────────────
        drawArc(
            color      = color.copy(alpha = 0.15f),
            startAngle = -90f,   // 12 o'clock position.
            sweepAngle = 360f,   // Full ring — always drawn entirely.
            useCenter  = false,  // Stroke only; no pie-slice fill.
            style      = arcStroke,
            topLeft    = arcTopLeft,
            size       = arcSize
        )

        // ── Fill (progress portion in full colour) ───────────────────────────
        drawArc(
            color      = color,
            startAngle = -90f,                      // Always starts at 12 o'clock.
            sweepAngle = 360f * animatedProgress,   // Clockwise sweep proportional to progress.
            useCenter  = false,
            style      = arcStroke,
            topLeft    = arcTopLeft,
            size       = arcSize
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 8. LoadingScreen — full-screen spinner
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen overlay shown while dashboard data is being fetched.
 *
 * Displays a [CircularProgressIndicator] in [CyanAccent] above a "Loading…" label.
 * Background is [DeepNavy] — consistent with [ErrorScreen] to avoid jarring
 * colour flashes when the state transitions between Loading and Error.
 */
@Composable
fun LoadingScreen() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Indeterminate spinner — no progress value needed here.
            CircularProgressIndicator(
                indicatorColor = CyanAccent,
                trackColor     = CardBorder
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = "UEST",
                color    = ElectricBlue,
                fontSize = 12.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 9. ErrorScreen — full-screen error + retry
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen error state shown when the ViewModel emits [UiState.Error].
 *
 * Displays:
 *  • A "⚠️" warning emoji as an immediate visual signal.
 *  • The error [message] in [CoralRed] so it reads as a problem at a glance.
 *  • A "Retry" [Chip] in [ElectricBlue] that calls [onRetry] when tapped.
 *
 * Content is centred with 16 dp horizontal padding to keep text away from
 * the circular display edges on any watch size.
 *
 * @param message Human-readable description of what went wrong.
 * @param onRetry Callback that triggers a fresh data fetch (usually [DashboardViewModel.refresh]).
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(16.dp)
        ) {
            // Warning icon — large enough to immediately signal a problem state.
            Text(text = "⚠️", fontSize = 24.sp)

            Spacer(modifier = Modifier.height(4.dp))

            // Error detail in coral red; centred so it reads cleanly on round displays.
            Text(
                text      = message,
                color     = CoralRed,
                fontSize  = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Retry button — electric blue stands out clearly against the dark background.
            Chip(
                onClick = onRetry,
                colors  = ChipDefaults.chipColors(backgroundColor = ElectricBlue),
                label   = {
                    Text(text = "Retry", color = SoftWhite, fontSize = 11.sp)
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 10. rememberGlowAlpha — MetricCard border-pulse animation
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Creates and remembers an infinite animation that oscillates a [Float] alpha
 * between [GLOW_MIN_ALPHA] and [GLOW_MAX_ALPHA].
 *
 * Design choices:
 *  - [EaseInOutSine] easing makes the pulse feel organic, not mechanical.
 *  - [RepeatMode.Reverse] flows dim → bright → dim without a hard reset at the end.
 *
 * Extracted into its own function so the glow setup is readable in isolation
 * and easily reusable by other composables in this file.
 *
 * @return A [State<Float>] whose value continuously cycles between the two alpha constants.
 */
@Composable
private fun rememberGlowAlpha(): State<Float> {
    val transition = rememberInfiniteTransition(label = "cardBorderGlow")
    return transition.animateFloat(
        initialValue  = GLOW_MIN_ALPHA,
        targetValue   = GLOW_MAX_ALPHA,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = GLOW_DURATION_MS, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse   // Reverse instead of snap back to start.
        ),
        label = "glowAlpha"
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 11. Extension helpers — formatting & safe ratio computation
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Formats any [Number] as Indian Rupee currency, e.g. "₹1,20,000.00".
 * Using an extension keeps the call site clean: `stats.feesCollected.toIndianCurrency()`.
 */
private fun Number.toIndianCurrency(): String =
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(this)

/**
 * Returns the staff attendance ratio as a value between 0f and 1f.
 * Guards against division-by-zero when [DashboardStats.totalStaff] is 0.
 */
private val DashboardStats.staffAttendanceRatio: Float
    get() = if (totalStaff > 0) staffPresent.toFloat() / totalStaff else 0f

/**
 * Returns the student attendance ratio as a value between 0f and 1f.
 * Guards against division-by-zero when [DashboardStats.totalStudents] is 0.
 */
private val DashboardStats.studentAttendanceRatio: Float
    get() = if (totalStudents > 0) studentsPresent.toFloat() / totalStudents else 0f