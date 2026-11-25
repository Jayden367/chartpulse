package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_setups")
data class TradeSetupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pair: String,
    val timeframe: String,
    val direction: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val accountBalance: Double,
    val riskPercent: Double,
    val lotSize: Double,
    val riskRewardRatio: Double,
    val profitLossUsd: Double? = null,
    val status: String,
    val notes: String,
    val marketCondition: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)
