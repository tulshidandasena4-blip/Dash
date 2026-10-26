package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenditures",
    indices = [Index(value = ["date"])]
)
data class ExpenditureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val description: String,
    val amount: Double,
    val category: String = "General", // "Stationery / Paper", "Machine Maintenance", "Electricity / Fuel", "Refreshment", "General"
    val createdAt: Long = System.currentTimeMillis()
)
