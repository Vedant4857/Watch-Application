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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.*
import com.school.erp.watch.data.FeeTransaction
import com.school.erp.watch.presentation.theme.*
import com.school.erp.watch.viewmodel.DashboardViewModel
import com.school.erp.watch.viewmodel.UiState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FeesScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.feeTransactions.collectAsStateWithLifecycle()

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error   -> ErrorScreen(message = s.message) { viewModel.refresh() }
        is UiState.Success -> FeesContent(transactions = s.data, onBack = onBack)
    }
}

@Composable
private fun FeesContent(transactions: List<FeeTransaction>, onBack: () -> Unit) {
    val listState = rememberScalingLazyListState()
    val totalFees = transactions.sumOf { it.amount }
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

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
//            item {
//                Button(
//                    onClick = onBack,
//                    modifier = Modifier.size(20.dp)
//                ) {
//                    Text("←")
//                }
//            }
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💰 Fees Collected",
                        color = FeesGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatter.format(totalFees),
                        color = MoneyGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${transactions.size} transactions today",
                        color = MutedGray,
                        fontSize = 10.sp
                    )
                }
            }

            // Category Breakdown
            item {
                val grouped = transactions.groupBy { it.type }
                    .mapValues { it.value.sumOf { t -> t.amount } }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(grey)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "By Category",
                        color = MutedGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    grouped.forEach { (type, amount) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = type, color = SoftWhite.copy(0.8f), fontSize = 10.sp)
                            Text(
                                text = formatter.format(amount),
                                color = MoneyGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Transactions Label
            item {
                Text(
                    text = "All Transactions",
                    color = MutedGray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Transaction rows
            items(transactions.size) { index ->
                FeeTransactionRow(transaction = transactions[index], formatter = formatter)
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
fun FeeTransactionRow(transaction: FeeTransaction, formatter: NumberFormat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(grey)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.studentName,
                color = SoftWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                Text(
                    text = transaction.className,
                    color = MutedGray,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${transaction.type}",
                    color = PurpleAccent.copy(0.8f),
                    fontSize = 9.sp
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatter.format(transaction.amount),
                color = MoneyGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.time,
                color = MutedGray,
                fontSize = 8.sp
            )
        }
    }
}
