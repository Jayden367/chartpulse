package com.example

import com.example.data.ai.PlaceholderAiAnalystRepository
import com.example.data.local.TradeJournalRepository
import com.example.data.local.TradeSetupDao
import com.example.data.local.TradeSetupEntity
import com.example.domain.ContractSpec
import com.example.domain.PositionCalculator
import com.example.domain.TradeDirection
import com.example.ui.AppRoute
import com.example.ui.JournalFilter
import com.example.ui.PlannerUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeTradeSetupDao : TradeSetupDao {
    private val items = MutableStateFlow<List<TradeSetupEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAllSetups(): Flow<List<TradeSetupEntity>> =
        items.map { list -> list.sortedByDescending { it.createdAtMillis } }

    override suspend fun insertSetup(setup: TradeSetupEntity): Long {
        val id = if (setup.id == 0L) nextId++ else setup.id
        val saved = setup.copy(id = id)
        items.value = items.value.filterNot { it.id == id } + saved
        return id
    }

    override suspend fun updateStatus(id: Long, status: String) {
        items.value = items.value.map { if (it.id == id) it.copy(status = status) else it }
    }

    override suspend fun updateNotes(id: Long, notes: String) {
        items.value = items.value.map { if (it.id == id) it.copy(notes = notes) else it }
    }

    override suspend fun deleteById(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun countSetups(): Int = items.value.size
}

class ExampleUnitTest {
    @Test
    fun calculateXauUsdLongPositionMetrics() {
        val metrics = PositionCalculator.calculate(
            direction = TradeDirection.LONG,
            accountBalance = 10000.0,
            riskPercent = 1.0,
            entryPrice = 3635.0,
            stopLoss = 3625.0,
            takeProfit = 3660.0,
            contractSpec = ContractSpec.GOLD_XAUUSD
        )
        assertTrue(metrics.isValid)
        assertEquals(10.0, metrics.stopDistance, 0.001)
        assertEquals(25.0, metrics.targetDistance, 0.001)
        assertEquals(2.5, metrics.riskRewardRatio, 0.001)
        assertEquals(100.0, metrics.riskAmountUsd, 0.01)
        assertEquals(250.0, metrics.rewardAmountUsd, 0.01)
        assertEquals(0.10, metrics.recommendedLots, 0.001)
    }

    @Test
    fun calculateXauUsdShortPositionMetrics() {
        val metrics = PositionCalculator.calculate(
            direction = TradeDirection.SHORT,
            accountBalance = 10000.0,
            riskPercent = 1.0,
            entryPrice = 3629.5,
            stopLoss = 3637.5,
            takeProfit = 3614.0,
            contractSpec = ContractSpec.GOLD_XAUUSD
        )
        assertTrue(metrics.isValid)
        assertEquals(8.0, metrics.stopDistance, 0.001)
        assertEquals(15.5, metrics.targetDistance, 0.001)
        assertEquals(1.9375, metrics.riskRewardRatio, 0.001)
        assertEquals(0.125, metrics.recommendedLots, 0.001)
    }

    @Test
    fun rejectInvalidLongStopLossAboveEntry() {
        val metrics = PositionCalculator.calculate(
            direction = TradeDirection.LONG,
            accountBalance = 10000.0,
            riskPercent = 1.0,
            entryPrice = 3635.0,
            stopLoss = 3640.0,
            takeProfit = 3660.0,
            contractSpec = ContractSpec.GOLD_XAUUSD
        )
        assertFalse(metrics.isValid)
    }

    @Test
    fun calculateEurUsdPositionMetrics() {
        val metrics = PositionCalculator.calculate(
            direction = TradeDirection.LONG,
            accountBalance = 10000.0,
            riskPercent = 1.0,
            entryPrice = 1.0830,
            stopLoss = 1.0798,
            takeProfit = 1.0894,
            contractSpec = ContractSpec.FX_STANDARD
        )
        assertTrue(metrics.isValid)
        assertEquals(0.0032, metrics.stopDistance, 0.00001)
        assertEquals(2.0, metrics.riskRewardRatio, 0.001)
        assertEquals(0.3125, metrics.recommendedLots, 0.001)
    }

    @Test
    fun calculateTakeProfitForRiskRewardTargets() {
        val longTp = PositionCalculator.calculateTakeProfitForRR(
            direction = TradeDirection.LONG,
            entryPrice = 3635.0,
            stopLoss = 3627.5,
            targetRR = 2.0
        )
        assertEquals(3650.0, longTp ?: 0.0, 0.001)

        val shortTp = PositionCalculator.calculateTakeProfitForRR(
            direction = TradeDirection.SHORT,
            entryPrice = 1.2708,
            stopLoss = 1.2748,
            targetRR = 2.5
        )
        assertEquals(1.2608, shortTp ?: 0.0, 0.0001)
    }

    @Test
    fun allThreeAppRoutesExistWithExpectedLabels() {
        val routes = AppRoute.entries
        assertEquals(listOf(AppRoute.ANALYSIS, AppRoute.PLANNER, AppRoute.JOURNAL), routes)
        assertEquals("Analysis", AppRoute.ANALYSIS.label)
        assertEquals("Planner", AppRoute.PLANNER.label)
        assertEquals("Journal", AppRoute.JOURNAL.label)
    }

    @Test
    fun plannerUiStateComputesLotSizeFromBalanceAndRiskPercent() {
        val state = PlannerUiState(
            pair = "XAUUSD",
            timeframe = "M15",
            direction = TradeDirection.LONG,
            balanceInput = "20000",
            riskPercentInput = "2.0",
            entryInput = "3635.00",
            stopLossInput = "3625.00",
            takeProfitInput = "3665.00",
            contractSpec = ContractSpec.GOLD_XAUUSD
        )
        assertTrue(state.metrics.isValid)
        assertEquals(400.0, state.metrics.riskAmountUsd, 0.01)
        assertEquals(1200.0, state.metrics.rewardAmountUsd, 0.01)
        assertEquals(0.40, state.metrics.recommendedLots, 0.001)
        assertEquals(3.0, state.metrics.riskRewardRatio, 0.001)
    }

    @Test
    fun plannerUiStateComputesRiskFromCustomLotSize() {
        val state = PlannerUiState(
            pair = "XAUUSD",
            timeframe = "M15",
            direction = TradeDirection.LONG,
            balanceInput = "10000",
            lotSizeInput = "0.25",
            isCustomLotMode = true,
            entryInput = "3635.00",
            stopLossInput = "3625.00",
            takeProfitInput = "3655.00",
            contractSpec = ContractSpec.GOLD_XAUUSD
        )
        assertTrue(state.metrics.isValid)
        assertEquals(0.25, state.metrics.recommendedLots, 0.001)
        assertEquals(250.0, state.metrics.riskAmountUsd, 0.01)
        assertEquals(2.5, state.metrics.impliedRiskPercent, 0.01)
        assertEquals(500.0, state.metrics.rewardAmountUsd, 0.01)
    }

    @Test
    fun journalRepositoryRecordsSavesViewsAndUpdatesTrades() = runTest {
        val repo = TradeJournalRepository(FakeTradeSetupDao())
        repo.seedDefaultsIfEmpty()

        val initial = repo.observeSetups().first()
        assertEquals(2, initial.size)

        val insertedId = repo.saveSetup(
            TradeSetupEntity(
                pair = "GBPUSD",
                timeframe = "H4",
                direction = "SHORT",
                entryPrice = 1.2708,
                stopLoss = 1.2748,
                takeProfit = 1.2610,
                accountBalance = 10000.0,
                riskPercent = 1.0,
                lotSize = 0.25,
                riskRewardRatio = 2.45,
                profitLossUsd = 245.0,
                status = "WON",
                notes = "London session supply rejection",
                marketCondition = "Manual Journal Entry"
            )
        )

        val afterInsert = repo.observeSetups().first()
        assertEquals(3, afterInsert.size)
        val saved = afterInsert.first { it.id == insertedId }
        assertEquals("GBPUSD", saved.pair)
        assertEquals("WON", saved.status)
        assertEquals(0.25, saved.lotSize, 0.001)
        assertEquals(245.0, saved.profitLossUsd ?: 0.0, 0.01)

        repo.updateStatus(insertedId, "LOST")
        repo.updateNotes(insertedId, "Stopped out on news")
        val updated = repo.observeSetups().first().first { it.id == insertedId }
        assertEquals("LOST", updated.status)
        assertEquals("Stopped out on news", updated.notes)

        val closedOnly = repo.observeSetups().first().filter {
            it.status == "WON" || it.status == "LOST"
        }
        assertEquals(listOf(JournalFilter.ALL, JournalFilter.PLANNED, JournalFilter.ACTIVE, JournalFilter.CLOSED), JournalFilter.entries)
        assertEquals(1, closedOnly.size)

        repo.deleteSetup(insertedId)
        assertEquals(2, repo.observeSetups().first().size)
    }

    @Test
    fun analystRepositoryProvidesXauUsdAndForexPresetsAndFollowUp() = runTest {
        val repo = PlaceholderAiAnalystRepository()
        val presets = repo.getPresetAnalyses()
        val pairs = presets.map { it.pair }
        assertTrue(pairs.contains("XAUUSD"))
        assertTrue(pairs.contains("EURUSD"))
        assertTrue(pairs.contains("GBPUSD"))
        assertTrue(pairs.contains("USDJPY"))

        val xau = presets.first { it.pair == "XAUUSD" }
        val reply = repo.askFollowUp(xau, "What are the support and resistance levels?", emptyList())
        assertTrue(reply.isSuccess)
        assertTrue(reply.getOrThrow().contains("3631"))
    }
}
