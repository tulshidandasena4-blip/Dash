package com.example.ui.entry

import android.app.DatePickerDialog
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessInfo
import com.example.ui.MainViewModel
import com.example.ui.theme.DieselBlue
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.NetProfitGreen
import com.example.ui.theme.PetrolOrange
import com.example.ui.theme.TruckAmber
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DailyVehicleEntryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val form by viewModel.vehicleForm.collectAsState()
    val dayExpenditures by viewModel.formDateExpenditures.collectAsState()
    val context = LocalContext.current

    val totalDayExp = dayExpenditures.sumOf { it.amount }
    val netDayCollection = form.grandTotalCollection - totalDayExp

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val cal = remember(form.date) {
        val c = Calendar.getInstance()
        try {
            val parts = form.date.split("-")
            c.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        } catch (_: Exception) {}
        c
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newDate = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                viewModel.onFormDateSelected(newDate)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_entry_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selector Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_selector_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            val c = Calendar.getInstance()
                            try {
                                val parts = form.date.split("-")
                                c.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                                c.add(Calendar.DAY_OF_YEAR, -1)
                                val prevDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
                                viewModel.onFormDateSelected(prevDate)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.testTag("btn_prev_date")
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Previous Day", modifier = Modifier.size(18.dp))
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { datePickerDialog.show() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Pick date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = FormatUtils.formatDisplayDate(form.date),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (form.existingEntryId != null) "Editing Existing Record" else "New Entry",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (form.existingEntryId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val c = Calendar.getInstance()
                            try {
                                val parts = form.date.split("-")
                                c.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                                c.add(Calendar.DAY_OF_YEAR, 1)
                                val nextDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
                                viewModel.onFormDateSelected(nextDate)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.testTag("btn_next_date")
                    ) {
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Day", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Success notification banner
        if (form.isSavedSuccess) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daily record saved permanently for ${FormatUtils.formatDisplayDate(form.date)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 1. PETROL VEHICLE ENTRY CARD
        item {
            VehicleCategoryEntryCard(
                categoryName = "Petrol",
                icon = Icons.Default.LocalGasStation,
                brandColor = PetrolOrange,
                countText = form.petrolCountText,
                onCountChange = { viewModel.updateForm(petrolCount = it) },
                amountText = form.petrolAmountText,
                onAmountChange = { viewModel.updateForm(petrolAmount = it) },
                isRateFixed = false,
                fixedRateInfo = null,
                calculatedAmount = form.petrolAmount,
                testTagPrefix = "petrol"
            )
        }

        // 2. DIESEL VEHICLE ENTRY CARD
        item {
            VehicleCategoryEntryCard(
                categoryName = "Diesel",
                icon = Icons.Default.DirectionsCar,
                brandColor = DieselBlue,
                countText = form.dieselCountText,
                onCountChange = { viewModel.updateForm(dieselCount = it) },
                amountText = form.dieselAmountText,
                onAmountChange = { viewModel.updateForm(dieselAmount = it) },
                isRateFixed = false,
                fixedRateInfo = null,
                calculatedAmount = form.dieselAmount,
                testTagPrefix = "diesel"
            )
        }

        // 3. TRUCK VEHICLE ENTRY CARD (Fixed rate ₹180 per vehicle auto calculation)
        item {
            VehicleCategoryEntryCard(
                categoryName = "Truck",
                icon = Icons.Default.LocalShipping,
                brandColor = TruckAmber,
                countText = form.truckCountText,
                onCountChange = { viewModel.updateForm(truckCount = it) },
                amountText = form.truckAmount.toString(),
                onAmountChange = {},
                isRateFixed = true,
                fixedRateInfo = "Fixed rate: ₹180 per vehicle • Auto calculated (Count × ₹180)",
                calculatedAmount = form.truckAmount,
                testTagPrefix = "truck"
            )
        }

        // 4. LIVE AUTOMATIC CALCULATION CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_calculation_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Automatic Daily Calculation (${FormatUtils.formatDisplayDate(form.date)})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CalculationRow(label = "Total Petrol Vehicles", value = "${form.petrolCount}")
                    CalculationRow(label = "Total Diesel Vehicles", value = "${form.dieselCount}")
                    CalculationRow(label = "Total Truck Vehicles", value = "${form.truckCount}")

                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(modifier = Modifier.fillMaxWidth().height(1.dp), color = MaterialTheme.colorScheme.outlineVariant) {}
                    Spacer(modifier = Modifier.height(4.dp))

                    CalculationRow(label = "Total Vehicles", value = "${form.totalVehicles}", isBold = true)

                    Spacer(modifier = Modifier.height(8.dp))
                    CalculationRow(label = "Total Petrol Collection", value = FormatUtils.formatCurrency(form.petrolAmount))
                    CalculationRow(label = "Total Diesel Collection", value = FormatUtils.formatCurrency(form.dieselAmount))
                    CalculationRow(label = "Total Truck Collection", value = FormatUtils.formatCurrency(form.truckAmount))

                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(modifier = Modifier.fillMaxWidth().height(1.dp), color = MaterialTheme.colorScheme.outlineVariant) {}
                    Spacer(modifier = Modifier.height(4.dp))

                    CalculationRow(
                        label = "Grand Total Collection",
                        value = FormatUtils.formatCurrency(form.grandTotalCollection),
                        isBold = true,
                        valueColor = MaterialTheme.colorScheme.primary
                    )

                    CalculationRow(
                        label = "Total Others Expenditure",
                        value = FormatUtils.formatCurrency(totalDayExp),
                        valueColor = ExpenseRed
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(modifier = Modifier.fillMaxWidth().height(1.5.dp), color = MaterialTheme.colorScheme.outline) {}
                    Spacer(modifier = Modifier.height(4.dp))

                    CalculationRow(
                        label = "Net Collection (Grand Total − Expenditure)",
                        value = FormatUtils.formatCurrency(netDayCollection),
                        isBold = true,
                        valueColor = if (netDayCollection >= 0) NetProfitGreen else ExpenseRed
                    )
                }
            }
        }

        // Optional Remarks
        item {
            OutlinedTextField(
                value = form.notes,
                onValueChange = { viewModel.updateForm(notes = it) },
                label = { Text("Daily Notes / Remarks (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_notes"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )
        }

        // Action Buttons: Save/Update & Delete
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.saveVehicleEntry() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("btn_save_entry"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (form.existingEntryId != null) "Update Record" else "Save Daily Entry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (form.existingEntryId != null) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("btn_delete_entry"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry for ${FormatUtils.formatDisplayDate(form.date)}?") },
            text = { Text("This will remove the vehicle testing record for this date permanently. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVehicleEntry()
                        showDeleteConfirm = false
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
private fun VehicleCategoryEntryCard(
    categoryName: String,
    icon: ImageVector,
    brandColor: Color,
    countText: String,
    onCountChange: (String) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    isRateFixed: Boolean,
    fixedRateInfo: String?,
    calculatedAmount: Double,
    testTagPrefix: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_entry_$testTagPrefix"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brandColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = brandColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$categoryName Vehicles",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (fixedRateInfo != null) {
                        Text(
                            text = fixedRateInfo,
                            style = MaterialTheme.typography.labelSmall,
                            color = brandColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = countText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) onCountChange(input)
                    },
                    label = { Text("$categoryName Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_${testTagPrefix}_count"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
                            val curr = FormatUtils.parseIntOrZero(countText)
                            if (curr > 0) onCountChange((curr - 1).toString())
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrement", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = CircleShape,
                    color = brandColor.copy(alpha = 0.18f),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
                            val curr = FormatUtils.parseIntOrZero(countText)
                            onCountChange((curr + 1).toString())
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Increment", tint = brandColor, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isRateFixed) {
                Surface(
                    color = brandColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Truck Collection (Auto = Count × ₹180)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = brandColor
                        )
                        Text(
                            text = FormatUtils.formatCurrency(calculatedAmount),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = brandColor
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) onAmountChange(input)
                    },
                    label = { Text("$categoryName Collection Amount (₹)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_${testTagPrefix}_amount"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun CalculationRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else MaterialTheme.typography.bodySmall,
            color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
