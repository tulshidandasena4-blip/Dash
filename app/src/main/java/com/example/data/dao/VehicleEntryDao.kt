package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VehicleEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleEntryDao {
    @Query("SELECT * FROM vehicle_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<VehicleEntryEntity>>

    @Query("SELECT * FROM vehicle_entries WHERE date = :date LIMIT 1")
    fun getEntryByDate(date: String): Flow<VehicleEntryEntity?>

    @Query("SELECT * FROM vehicle_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryByDateSync(date: String): VehicleEntryEntity?

    @Query("SELECT * FROM vehicle_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): VehicleEntryEntity?

    @Query("SELECT * FROM vehicle_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<VehicleEntryEntity>>

    @Query("SELECT * FROM vehicle_entries WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getEntriesForMonth(monthPrefix: String): Flow<List<VehicleEntryEntity>> // e.g. "2026-10"

    @Query("SELECT * FROM vehicle_entries WHERE date LIKE :yearPrefix || '%' ORDER BY date ASC")
    fun getEntriesForYear(yearPrefix: String): Flow<List<VehicleEntryEntity>> // e.g. "2026"

    @Query("SELECT * FROM vehicle_entries WHERE date LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchEntries(query: String): Flow<List<VehicleEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: VehicleEntryEntity): Long

    @Update
    suspend fun update(entry: VehicleEntryEntity)

    @Delete
    suspend fun delete(entry: VehicleEntryEntity)

    @Query("DELETE FROM vehicle_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vehicle_entries WHERE date = :date")
    suspend fun deleteByDate(date: String)
}
