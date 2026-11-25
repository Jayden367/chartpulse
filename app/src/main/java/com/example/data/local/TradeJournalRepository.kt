package com.example.data.local

import kotlinx.coroutines.flow.Flow

class TradeJournalRepository(private val dao: TradeSetupDao) {

    fun observeSetups(): Flow<List<TradeSetupEntity>> = dao.observeAllSetups()

    suspend fun seedDefaultsIfEmpty() {
        if (dao.countSetups() > 0) return
        val now = System.currentTimeMillis()
        dao.insertSetup(
            TradeSetupEntity(
                pair = "XAUUSD",
                timeframe = "M15",
                direction = "LONG",
                entryPrice = 3634.50,
                stopLoss = 3627.50,
                takeProfit = 3655.00,
                accountBalance = 10000.0,
                riskPercent = 1.0,
                lotSize = 0.14,
                riskRewardRatio = 2.93,
                status = "PLANNED",
                notes = "Wait for market reopen spread normalization + M15 bullish engulfing hold above 3631–3635 flip zone.",
                marketCondition = "Rollover Caution · Support Flip",
                createdAtMillis = now
            )
        )
        dao.insertSetup(
            TradeSetupEntity(
                pair = "XAUUSD",
                timeframe = "M15",
                direction = "SHORT",
                entryPrice = 3629.50,
                stopLoss = 3637.50,
                takeProfit = 3614.00,
                accountBalance = 10000.0,
                riskPercent = 1.0,
                lotSize = 0.12,
                riskRewardRatio = 1.94,
                status = "PLANNED",
                notes = "Bearish continuation trigger on decisive M15 close below 3630 targeting lower breakout imbalance 3613–3620.",
                marketCondition = "Breakdown Contingency",
                createdAtMillis = now - 3_600_000L
            )
        )
    }

    suspend fun saveSetup(setup: TradeSetupEntity): Long = dao.insertSetup(setup)

    suspend fun updateStatus(id: Long, status: String) = dao.updateStatus(id, status)

    suspend fun updateNotes(id: Long, notes: String) = dao.updateNotes(id, notes)

    suspend fun deleteSetup(id: Long) = dao.deleteById(id)
}
