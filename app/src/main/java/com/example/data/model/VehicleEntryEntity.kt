package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicle_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class VehicleEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val petrolCount: Int = 0,
    val petrolAmount: Double = 0.0,
    val dieselCount: Int = 0,
    val dieselAmount: Double = 0.0,
    val truckCount: Int = 0,
    val truckRate: Double = 180.0, // Fixed default ₹180 per vehicle
    val truckAmount: Double = 0.0, // truckCount * truckRate
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalVehicles: Int
        get() = petrolCount + dieselCount + truckCount

    val grandTotalCollection: Double
        get() = petrolAmount + dieselAmount + truckAmount
}
