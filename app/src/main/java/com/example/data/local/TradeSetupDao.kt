package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeSetupDao {
    @Query("SELECT * FROM trade_setups ORDER BY createdAtMillis DESC")
    fun observeAllSetups(): Flow<List<TradeSetupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetup(setup: TradeSetupEntity): Long

    @Query("UPDATE trade_setups SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE trade_setups SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("DELETE FROM trade_setups WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM trade_setups")
    suspend fun countSetups(): Int
}
