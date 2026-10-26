package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AggregateReportSummary
import com.example.data.model.BusinessInfo
import com.example.data.model.DailyReportRow
import com.example.data.model.ExpenditureEntity
import com.example.data.model.VehicleEntryEntity
import com.example.data.repository.ReportRepository
import com.example.util.FormatUtils
import com.example.util.PdfReportGenerator
import com.example.util.PrintUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

enum class AppTab {
    DASHBOARD,
    DAILY_ENTRY,
    EXPENDITURES,
    REPORTS
}

enum class ReportMode {
    DAILY,
    MONTHLY,
    YEARLY,
    HISTORY
}

data class VehicleFormState(
    val date: String = FormatUtils.getTodayIsoDate(),
    val petrolCountText: String = "",
    val petrolAmountText: String = "",
    val dieselCountText: String = "",
    val dieselAmountText: String = "",
    val truckCountText: String = "",
    val truckRate: Double = BusinessInfo.DEFAULT_TRUCK_RATE,
    val notes: String = "",
    val existingEntryId: Long? = null,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val petrolCount: Int get() = FormatUtils.parseIntOrZero(petrolCountText)
    val petrolAmount: Double get() = FormatUtils.parseDoubleOrZero(petrolAmountText)
    val dieselCount: Int get() = FormatUtils.parseIntOrZero(dieselCountText)
    val dieselAmount: Double get() = FormatUtils.parseDoubleOrZero(dieselAmountText)
    val truckCount: Int get() = FormatUtils.parseIntOrZero(truckCountText)
    val truckAmount: Double get() = truckCount * truckRate

    val totalVehicles: Int get() = petrolCount + dieselCount + truckCount
    val grandTotalCollection: Double get() = petrolAmount + dieselAmount + truckAmount
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(private val repository: ReportRepository) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Dashboard overall statistics
    val dashboardSummary: StateFlow<AggregateReportSummary> = repository.overallSummary
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AggregateReportSummary()
        )

    // Form state for daily vehicle entry
    private val _vehicleForm = MutableStateFlow(VehicleFormState())
    val vehicleForm: StateFlow<VehicleFormState> = _vehicleForm.asStateFlow()

    // Expenditures for currently selected form date
    val formDateExpenditures: StateFlow<List<ExpenditureEntity>> = _vehicleForm
        .flatMapLatest { form -> repository.getExpendituresByDate(form.date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All lifetime report rows (Date DESC)
    val allReportRows: StateFlow<List<DailyReportRow>> = repository.allReportRows
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All expenditures list
    val allExpenditures: StateFlow<List<ExpenditureEntity>> = repository.allExpenditures
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Report Filter states
    private val _reportMode = MutableStateFlow(ReportMode.DAILY)
    val reportMode: StateFlow<ReportMode> = _reportMode.asStateFlow()

    private val _reportSelectedDate = MutableStateFlow(FormatUtils.getTodayIsoDate())
    val reportSelectedDate: StateFlow<String> = _reportSelectedDate.asStateFlow()

    private val _reportSelectedMonth = MutableStateFlow(FormatUtils.getCurrentMonthPrefix())
    val reportSelectedMonth: StateFlow<String> = _reportSelectedMonth.asStateFlow()

    private val _reportSelectedYear = MutableStateFlow(FormatUtils.getCurrentYear())
    val reportSelectedYear: StateFlow<String> = _reportSelectedYear.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private data class FilterCriteria(
        val mode: ReportMode,
        val date: String,
        val month: String,
        val year: String,
        val query: String
    )

    private val filterCriteria = combine(
        reportMode,
        reportSelectedDate,
        reportSelectedMonth,
        reportSelectedYear,
        searchQuery
    ) { mode, date, month, year, query ->
        FilterCriteria(mode, date, month, year, query)
    }

    // Filtered Report rows based on active mode & search query
    val filteredReportRows: StateFlow<List<DailyReportRow>> = combine(
        allReportRows,
        filterCriteria
    ) { rows, criteria ->
        var list = when (criteria.mode) {
            ReportMode.DAILY -> rows.filter { it.date == criteria.date }
            ReportMode.MONTHLY -> rows.filter { it.date.startsWith(criteria.month) }
            ReportMode.YEARLY -> rows.filter { it.date.startsWith(criteria.year) }
            ReportMode.HISTORY -> rows
        }

        if (criteria.query.isNotBlank()) {
            val q = criteria.query.trim().lowercase()
            list = list.filter {
                it.date.contains(q) ||
                FormatUtils.formatDisplayDate(it.date).lowercase().contains(q)
            }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Report Summary based on filtered rows
    val filteredSummary: StateFlow<AggregateReportSummary> = filteredReportRows.combine(reportMode) { rows, _ ->
        var pVehicles = 0
        var dVehicles = 0
        var tVehicles = 0
        var pCollection = 0.0
        var dCollection = 0.0
        var tCollection = 0.0
        var totalExp = 0.0

        for (r in rows) {
            pVehicles += r.petrolCount
            dVehicles += r.dieselCount
            tVehicles += r.truckCount
            pCollection += r.petrolAmount
            dCollection += r.dieselAmount
            tCollection += r.truckAmount
            totalExp += r.expenditure
        }

        val totalVehicles = pVehicles + dVehicles + tVehicles
        val grandTotal = pCollection + dCollection + tCollection
        val net = grandTotal - totalExp

        AggregateReportSummary(
            totalPetrolVehicles = pVehicles,
            totalDieselVehicles = dVehicles,
            totalTruckVehicles = tVehicles,
            totalVehicles = totalVehicles,
            totalPetrolCollection = pCollection,
            totalDieselCollection = dCollection,
            totalTruckCollection = tCollection,
            grandTotalCollection = grandTotal,
            totalOthersExpenditure = totalExp,
            netCollection = net,
            daysCount = rows.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AggregateReportSummary()
    )

    init {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
            loadEntryForDate(FormatUtils.getTodayIsoDate())
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setReportMode(mode: ReportMode) {
        _reportMode.value = mode
    }

    fun setReportDate(date: String) {
        _reportSelectedDate.value = date
    }

    fun setReportMonth(yearMonth: String) {
        _reportSelectedMonth.value = yearMonth
    }

    fun setReportYear(year: String) {
        _reportSelectedYear.value = year
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onFormDateSelected(newDate: String) {
        viewModelScope.launch {
            loadEntryForDate(newDate)
        }
    }

    private suspend fun loadEntryForDate(date: String) {
        val existing = repository.getEntryByDateSync(date)
        if (existing != null) {
            _vehicleForm.value = VehicleFormState(
                date = date,
                petrolCountText = if (existing.petrolCount > 0) existing.petrolCount.toString() else "",
                petrolAmountText = if (existing.petrolAmount > 0.0) existing.petrolAmount.toString() else "",
                dieselCountText = if (existing.dieselCount > 0) existing.dieselCount.toString() else "",
                dieselAmountText = if (existing.dieselAmount > 0.0) existing.dieselAmount.toString() else "",
                truckCountText = if (existing.truckCount > 0) existing.truckCount.toString() else "",
                truckRate = existing.truckRate,
                notes = existing.notes,
                existingEntryId = existing.id,
                isSavedSuccess = false,
                errorMessage = null
            )
        } else {
            _vehicleForm.value = VehicleFormState(
                date = date,
                petrolCountText = "",
                petrolAmountText = "",
                dieselCountText = "",
                dieselAmountText = "",
                truckCountText = "",
                truckRate = BusinessInfo.DEFAULT_TRUCK_RATE,
                notes = "",
                existingEntryId = null,
                isSavedSuccess = false,
                errorMessage = null
            )
        }
    }

    fun updateForm(
        petrolCount: String? = null,
        petrolAmount: String? = null,
        dieselCount: String? = null,
        dieselAmount: String? = null,
        truckCount: String? = null,
        truckRate: Double? = null,
        notes: String? = null
    ) {
        _vehicleForm.value = _vehicleForm.value.copy(
            petrolCountText = petrolCount ?: _vehicleForm.value.petrolCountText,
            petrolAmountText = petrolAmount ?: _vehicleForm.value.petrolAmountText,
            dieselCountText = dieselCount ?: _vehicleForm.value.dieselCountText,
            dieselAmountText = dieselAmount ?: _vehicleForm.value.dieselAmountText,
            truckCountText = truckCount ?: _vehicleForm.value.truckCountText,
            truckRate = truckRate ?: _vehicleForm.value.truckRate,
            notes = notes ?: _vehicleForm.value.notes,
            isSavedSuccess = false,
            errorMessage = null
        )
    }

    fun saveVehicleEntry() {
        val form = _vehicleForm.value
        viewModelScope.launch {
            try {
                repository.saveVehicleEntry(
                    date = form.date,
                    petrolCount = form.petrolCount,
                    petrolAmount = form.petrolAmount,
                    dieselCount = form.dieselCount,
                    dieselAmount = form.dieselAmount,
                    truckCount = form.truckCount,
                    truckRate = form.truckRate,
                    notes = form.notes
                )
                val updated = repository.getEntryByDateSync(form.date)
                _vehicleForm.value = form.copy(
                    existingEntryId = updated?.id,
                    isSavedSuccess = true,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _vehicleForm.value = form.copy(errorMessage = "Save failed: ${e.message}")
            }
        }
    }

    fun deleteVehicleEntry() {
        val id = _vehicleForm.value.existingEntryId ?: return
        viewModelScope.launch {
            repository.deleteVehicleEntryById(id)
            loadEntryForDate(_vehicleForm.value.date)
        }
    }

    fun deleteVehicleEntryById(id: Long) {
        viewModelScope.launch {
            repository.deleteVehicleEntryById(id)
        }
    }

    // Expenditure operations
    fun addExpenditure(
        date: String,
        description: String,
        amount: Double,
        category: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveExpenditure(
                date = date,
                description = description,
                amount = amount,
                category = category
            )
            onComplete()
        }
    }

    fun updateExpenditure(
        id: Long,
        date: String,
        description: String,
        amount: Double,
        category: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveExpenditure(
                id = id,
                date = date,
                description = description,
                amount = amount,
                category = category
            )
            onComplete()
        }
    }

    fun deleteExpenditure(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenditureById(id)
        }
    }

    // PDF, Share, and Print operations
    fun generateAndSharePdf(context: Context) {
        viewModelScope.launch {
            val file = buildCurrentReportPdf(context)
            PrintUtils.sharePdfReport(context, file, "Dash PUC ${_reportMode.value.name} Report")
        }
    }

    fun generateAndPrintPdf(context: Context) {
        viewModelScope.launch {
            val file = buildCurrentReportPdf(context)
            PrintUtils.printPdfReport(context, file, "Dash_PUC_${_reportMode.value.name}_Report")
        }
    }

    fun shareWhatsAppSummary(context: Context) {
        val summary = filteredSummary.value
        val periodText = when (_reportMode.value) {
            ReportMode.DAILY -> FormatUtils.formatDisplayDate(_reportSelectedDate.value)
            ReportMode.MONTHLY -> FormatUtils.formatMonthYear(_reportSelectedMonth.value)
            ReportMode.YEARLY -> "Year ${_reportSelectedYear.value}"
            ReportMode.HISTORY -> "All Time History"
        }

        val text = buildString {
            appendLine("📋 *${BusinessInfo.NAME}*")
            appendLine("📍 ${BusinessInfo.ADDRESS}")
            appendLine("📞 Contact: ${BusinessInfo.MOBILE}")
            appendLine("🗓️ *${_reportMode.value.name} REPORT: $periodText*")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🚗 Petrol: ${summary.totalPetrolVehicles} veh • ${FormatUtils.formatCurrency(summary.totalPetrolCollection)}")
            appendLine("🚙 Diesel: ${summary.totalDieselVehicles} veh • ${FormatUtils.formatCurrency(summary.totalDieselCollection)}")
            appendLine("🚚 Truck: ${summary.totalTruckVehicles} veh • ${FormatUtils.formatCurrency(summary.totalTruckCollection)} (Fixed ₹180)")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🔢 *Total Vehicles:* ${summary.totalVehicles}")
            appendLine("💰 *Grand Total Collection:* ${FormatUtils.formatCurrency(summary.grandTotalCollection)}")
            appendLine("📉 *Total Others Expenditure:* ${FormatUtils.formatCurrency(summary.totalOthersExpenditure)}")
            appendLine("✅ *NET COLLECTION:* ${FormatUtils.formatCurrency(summary.netCollection)}")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("Report generated from Dash PUC Mobile App.")
        }

        PrintUtils.shareTextSummary(context, text, "Dash PUC Daily Report")
    }

    private suspend fun buildCurrentReportPdf(context: Context): File {
        val rows = filteredReportRows.value
        val summary = filteredSummary.value
        val mode = _reportMode.value
        val title = when (mode) {
            ReportMode.DAILY -> "Daily Testing & Financial Report"
            ReportMode.MONTHLY -> "Monthly Testing & Financial Report"
            ReportMode.YEARLY -> "Yearly Testing & Financial Report"
            ReportMode.HISTORY -> "Comprehensive Testing History Report"
        }
        val period = when (mode) {
            ReportMode.DAILY -> FormatUtils.formatDisplayDate(_reportSelectedDate.value)
            ReportMode.MONTHLY -> FormatUtils.formatMonthYear(_reportSelectedMonth.value)
            ReportMode.YEARLY -> "Year ${_reportSelectedYear.value}"
            ReportMode.HISTORY -> "Complete History (${rows.size} days recorded)"
        }

        val exps = if (mode == ReportMode.DAILY) {
            repository.getExpendituresByDateSync(_reportSelectedDate.value)
        } else {
            emptyList()
        }

        return PdfReportGenerator.generateReportPdf(
            context = context,
            reportTitle = title,
            periodText = period,
            rows = rows,
            summary = summary,
            expenditures = exps
        )
    }
}
