package com.example.ui.report

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyReportRow
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.ReportMode
import com.example.ui.theme.DieselBlue
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.NetProfitGreen
import com.example.ui.theme.PetrolOrange
import com.example.ui.theme.TruckAmber
import com.example.util.FormatUtils
import java.util.Calendar

@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onNavigateToDailyEntry: () -> Unit = {}
) {
    val reportMode by viewModel.reportMode.collectAsState()
    val reportDate by viewModel.reportSelectedDate.collectAsState()
    val reportMonth by viewModel.reportSelectedMonth.collectAsState()
    val reportYear by viewModel.reportSelectedYear.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filteredRows by viewModel.filteredReportRows.collectAsState()
    val filteredSummary by viewModel.filteredSummary.collectAsState()

    val context = LocalContext.current
    val tableScrollState = rememberScrollState()

    var rowToEditOrDelete by remember { mutableStateOf<DailyReportRow?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Date Picker for Daily filter
    val cal = remember(reportDate) {
        val c = Calendar.getInstance()
        try {
            val parts = reportDate.split("-")
            c.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        } catch (_: Exception) {}
        c
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                viewModel.setReportDate(newDate)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Report Mode Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportMode.values().forEach { mode ->
                    val isSelected = reportMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setReportMode(mode) },
                        label = {
                            Text(
                                text = when (mode) {
                                    ReportMode.DAILY -> "Daily"
                                    ReportMode.MONTHLY -> "Monthly"
                                    ReportMode.YEARLY -> "Yearly"
                                    ReportMode.HISTORY -> "History"
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_report_mode_${mode.name.lowercase()}")
                    )
                }
            }
        }

        // Sub-filter selector based on mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    when (reportMode) {
                        ReportMode.DAILY -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { datePickerDialog.show() }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Select Report Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Text(FormatUtils.formatDisplayDate(reportDate), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                                TextButton(onClick = { viewModel.setReportDate(FormatUtils.getTodayIsoDate()) }) {
                                    Text("Today")
                                }
                            }
                        }
                        ReportMode.MONTHLY -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Selected Month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(FormatUtils.formatMonthYear(reportMonth), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val currentMonth = FormatUtils.getCurrentMonthPrefix()
                                    OutlinedButton(
                                        onClick = { viewModel.setReportMonth(currentMonth) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Current Month", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        ReportMode.YEARLY -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Selected Year", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text("Year $reportYear", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                }

                                val currYear = FormatUtils.getCurrentYear()
                                OutlinedButton(
                                    onClick = { viewModel.setReportYear(currYear) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Current Year", fontSize = 12.sp)
                                }
                            }
                        }
                        ReportMode.HISTORY -> {
                            Text(
                                text = "Showing all recorded testing dates (${filteredRows.size} records)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Search box inside card
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Filter by date (e.g. 2026-10 or 26 Oct)...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_report_search"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Summary Metric Cards for Filtered Range
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_summary_kpi_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Report Summary Totals",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Vehicles", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("${filteredSummary.totalVehicles}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Total Collection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(FormatUtils.formatCurrency(filteredSummary.grandTotalCollection), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Expenditure", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(FormatUtils.formatCurrency(filteredSummary.totalOthersExpenditure), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ExpenseRed)
                        }
                        Column {
                            Text("Net Collection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = FormatUtils.formatCurrency(filteredSummary.netCollection),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (filteredSummary.netCollection >= 0) NetProfitGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // Export Actions: Download PDF, Print Report, Share Report
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.generateAndSharePdf(context) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_download_pdf"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.generateAndPrintPdf(context) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_print_report"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.shareWhatsAppSummary(context) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_share_report"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 12.sp)
                }
            }
        }

        // Table Header Label
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Report Breakdown Table",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "Tap row for actions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // THE SPECIFIED REPORT TABLE:
        // Date | Petrol Vehicles | Petrol Amount | Diesel Vehicles | Diesel Amount | Truck Vehicles | Truck Amount | Total Vehicles | Total Collection | Expenditure | Net Collection
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_table_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(tableScrollState)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(text = "Date", width = 95.dp, isHeader = true, align = TextAlign.Start)
                        TableCell(text = "Petrol Vehicles", width = 85.dp, isHeader = true, color = PetrolOrange)
                        TableCell(text = "Petrol Amount", width = 95.dp, isHeader = true, color = PetrolOrange)
                        TableCell(text = "Diesel Vehicles", width = 85.dp, isHeader = true, color = DieselBlue)
                        TableCell(text = "Diesel Amount", width = 95.dp, isHeader = true, color = DieselBlue)
                        TableCell(text = "Truck Vehicles", width = 85.dp, isHeader = true, color = TruckAmber)
                        TableCell(text = "Truck Amount", width = 95.dp, isHeader = true, color = TruckAmber)
                        TableCell(text = "Total Vehicles", width = 85.dp, isHeader = true)
                        TableCell(text = "Total Collection", width = 105.dp, isHeader = true, color = MaterialTheme.colorScheme.primary)
                        TableCell(text = "Expenditure", width = 95.dp, isHeader = true, color = ExpenseRed)
                        TableCell(text = "Net Collection", width = 105.dp, isHeader = true, color = NetProfitGreen)
                    }

                    if (filteredRows.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No records found for the selected period",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        filteredRows.forEachIndexed { index, row ->
                            val bg = if (index % 2 == 1) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }

                            Row(
                                modifier = Modifier
                                    .background(bg)
                                    .clickable { rowToEditOrDelete = row }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell(text = row.date, width = 95.dp, align = TextAlign.Start, fontWeight = FontWeight.SemiBold)
                                TableCell(text = "${row.petrolCount}", width = 85.dp)
                                TableCell(text = FormatUtils.formatCurrency(row.petrolAmount), width = 95.dp)
                                TableCell(text = "${row.dieselCount}", width = 85.dp)
                                TableCell(text = FormatUtils.formatCurrency(row.dieselAmount), width = 95.dp)
                                TableCell(text = "${row.truckCount}", width = 85.dp)
                                TableCell(text = FormatUtils.formatCurrency(row.truckAmount), width = 95.dp)
                                TableCell(text = "${row.totalVehicles}", width = 85.dp, fontWeight = FontWeight.Bold)
                                TableCell(text = FormatUtils.formatCurrency(row.totalCollection), width = 105.dp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                TableCell(text = FormatUtils.formatCurrency(row.expenditure), width = 95.dp, color = ExpenseRed)
                                TableCell(
                                    text = FormatUtils.formatCurrency(row.netCollection),
                                    width = 105.dp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (row.netCollection >= 0) NetProfitGreen else ExpenseRed
                                )
                            }

                            Surface(modifier = Modifier.fillMaxWidth().height(0.5.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) {}
                        }

                        // Table Totals Footer Row
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(text = "TOTAL", width = 95.dp, isHeader = true, align = TextAlign.Start)
                            TableCell(text = "${filteredSummary.totalPetrolVehicles}", width = 85.dp, isHeader = true)
                            TableCell(text = FormatUtils.formatCurrency(filteredSummary.totalPetrolCollection), width = 95.dp, isHeader = true)
                            TableCell(text = "${filteredSummary.totalDieselVehicles}", width = 85.dp, isHeader = true)
                            TableCell(text = FormatUtils.formatCurrency(filteredSummary.totalDieselCollection), width = 95.dp, isHeader = true)
                            TableCell(text = "${filteredSummary.totalTruckVehicles}", width = 85.dp, isHeader = true)
                            TableCell(text = FormatUtils.formatCurrency(filteredSummary.totalTruckCollection), width = 95.dp, isHeader = true)
                            TableCell(text = "${filteredSummary.totalVehicles}", width = 85.dp, isHeader = true)
                            TableCell(text = FormatUtils.formatCurrency(filteredSummary.grandTotalCollection), width = 105.dp, isHeader = true, color = MaterialTheme.colorScheme.primary)
                            TableCell(text = FormatUtils.formatCurrency(filteredSummary.totalOthersExpenditure), width = 95.dp, isHeader = true, color = ExpenseRed)
                            TableCell(
                                text = FormatUtils.formatCurrency(filteredSummary.netCollection),
                                width = 105.dp,
                                isHeader = true,
                                color = if (filteredSummary.netCollection >= 0) NetProfitGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Row Action Dialog: Edit or Delete for clicked date
    if (rowToEditOrDelete != null) {
        val row = rowToEditOrDelete!!
        AlertDialog(
            onDismissRequest = { rowToEditOrDelete = null },
            title = { Text("Record for ${FormatUtils.formatDisplayDate(row.date)}") },
            text = {
                Column {
                    Text("Total Vehicles: ${row.totalVehicles} (P: ${row.petrolCount}, D: ${row.dieselCount}, T: ${row.truckCount})")
                    Text("Total Collection: ${FormatUtils.formatCurrency(row.totalCollection)}")
                    Text("Expenditure: ${FormatUtils.formatCurrency(row.expenditure)}")
                    Text("Net Collection: ${FormatUtils.formatCurrency(row.netCollection)}", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onFormDateSelected(row.date)
                        viewModel.setTab(AppTab.DAILY_ENTRY)
                        onNavigateToDailyEntry()
                        rowToEditOrDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Daily Record")
                }
            },
            dismissButton = {
                if (row.vehicleEntryId != null) {
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirm = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                } else {
                    TextButton(onClick = { rowToEditOrDelete = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    if (showDeleteConfirm && rowToEditOrDelete?.vehicleEntryId != null) {
        val entryId = rowToEditOrDelete!!.vehicleEntryId!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry for ${rowToEditOrDelete?.date}?") },
            text = { Text("Are you sure you want to permanently delete this vehicle record?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVehicleEntryById(entryId)
                        showDeleteConfirm = false
                        rowToEditOrDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    align: TextAlign = TextAlign.End,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        textAlign = align,
        style = if (isHeader) {
            MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
        } else {
            MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                fontWeight = fontWeight ?: FontWeight.Normal
            )
        },
        color = if (color != Color.Unspecified) color
        else if (isHeader) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
    )
}
