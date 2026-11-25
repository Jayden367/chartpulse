package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiAnalystRepository
import com.example.data.ai.PlaceholderAiAnalystRepository
import com.example.data.local.ChartPulseDatabase
import com.example.data.local.TradeJournalRepository
import com.example.data.local.TradeSetupEntity
import com.example.data.model.AnalystMessage
import com.example.data.model.ChartAnalysis
import com.example.data.model.PriceSnapTarget
import com.example.data.model.TradeBias
import com.example.data.model.TradeScenario
import com.example.domain.ContractSpec
import com.example.domain.PositionCalculator
import com.example.domain.PositionMetrics
import com.example.domain.TradeDirection
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AnalysisUiState {
    data object Loading : AnalysisUiState
    data class Ready(val analysis: ChartAnalysis) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

enum class AppRoute(val label: String) {
    ANALYSIS("Analysis"),
    PLANNER("Planner"),
    JOURNAL("Journal")
}

enum class JournalFilter(val label: String) {
    ALL("All"),
    PLANNED("Planned"),
    ACTIVE("Active"),
    CLOSED("Closed")
}

data class PlannerUiState(
    val pair: String = "XAUUSD",
    val timeframe: String = "M15",
    val direction: TradeDirection = TradeDirection.LONG,
    val balanceInput: String = "10000",
    val riskPercentInput: String = "1.0",
    val lotSizeInput: String = "0.13",
    val isCustomLotMode: Boolean = false,
    val entryInput: String = "3635.00",
    val stopLossInput: String = "3627.50",
    val takeProfitInput: String = "3658.00",
    val contractSpec: ContractSpec = ContractSpec.GOLD_XAUUSD,
    val spreadNormalized: Boolean = false,
    val candleConfirmed: Boolean = false,
    val notesInput: String = "Wait for market reopen spreads to normalize, then watch for M15 bullish hold above 3631–3635.",
    val marketCondition: String = "Market Closed · Rollover",
    val calculationStatusMessage: String? = null,
    val savedSnackbarMessage: String? = null
) {
    val entryValue: Double get() = entryInput.toDoubleOrNull() ?: 0.0
    val stopLossValue: Double get() = stopLossInput.toDoubleOrNull() ?: 0.0
    val takeProfitValue: Double get() = takeProfitInput.toDoubleOrNull() ?: 0.0
    val balanceValue: Double get() = balanceInput.toDoubleOrNull() ?: 0.0
    val riskPercentValue: Double get() = riskPercentInput.toDoubleOrNull() ?: 0.0
    val lotSizeValue: Double get() = lotSizeInput.toDoubleOrNull() ?: 0.0

    val metrics: PositionMetrics
        get() = if (isCustomLotMode) {
            PositionCalculator.calculateFromLotSize(
                direction = direction,
                accountBalance = balanceValue,
                lotSize = lotSizeValue,
                entryPrice = entryValue,
                stopLoss = stopLossValue,
                takeProfit = takeProfitValue,
                contractSpec = contractSpec
            )
        } else {
            PositionCalculator.calculate(
                direction = direction,
                accountBalance = balanceValue,
                riskPercent = riskPercentValue,
                entryPrice = entryValue,
                stopLoss = stopLossValue,
                takeProfit = takeProfitValue,
                contractSpec = contractSpec
            )
        }
}

class ChartPulseViewModel(
    application: Application,
    private val aiRepository: AiAnalystRepository = PlaceholderAiAnalystRepository(),
    private val journalRepository: TradeJournalRepository = TradeJournalRepository(
        ChartPulseDatabase.getInstance(application).tradeSetupDao()
    )
) : AndroidViewModel(application) {

    val presets: List<ChartAnalysis> = aiRepository.getPresetAnalyses()

    private val _currentAnalysis = MutableStateFlow(presets.first())
    val currentAnalysis: StateFlow<ChartAnalysis> = _currentAnalysis.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Ready(presets.first()))
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(
            AnalystMessage(
                id = "init_1",
                isUser = false,
                text = "[Sample Preview] Loaded XAUUSD M15 (3634.69 · Market Closed). Price is resting on the 3631–3635 resistance-turned-support flip after a 70–75% retracement from 3689. Tap a scenario below or ask a follow-up question.",
                isSampleNotice = true
            )
        )
    )
    val messages: StateFlow<List<AnalystMessage>> = _messages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _activeTab = MutableStateFlow(AppRoute.ANALYSIS)
    val activeTab: StateFlow<AppRoute> = _activeTab.asStateFlow()

    private val _plannerState = MutableStateFlow(syncComputedFields(PlannerUiState()))
    val plannerState: StateFlow<PlannerUiState> = _plannerState.asStateFlow()

    private val _journalFilter = MutableStateFlow(JournalFilter.ALL)
    val journalFilter: StateFlow<JournalFilter> = _journalFilter.asStateFlow()

    fun selectTab(tab: AppRoute) {
        _activeTab.value = tab
    }

    val journalSetups: StateFlow<List<TradeSetupEntity>> = combine(
        journalRepository.observeSetups(),
        _journalFilter
    ) { all, filter ->
        when (filter) {
            JournalFilter.ALL -> all
            JournalFilter.PLANNED -> all.filter { it.status == "PLANNED" }
            JournalFilter.ACTIVE -> all.filter { it.status == "ACTIVE" }
            JournalFilter.CLOSED -> all.filter { it.status == "WON" || it.status == "LOST" }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    val allSetupsCount: StateFlow<List<TradeSetupEntity>> = journalRepository.observeSetups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            journalRepository.seedDefaultsIfEmpty()
        }
    }

    fun selectPreset(presetId: String) {
        val chosen = presets.firstOrNull { it.id == presetId } ?: return
        _currentAnalysis.value = chosen
        _analysisState.value = AnalysisUiState.Ready(chosen)
        applyScenarioToPlanner(chosen.bullishScenario, chosen)
        _messages.value = listOf(
            AnalystMessage(
                id = "preset_${chosen.id}",
                isUser = false,
                text = "[Sample Preview] Switched workspace to ${chosen.pair} ${chosen.timeframe} at ${formatNum(chosen.currentPrice, chosen.priceDecimals)}. ${chosen.marketStatusNote}",
                isSampleNotice = true
            )
        )
    }

    fun analyzeChartScreenshot(
        imageUri: Uri?,
        pairHint: String,
        timeframeHint: String,
        contextNote: String
    ) {
        viewModelScope.launch {
            _analysisState.value = AnalysisUiState.Loading
            try {
                val result = aiRepository.analyzeChart(
                    imageUri = imageUri?.toString(),
                    pairHint = pairHint,
                    timeframeHint = timeframeHint,
                    promptContext = contextNote
                )
                result.onSuccess { analysis ->
                    _currentAnalysis.value = analysis
                    _analysisState.value = AnalysisUiState.Ready(analysis)
                    applyScenarioToPlanner(analysis.bullishScenario, analysis)
                    _messages.update { current ->
                        current + AnalystMessage(
                            id = "scan_${System.currentTimeMillis()}",
                            isUser = false,
                            text = "[Sample Preview] Analyzed ${analysis.pair} (${analysis.timeframe}) chart preview. Watch ${analysis.keyLevels.firstOrNull()?.title ?: "key zones"}.",
                            isSampleNotice = true
                        )
                    }
                }.onFailure { err ->
                    _analysisState.value = AnalysisUiState.Error(err.localizedMessage ?: "Failed to analyze chart.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _analysisState.value = AnalysisUiState.Error(e.localizedMessage ?: "Analysis failed.")
            }
        }
    }

    fun askFollowUp(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank() || _isChatLoading.value) return
        val userMsg = AnalystMessage(
            id = "u_${System.currentTimeMillis()}",
            isUser = true,
            text = trimmed
        )
        _messages.update { it + userMsg }
        viewModelScope.launch {
            _isChatLoading.value = true
            try {
                val replyResult = aiRepository.askFollowUp(
                    analysis = _currentAnalysis.value,
                    question = trimmed,
                    history = _messages.value
                )
                replyResult.onSuccess { reply ->
                    _messages.update {
                        it + AnalystMessage(
                            id = "a_${System.currentTimeMillis()}",
                            isUser = false,
                            text = reply,
                            isSampleNotice = true
                        )
                    }
                }.onFailure { err ->
                    _messages.update {
                        it + AnalystMessage(
                            id = "e_${System.currentTimeMillis()}",
                            isUser = false,
                            text = "Unable to answer right now: ${err.localizedMessage ?: "Unknown error"}"
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun applyScenarioToPlanner(
        scenario: TradeScenario,
        analysis: ChartAnalysis = _currentAnalysis.value
    ) {
        val decimals = analysis.priceDecimals
        val dir = if (scenario.bias == TradeBias.BEARISH) TradeDirection.SHORT else TradeDirection.LONG
        val spec = PositionCalculator.inferContractSpec(analysis.pair)
        _plannerState.update { state ->
            syncComputedFields(
                state.copy(
                    pair = analysis.pair,
                    timeframe = analysis.timeframe,
                    direction = dir,
                    isCustomLotMode = false,
                    entryInput = formatNum(scenario.entryPrice, decimals),
                    stopLossInput = formatNum(scenario.stopLossPrice, decimals),
                    takeProfitInput = formatNum(scenario.takeProfitPrice, decimals),
                    contractSpec = spec,
                    notesInput = "${scenario.title}: ${scenario.triggerCondition}",
                    marketCondition = analysis.marketStatus.label,
                    calculationStatusMessage = null
                )
            )
        }
    }

    fun snapPriceToPlanner(price: Double, target: PriceSnapTarget) {
        val analysis = _currentAnalysis.value
        val decimals = analysis.priceDecimals
        val formatted = formatNum(price, decimals)
        val inferredSpec = PositionCalculator.inferContractSpec(analysis.pair)
        _plannerState.update { state ->
            val synced = if (state.pair != analysis.pair) {
                state.copy(
                    pair = analysis.pair,
                    timeframe = analysis.timeframe,
                    contractSpec = inferredSpec,
                    marketCondition = analysis.marketStatus.label
                )
            } else {
                state
            }
            val updated = when (target) {
                PriceSnapTarget.ENTRY -> synced.copy(entryInput = formatted)
                PriceSnapTarget.STOP_LOSS -> synced.copy(stopLossInput = formatted)
                PriceSnapTarget.TAKE_PROFIT -> synced.copy(takeProfitInput = formatted)
            }
            syncComputedFields(updated.copy(calculationStatusMessage = null))
        }
        _activeTab.value = AppRoute.PLANNER
    }

    fun updatePairInput(value: String) {
        val cleaned = value.uppercase().take(12)
        val spec = PositionCalculator.inferContractSpec(cleaned)
        _plannerState.update {
            syncComputedFields(
                it.copy(
                    pair = cleaned,
                    contractSpec = spec,
                    calculationStatusMessage = null
                )
            )
        }
    }

    fun updateTimeframeInput(value: String) {
        _plannerState.update { it.copy(timeframe = value.uppercase().take(6)) }
    }

    fun selectPlannerPairPreset(presetId: String) {
        val chosen = presets.firstOrNull { it.id == presetId } ?: return
        applyScenarioToPlanner(chosen.bullishScenario, chosen)
    }

    fun applyRiskRewardTarget(targetRR: Double) {
        val state = _plannerState.value
        val tp = PositionCalculator.calculateTakeProfitForRR(
            direction = state.direction,
            entryPrice = state.entryValue,
            stopLoss = state.stopLossValue,
            targetRR = targetRR
        )
        if (tp == null) {
            _plannerState.update {
                it.copy(calculationStatusMessage = "Set valid Entry Price and Stop Loss first to compute 1:${formatNum(targetRR, 1)} Take Profit.")
            }
            return
        }
        val formatted = formatNum(tp, state.contractSpec.priceDecimals)
        _plannerState.update {
            syncComputedFields(
                it.copy(
                    takeProfitInput = formatted,
                    calculationStatusMessage = null
                )
            )
        }
    }

    fun updateDirection(direction: TradeDirection) {
        _plannerState.update {
            syncComputedFields(it.copy(direction = direction, calculationStatusMessage = null))
        }
    }

    fun updateContractSpec(spec: ContractSpec) {
        _plannerState.update {
            syncComputedFields(it.copy(contractSpec = spec, calculationStatusMessage = null))
        }
    }

    fun updateBalanceInput(value: String) {
        _plannerState.update {
            syncComputedFields(it.copy(balanceInput = filterNumeric(value), calculationStatusMessage = null))
        }
    }

    fun updateRiskPercentInput(value: String) {
        _plannerState.update {
            syncComputedFields(
                it.copy(
                    riskPercentInput = filterNumeric(value),
                    isCustomLotMode = false,
                    calculationStatusMessage = null
                )
            )
        }
    }

    fun updateLotSizeInput(value: String) {
        _plannerState.update {
            syncComputedFields(
                it.copy(
                    lotSizeInput = filterNumeric(value),
                    isCustomLotMode = true,
                    calculationStatusMessage = null
                )
            )
        }
    }

    fun updateEntryInput(value: String) {
        _plannerState.update {
            syncComputedFields(it.copy(entryInput = filterNumeric(value), calculationStatusMessage = null))
        }
    }

    fun updateStopLossInput(value: String) {
        _plannerState.update {
            syncComputedFields(it.copy(stopLossInput = filterNumeric(value), calculationStatusMessage = null))
        }
    }

    fun updateTakeProfitInput(value: String) {
        _plannerState.update {
            syncComputedFields(it.copy(takeProfitInput = filterNumeric(value), calculationStatusMessage = null))
        }
    }

    fun calculatePlannerPosition() {
        _plannerState.update { current ->
            val synced = syncComputedFields(current)
            val m = synced.metrics
            if (m.isValid) {
                val summary = "${formatNum(m.recommendedLots, 2)} lots · Risk $${formatNum(m.riskAmountUsd, 2)} (${formatNum(m.impliedRiskPercent, 2)}%) · Profit +$${formatNum(m.rewardAmountUsd, 2)} · 1:${formatNum(m.riskRewardRatio, 2)} R:R"
                synced.copy(calculationStatusMessage = summary)
            } else {
                synced.copy(calculationStatusMessage = m.validationHint ?: "Check Entry, Stop Loss, Take Profit, and Account Balance.")
            }
        }
    }

    fun updateNotesInput(value: String) {
        _plannerState.update { it.copy(notesInput = value) }
    }

    fun toggleSpreadNormalized(checked: Boolean) {
        _plannerState.update { it.copy(spreadNormalized = checked) }
    }

    fun toggleCandleConfirmed(checked: Boolean) {
        _plannerState.update { it.copy(candleConfirmed = checked) }
    }

    fun clearSnackbar() {
        _plannerState.update { it.copy(savedSnackbarMessage = null) }
    }

    fun savePlannerSetupToJournal() {
        val state = syncComputedFields(_plannerState.value)
        val metrics = state.metrics
        if (!metrics.isValid) {
            _plannerState.update {
                it.copy(calculationStatusMessage = metrics.validationHint ?: "Invalid trade levels.")
            }
            return
        }
        val effectiveLots = if (state.isCustomLotMode && state.lotSizeValue > 0.0) {
            state.lotSizeValue
        } else {
            metrics.recommendedLots
        }
        viewModelScope.launch {
            journalRepository.saveSetup(
                TradeSetupEntity(
                    pair = state.pair.ifBlank { "XAUUSD" },
                    timeframe = state.timeframe.ifBlank { "M15" },
                    direction = state.direction.label,
                    entryPrice = state.entryValue,
                    stopLoss = state.stopLossValue,
                    takeProfit = state.takeProfitValue,
                    accountBalance = state.balanceValue,
                    riskPercent = metrics.impliedRiskPercent,
                    lotSize = effectiveLots,
                    riskRewardRatio = metrics.riskRewardRatio,
                    profitLossUsd = null,
                    status = "PLANNED",
                    notes = state.notesInput.trim(),
                    marketCondition = state.marketCondition
                )
            )
            _journalFilter.value = JournalFilter.ALL
            _plannerState.update {
                state.copy(savedSnackbarMessage = "Saved ${state.pair} setup to Journal")
            }
            _activeTab.value = AppRoute.JOURNAL
        }
    }

    fun setJournalFilter(filter: JournalFilter) {
        _journalFilter.value = filter
    }

    fun updateSetupStatus(id: Long, status: String) {
        viewModelScope.launch {
            journalRepository.updateStatus(id, status)
        }
    }

    fun recordJournalTrade(
        pair: String,
        timeframe: String,
        direction: TradeDirection,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double,
        lotSize: Double,
        profitLossUsd: Double?,
        status: String,
        notes: String
    ) {
        if (entryPrice <= 0.0 || stopLoss <= 0.0 || takeProfit <= 0.0 || lotSize <= 0.0) {
            _plannerState.update {
                it.copy(savedSnackbarMessage = "Enter positive values for Entry, Stop Loss, Take Profit, and Lot Size.")
            }
            return
        }
        val normalizedPair = pair.trim().uppercase().ifBlank { "XAUUSD" }
        val normalizedTf = timeframe.trim().uppercase().ifBlank { "M15" }
        val stopDist = kotlin.math.abs(entryPrice - stopLoss).coerceAtLeast(0.00001)
        val targetDist = kotlin.math.abs(takeProfit - entryPrice)
        val rr = targetDist / stopDist

        viewModelScope.launch {
            journalRepository.saveSetup(
                TradeSetupEntity(
                    pair = normalizedPair,
                    timeframe = normalizedTf,
                    direction = direction.label,
                    entryPrice = entryPrice,
                    stopLoss = stopLoss,
                    takeProfit = takeProfit,
                    accountBalance = _plannerState.value.balanceValue.takeIf { it > 0 } ?: 10000.0,
                    riskPercent = _plannerState.value.riskPercentValue.takeIf { it > 0 } ?: 1.0,
                    lotSize = lotSize,
                    riskRewardRatio = rr,
                    profitLossUsd = profitLossUsd,
                    status = status,
                    notes = notes.trim(),
                    marketCondition = "Manual Journal Entry"
                )
            )
            val currentFilter = _journalFilter.value
            val matchesFilter = when (currentFilter) {
                JournalFilter.ALL -> true
                JournalFilter.PLANNED -> status == "PLANNED"
                JournalFilter.ACTIVE -> status == "ACTIVE"
                JournalFilter.CLOSED -> status == "WON" || status == "LOST"
            }
            if (!matchesFilter) {
                _journalFilter.value = JournalFilter.ALL
            }
            _plannerState.update {
                it.copy(savedSnackbarMessage = "Saved $normalizedPair trade to Journal")
            }
        }
    }

    fun updateSetupNotes(id: Long, notes: String) {
        viewModelScope.launch {
            journalRepository.updateNotes(id, notes.trim())
            _plannerState.update { it.copy(savedSnackbarMessage = "Journal notes updated") }
        }
    }

    fun deleteSetup(id: Long) {
        viewModelScope.launch {
            journalRepository.deleteSetup(id)
            _plannerState.update { it.copy(savedSnackbarMessage = "Removed trade from Journal") }
        }
    }

    fun loadJournalSetupIntoPlanner(setup: TradeSetupEntity) {
        val spec = PositionCalculator.inferContractSpec(setup.pair)
        val dec = spec.priceDecimals
        _plannerState.update { state ->
            syncComputedFields(
                state.copy(
                    pair = setup.pair,
                    timeframe = setup.timeframe,
                    direction = if (setup.direction == "SHORT" || setup.direction == "SELL") TradeDirection.SHORT else TradeDirection.LONG,
                    balanceInput = formatNum(setup.accountBalance, 0),
                    riskPercentInput = formatNum(setup.riskPercent, 1),
                    lotSizeInput = formatNum(setup.lotSize, 2),
                    isCustomLotMode = false,
                    entryInput = formatNum(setup.entryPrice, dec),
                    stopLossInput = formatNum(setup.stopLoss, dec),
                    takeProfitInput = formatNum(setup.takeProfit, dec),
                    contractSpec = spec,
                    notesInput = setup.notes,
                    marketCondition = setup.marketCondition,
                    calculationStatusMessage = null
                )
            )
        }
    }

    private fun syncComputedFields(state: PlannerUiState): PlannerUiState {
        val m = state.metrics
        if (!m.isValid) return state
        return if (state.isCustomLotMode) {
            if (state.balanceValue > 0.0) {
                state.copy(riskPercentInput = formatNum(m.impliedRiskPercent, 2))
            } else {
                state
            }
        } else {
            state.copy(lotSizeInput = formatNum(m.recommendedLots, 2))
        }
    }

    private fun filterNumeric(raw: String): String {
        val normalized = raw.replace(',', '.')
        return normalized.filterIndexed { idx, c -> c.isDigit() || (c == '.' && normalized.indexOf('.') == idx) }
    }

    private fun formatNum(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value)

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChartPulseViewModel(application) as T
        }
    }
}
