package com.example.data.repository

import com.example.data.dao.ExpenditureDao
import com.example.data.dao.VehicleEntryDao
import com.example.data.model.AggregateReportSummary
import com.example.data.model.BusinessInfo
import com.example.data.model.DailyReportRow
import com.example.data.model.ExpenditureEntity
import com.example.data.model.VehicleEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportRepository(
    private val vehicleEntryDao: VehicleEntryDao,
    private val expenditureDao: ExpenditureDao
) {
    val allVehicleEntries: Flow<List<VehicleEntryEntity>> = vehicleEntryDao.getAllEntries()
    val allExpenditures: Flow<List<ExpenditureEntity>> = expenditureDao.getAllExpenditures()

    /**
     * Combines vehicle entries and expenditures into a unified reactive stream of daily report rows.
     */
    val allReportRows: Flow<List<DailyReportRow>> = combine(
        vehicleEntryDao.getAllEntries(),
        expenditureDao.getAllExpenditures()
    ) { entries, expenditures ->
        val expByDate = expenditures.groupBy { it.date }
        val entryByDate = entries.associateBy { it.date }

        val allDates = (entries.map { it.date } + expenditures.map { it.date })
            .toSet()
            .sortedDescending()

        allDates.map { date ->
            val entry = entryByDate[date]
            val dayExps = expByDate[date] ?: emptyList()
            val totalExp = dayExps.sumOf { it.amount }

            val pCount = entry?.petrolCount ?: 0
            val pAmount = entry?.petrolAmount ?: 0.0
            val dCount = entry?.dieselCount ?: 0
            val dAmount = entry?.dieselAmount ?: 0.0
            val tCount = entry?.truckCount ?: 0
            val tAmount = entry?.truckAmount ?: 0.0

            val totalVehicles = pCount + dCount + tCount
            val totalCollection = pAmount + dAmount + tAmount
            val netCollection = totalCollection - totalExp

            DailyReportRow(
                date = date,
                petrolCount = pCount,
                petrolAmount = pAmount,
                dieselCount = dCount,
                dieselAmount = dAmount,
                truckCount = tCount,
                truckAmount = tAmount,
                totalVehicles = totalVehicles,
                totalCollection = totalCollection,
                expenditure = totalExp,
                netCollection = netCollection,
                vehicleEntryId = entry?.id,
                hasExpenditureDetails = dayExps.isNotEmpty()
            )
        }
    }

    /**
     * Aggregates all lifetime totals for the main dashboard.
     */
    val overallSummary: Flow<AggregateReportSummary> = combine(
        vehicleEntryDao.getAllEntries(),
        expenditureDao.getAllExpenditures()
    ) { entries, expenditures ->
        var pVehicles = 0
        var dVehicles = 0
        var tVehicles = 0
        var pCollection = 0.0
        var dCollection = 0.0
        var tCollection = 0.0

        for (e in entries) {
            pVehicles += e.petrolCount
            dVehicles += e.dieselCount
            tVehicles += e.truckCount
            pCollection += e.petrolAmount
            dCollection += e.dieselAmount
            tCollection += e.truckAmount
        }

        val totalVehicles = pVehicles + dVehicles + tVehicles
        val grandTotalCollection = pCollection + dCollection + tCollection
        val totalExp = expenditures.sumOf { it.amount }
        val netCollection = grandTotalCollection - totalExp

        val distinctDays = (entries.map { it.date } + expenditures.map { it.date }).toSet().size

        AggregateReportSummary(
            totalPetrolVehicles = pVehicles,
            totalDieselVehicles = dVehicles,
            totalTruckVehicles = tVehicles,
            totalVehicles = totalVehicles,
            totalPetrolCollection = pCollection,
            totalDieselCollection = dCollection,
            totalTruckCollection = tCollection,
            grandTotalCollection = grandTotalCollection,
            totalOthersExpenditure = totalExp,
            netCollection = netCollection,
            daysCount = distinctDays
        )
    }

    fun getEntryByDate(date: String): Flow<VehicleEntryEntity?> =
        vehicleEntryDao.getEntryByDate(date)

    suspend fun getEntryByDateSync(date: String): VehicleEntryEntity? = withContext(Dispatchers.IO) {
        vehicleEntryDao.getEntryByDateSync(date)
    }

    fun getExpendituresByDate(date: String): Flow<List<ExpenditureEntity>> =
        expenditureDao.getExpendituresByDate(date)

    suspend fun getExpendituresByDateSync(date: String): List<ExpenditureEntity> = withContext(Dispatchers.IO) {
        expenditureDao.getExpendituresByDateSync(date)
    }

    suspend fun saveVehicleEntry(
        date: String,
        petrolCount: Int,
        petrolAmount: Double,
        dieselCount: Int,
        dieselAmount: Double,
        truckCount: Int,
        truckRate: Double = BusinessInfo.DEFAULT_TRUCK_RATE,
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val existing = vehicleEntryDao.getEntryByDateSync(date)
        val calculatedTruckAmount = truckCount * truckRate
        val entry = VehicleEntryEntity(
            id = existing?.id ?: 0,
            date = date,
            petrolCount = petrolCount,
            petrolAmount = petrolAmount,
            dieselCount = dieselCount,
            dieselAmount = dieselAmount,
            truckCount = truckCount,
            truckRate = truckRate,
            truckAmount = calculatedTruckAmount,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
        vehicleEntryDao.insertOrUpdate(entry)
    }

    suspend fun deleteVehicleEntryById(id: Long) = withContext(Dispatchers.IO) {
        vehicleEntryDao.deleteById(id)
    }

    suspend fun deleteVehicleEntryByDate(date: String) = withContext(Dispatchers.IO) {
        vehicleEntryDao.deleteByDate(date)
    }

    suspend fun saveExpenditure(
        id: Long = 0,
        date: String,
        description: String,
        amount: Double,
        category: String = "General"
    ): Long = withContext(Dispatchers.IO) {
        val entity = ExpenditureEntity(
            id = id,
            date = date,
            description = description.trim(),
            amount = amount,
            category = category
        )
        expenditureDao.insert(entity)
    }

    suspend fun deleteExpenditureById(id: Long) = withContext(Dispatchers.IO) {
        expenditureDao.deleteById(id)
    }

    /**
     * Seeds initial demo records so first launch demonstrates the complete flow immediately.
     */
    suspend fun seedDemoDataIfEmpty() = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        val cal = Calendar.getInstance()

        // Check if entries exist
        val existingToday = vehicleEntryDao.getEntryByDateSync(today)
        if (existingToday == null) {
            // Seed today
            saveVehicleEntry(
                date = today,
                petrolCount = 14,
                petrolAmount = 1400.0,
                dieselCount = 8,
                dieselAmount = 960.0,
                truckCount = 5,
                truckRate = 180.0,
                notes = "Morning rush heavy 2-wheelers"
            )

            saveExpenditure(
                date = today,
                description = "PUC Certificate Hologram & Paper Roll",
                amount = 350.0,
                category = "Stationery / Paper"
            )
            saveExpenditure(
                date = today,
                description = "Smoke Meter Sensor Cleaning & Filter",
                amount = 200.0,
                category = "Machine Maintenance"
            )

            // Seed yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            saveVehicleEntry(
                date = yesterday,
                petrolCount = 20,
                petrolAmount = 2000.0,
                dieselCount = 12,
                dieselAmount = 1440.0,
                truckCount = 7,
                truckRate = 180.0,
                notes = "Commercial trucks inspection day"
            )
            saveExpenditure(
                date = yesterday,
                description = "Electricity bill / generator diesel",
                amount = 420.0,
                category = "Electricity / Fuel"
            )

            // Seed 2 days ago
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val dayBefore = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            saveVehicleEntry(
                date = dayBefore,
                petrolCount = 16,
                petrolAmount = 1600.0,
                dieselCount = 10,
                dieselAmount = 1200.0,
                truckCount = 4,
                truckRate = 180.0,
                notes = "Routine testing"
            )
        }
    }
}
