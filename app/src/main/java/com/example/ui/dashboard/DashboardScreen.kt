package com.example.ui.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessInfo
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.theme.DieselBlue
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.NetProfitGreen
import com.example.ui.theme.PetrolOrange
import com.example.ui.theme.TruckAmber
import com.example.util.FormatUtils

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onNavigateToDailyEntry: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("business_header_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = BusinessInfo.NAME,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = BusinessInfo.TAGLINE,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        // Call Quick Action
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${BusinessInfo.MOBILE}")
                                    }
                                    context.startActivity(dialIntent)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call testing station",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "📍 ${BusinessInfo.ADDRESS}  •  📞 ${BusinessInfo.MOBILE}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Overall Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Large Summary Cards Grid (2x2)
        // 1. Total Vehicles
        // 2. Total Collection
        // 3. Total Others Expenditure
        // 4. Net Collection
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Vehicles",
                    value = "${summary.totalVehicles}",
                    subtitle = "${summary.daysCount} recorded testing days",
                    icon = Icons.Default.DirectionsCar,
                    accentColor = MaterialTheme.colorScheme.primary,
                    testTag = "card_total_vehicles"
                )

                SummaryKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Collection",
                    value = FormatUtils.formatCurrency(summary.grandTotalCollection),
                    subtitle = "Gross revenue collected",
                    icon = Icons.Default.MonetizationOn,
                    accentColor = Color(0xFF0F6B4F),
                    testTag = "card_total_collection"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Others Expenditure",
                    value = FormatUtils.formatCurrency(summary.totalOthersExpenditure),
                    subtitle = "Operational & stationery cost",
                    icon = Icons.Default.Receipt,
                    accentColor = ExpenseRed,
                    testTag = "card_total_expenditure"
                )

                SummaryKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Net Collection",
                    value = FormatUtils.formatCurrency(summary.netCollection),
                    subtitle = "Collection − Expenditure",
                    icon = Icons.Default.TrendingUp,
                    accentColor = NetProfitGreen,
                    testTag = "card_net_collection"
                )
            }
        }

        // Vehicle Category Breakdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("breakdown_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vehicle Category Breakdown",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Petrol Row
                    VehicleCategoryRow(
                        categoryName = "Petrol Vehicles",
                        count = summary.totalPetrolVehicles,
                        amount = summary.totalPetrolCollection,
                        icon = Icons.Default.LocalGasStation,
                        color = PetrolOrange,
                        rateInfo = "2/3/4 Wheeler emission test"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Diesel Row
                    VehicleCategoryRow(
                        categoryName = "Diesel Vehicles",
                        count = summary.totalDieselVehicles,
                        amount = summary.totalDieselCollection,
                        icon = Icons.Default.DirectionsCar,
                        color = DieselBlue,
                        rateInfo = "Smoke opacity inspection"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Truck Row (Fixed rate ₹180)
                    VehicleCategoryRow(
                        categoryName = "Truck Vehicles",
                        count = summary.totalTruckVehicles,
                        amount = summary.totalTruckCollection,
                        icon = Icons.Default.LocalShipping,
                        color = TruckAmber,
                        rateInfo = "Fixed Rate: ₹180 per vehicle (Auto)"
                    )
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.setTab(AppTab.DAILY_ENTRY)
                        onNavigateToDailyEntry()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_quick_new_entry"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Daily Entry", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.setTab(AppTab.REPORTS)
                        onNavigateToReports()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_quick_reports"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reports", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.shareWhatsAppSummary(context) },
                    modifier = Modifier.testTag("btn_quick_share"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = accentColor,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun VehicleCategoryRow(
    categoryName: String,
    count: Int,
    amount: Double,
    icon: ImageVector,
    color: Color,
    rateInfo: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count vehicles • $rateInfo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Text(
            text = FormatUtils.formatCurrency(amount),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
