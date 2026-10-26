package com.example.data.model

/**
 * Represents a single row in the daily/monthly report table:
 * Date | Petrol Vehicles | Petrol Amount | Diesel Vehicles | Diesel Amount | Truck Vehicles | Truck Amount | Total Vehicles | Total Collection | Expenditure | Net Collection
 */
data class DailyReportRow(
    val date: String,
    val petrolCount: Int = 0,
    val petrolAmount: Double = 0.0,
    val dieselCount: Int = 0,
    val dieselAmount: Double = 0.0,
    val truckCount: Int = 0,
    val truckAmount: Double = 0.0,
    val totalVehicles: Int = 0,
    val totalCollection: Double = 0.0,
    val expenditure: Double = 0.0,
    val netCollection: Double = 0.0,
    val vehicleEntryId: Long? = null,
    val hasExpenditureDetails: Boolean = false
)

/**
 * Summary cards data for Dashboard and Reports:
 * - Total Vehicles
 * - Total Collection
 * - Total Others Expenditure
 * - Net Collection
 */
data class AggregateReportSummary(
    val totalPetrolVehicles: Int = 0,
    val totalDieselVehicles: Int = 0,
    val totalTruckVehicles: Int = 0,
    val totalVehicles: Int = 0,
    val totalPetrolCollection: Double = 0.0,
    val totalDieselCollection: Double = 0.0,
    val totalTruckCollection: Double = 0.0,
    val grandTotalCollection: Double = 0.0,
    val totalOthersExpenditure: Double = 0.0,
    val netCollection: Double = 0.0,
    val daysCount: Int = 0
)

/**
 * Business information constant
 */
object BusinessInfo {
    const val NAME = "Dash Pollution Testing Units"
    const val TAGLINE = "Authorized Vehicle Pollution Under Control (PUC) Testing Station"
    const val ADDRESS = "Palas, Junagarh, Kalahandi, Odisha"
    const val MOBILE = "9938740419"
    const val DEFAULT_TRUCK_RATE = 180.0
}
