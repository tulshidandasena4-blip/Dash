package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpenditureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenditureDao {
    @Query("SELECT * FROM expenditures ORDER BY date DESC, createdAt DESC")
    fun getAllExpenditures(): Flow<List<ExpenditureEntity>>

    @Query("SELECT * FROM expenditures WHERE date = :date ORDER BY createdAt DESC")
    fun getExpendituresByDate(date: String): Flow<List<ExpenditureEntity>>

    @Query("SELECT * FROM expenditures WHERE date = :date ORDER BY createdAt DESC")
    suspend fun getExpendituresByDateSync(date: String): List<ExpenditureEntity>

    @Query("SELECT * FROM expenditures WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getExpendituresBetweenDates(startDate: String, endDate: String): Flow<List<ExpenditureEntity>>

    @Query("SELECT * FROM expenditures WHERE date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getExpendituresForMonth(monthPrefix: String): Flow<List<ExpenditureEntity>>

    @Query("SELECT * FROM expenditures WHERE date LIKE :yearPrefix || '%' ORDER BY date ASC")
    fun getExpendituresForYear(yearPrefix: String): Flow<List<ExpenditureEntity>>

    @Query("SELECT * FROM expenditures WHERE description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR date LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchExpenditures(query: String): Flow<List<ExpenditureEntity>>

    @Query("SELECT * FROM expenditures WHERE id = :id LIMIT 1")
    suspend fun getExpenditureById(id: Long): ExpenditureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expenditure: ExpenditureEntity): Long

    @Update
    suspend fun update(expenditure: ExpenditureEntity)

    @Delete
    suspend fun delete(expenditure: ExpenditureEntity)

    @Query("DELETE FROM expenditures WHERE id = :id")
    suspend fun deleteById(id: Long)
}
