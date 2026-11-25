package com.example.data.ai

import com.example.data.model.AnalystMessage
import com.example.data.model.ChartAnalysis
import com.example.data.model.KeyLevelZone
import com.example.data.model.MarketStatus
import com.example.data.model.PriceActionPhase
import com.example.data.model.TradeBias
import com.example.data.model.TradeScenario
import com.example.data.model.ZoneType
import kotlinx.coroutines.delay

/**
 * Abstraction for chart screenshot analysis and trade desk Q&A.
 * When Firebase AI Logic is enabled, a Gemini-backed implementation can replace
 * PlaceholderAiAnalystRepository without altering ViewModel or UI code.
 */
interface AiAnalystRepository {
    fun getPresetAnalyses(): List<ChartAnalysis>

    suspend fun analyzeChart(
        imageUri: String?,
        pairHint: String,
        timeframeHint: String,
        promptContext: String
    ): Result<ChartAnalysis>

    suspend fun askFollowUp(
        analysis: ChartAnalysis,
        question: String,
        history: List<AnalystMessage>
    ): Result<String>
}

class PlaceholderAiAnalystRepository : AiAnalystRepository {

    override fun getPresetAnalyses(): List<ChartAnalysis> = listOf(
        xauUsdM15Analysis(),
        eurUsdH1Analysis(),
        gbpUsdH4Analysis(),
        usdJpyH1Analysis(),
        btcUsdM30Analysis()
    )

    override suspend fun analyzeChart(
        imageUri: String?,
        pairHint: String,
        timeframeHint: String,
        promptContext: String
    ): Result<ChartAnalysis> {
        delay(550L)
        val normalizedPair = pairHint.trim().uppercase().ifBlank { "XAUUSD" }
        val normalizedTf = timeframeHint.trim().uppercase().ifBlank { "M15" }
        val base = when {
            normalizedPair.contains("GBP") -> gbpUsdH4Analysis()
            normalizedPair.contains("JPY") -> usdJpyH1Analysis()
            normalizedPair.contains("EUR") || normalizedPair.contains("AUD") || normalizedPair.contains("NZD") || normalizedPair.contains("CAD") || normalizedPair.contains("CHF") -> eurUsdH1Analysis()
            normalizedPair.contains("BTC") || normalizedPair.contains("ETH") -> btcUsdM30Analysis()
            else -> xauUsdM15Analysis()
        }
        val contextNote = if (promptContext.isNotBlank()) {
            "${base.marketStatusNote} · Focus: ${promptContext.trim()}"
        } else {
            base.marketStatusNote
        }
        return Result.success(
            base.copy(
                id = "sample_${System.currentTimeMillis()}",
                pair = normalizedPair,
                timeframe = normalizedTf,
                marketStatusNote = contextNote,
                imageUri = imageUri,
                isSampleResponse = true,
                createdAtMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun askFollowUp(
        analysis: ChartAnalysis,
        question: String,
        history: List<AnalystMessage>
    ): Result<String> {
        delay(400L)
        val q = question.lowercase()
        val pair = analysis.pair
        val tf = analysis.timeframe
        val support = analysis.keyLevels.firstOrNull { it.type == ZoneType.SUPPORT }
        val resistance = analysis.keyLevels.firstOrNull { it.type == ZoneType.RESISTANCE }
        val imbalance = analysis.keyLevels.firstOrNull { it.type == ZoneType.IMBALANCE }

        val reply = when {
            q.contains("long") || q.contains("buy") || q.contains("entry") -> {
                val bull = analysis.bullishScenario
                "[Sample Preview] For a $pair ($tf) BUY signal: ${bull.triggerCondition} " +
                    "Entry: ${formatPrice(bull.entryPrice, analysis.priceDecimals)}, " +
                    "Stop Loss: ${formatPrice(bull.stopLossPrice, analysis.priceDecimals)}, " +
                    "Take Profit: ${formatPrice(bull.takeProfitPrice, analysis.priceDecimals)}. " +
                    "Invalidation: ${bull.invalidationNote}"
            }
            q.contains("short") || q.contains("sell") || q.contains("bear") -> {
                val bear = analysis.bearishScenario
                "[Sample Preview] For a $pair ($tf) SELL signal: ${bear.triggerCondition} " +
                    "Entry: ${formatPrice(bear.entryPrice, analysis.priceDecimals)}, " +
                    "Stop Loss: ${formatPrice(bear.stopLossPrice, analysis.priceDecimals)}, " +
                    "Take Profit: ${formatPrice(bear.takeProfitPrice, analysis.priceDecimals)}. " +
                    "Invalidation: ${bear.invalidationNote}"
            }
            q.contains("support") || q.contains("resistance") || q.contains("level") || q.contains("zone") -> {
                val supLabel = support?.let {
                    "${it.title} (${formatPrice(it.lowPrice, analysis.priceDecimals)}–${formatPrice(it.highPrice, analysis.priceDecimals)})"
                } ?: "key support"
                val resLabel = resistance?.let {
                    "${it.title} (${formatPrice(it.lowPrice, analysis.priceDecimals)}–${formatPrice(it.highPrice, analysis.priceDecimals)})"
                } ?: "overhead resistance"
                "[Sample Preview] $pair ($tf) key S/R map: Support sits at $supLabel while Resistance caps near $resLabel. Tap any Set Entry / SL / TP button below to snap levels directly to the Planner."
            }
            q.contains("stop") || q.contains("sl") || q.contains("risk") -> {
                val supLabel = support?.let {
                    "${formatPrice(it.lowPrice, analysis.priceDecimals)}–${formatPrice(it.highPrice, analysis.priceDecimals)}"
                } ?: "key support"
                val resLabel = resistance?.let {
                    "${formatPrice(it.lowPrice, analysis.priceDecimals)}–${formatPrice(it.highPrice, analysis.priceDecimals)}"
                } ?: "overhead resistance"
                "[Sample Preview] Keep stops outside the $supLabel reaction wick for longs or above $resLabel for shorts so normal spread noise doesn't tag you out. Aim for >= 1:1.5 R:R in the Planner tab."
            }
            q.contains("rollover") || q.contains("spread") || q.contains("close") || q.contains("open") -> {
                "[Sample Preview] ${analysis.marketStatusNote} Avoid market orders in the first 10–15 minutes of reopen; wait for bid/ask spreads to tighten back to normal before triggering."
            }
            else -> {
                val imbText = imbalance?.let {
                    " Lower imbalance sits at ${formatPrice(it.lowPrice, analysis.priceDecimals)}–${formatPrice(it.highPrice, analysis.priceDecimals)}."
                }.orEmpty()
                "[Sample Preview] $pair ($tf) bias is ${analysis.trendBias.label} at ${formatPrice(analysis.currentPrice, analysis.priceDecimals)}. " +
                    "Watch whether buyers defend ${support?.title ?: "support"} or sellers break below it.$imbText"
            }
        }
        return Result.success(reply)
    }

    private fun formatPrice(value: Double, decimals: Int): String =
        String.format("%.${decimals}f", value)

    companion object {
        fun xauUsdM15Analysis(): ChartAnalysis = ChartAnalysis(
            id = "preset_xauusd_m15",
            pair = "XAUUSD",
            timeframe = "M15",
            currentPrice = 3634.69,
            marketStatus = MarketStatus.CLOSED_ROLLOVER,
            marketStatusNote = "Market closed during daily rollover window — spreads can widen sharply at reopen. Wait for spreads to normalize.",
            trendBias = TradeBias.BULLISH,
            signalSummary = "BUY WATCH on 3631–3635 flip defense · SELL contingency on M15 close below 3630.",
            priceActionPhases = listOf(
                PriceActionPhase(
                    title = "Earlier Consolidation",
                    rangeLabel = "~3600 – 3637",
                    summary = "Sideways range with swing low near 3600 and resistance capping rallies around 3635–3637.",
                    bias = TradeBias.NEUTRAL
                ),
                PriceActionPhase(
                    title = "Sharp Bullish Impulse",
                    rangeLabel = "~3613 → 3689",
                    summary = "Mid-session vertical breakout cleared range highs and drove Gold up to a 3689 peak.",
                    bias = TradeBias.BULLISH
                ),
                PriceActionPhase(
                    title = "Steady Retracement",
                    rangeLabel = "3689 → 3631",
                    summary = "Lower highs and lower lows retraced ~70–75% of the breakout, resting right on old range highs.",
                    bias = TradeBias.BEARISH
                )
            ),
            keyLevels = listOf(
                KeyLevelZone(
                    id = "xau_peak",
                    title = "Session Impulse High",
                    lowPrice = 3686.0,
                    highPrice = 3689.0,
                    role = "Major Peak",
                    type = ZoneType.SESSION_EXTREME,
                    note = "Full breakout peak and major liquidity target if bullish momentum resumes."
                ),
                KeyLevelZone(
                    id = "xau_res_mid",
                    title = "Mid-Range Supply",
                    lowPrice = 3658.0,
                    highPrice = 3662.0,
                    role = "Upper Target",
                    type = ZoneType.RESISTANCE,
                    note = "Secondary upside magnet once 3647 clears cleanly."
                ),
                KeyLevelZone(
                    id = "xau_res_near",
                    title = "Overhead Resistance",
                    lowPrice = 3640.0,
                    highPrice = 3647.0,
                    role = "Structure Shift",
                    type = ZoneType.RESISTANCE,
                    note = "Buyers must reclaim 3640–3647 to break the M15 lower-high sequence."
                ),
                KeyLevelZone(
                    id = "xau_sup_flip",
                    title = "Resistance-Turned-Support",
                    lowPrice = 3631.0,
                    highPrice = 3635.0,
                    role = "Active Flip Zone",
                    type = ZoneType.SUPPORT,
                    note = "Price is resting here at 3634.69 with early lower wicks defending 3631–3633."
                ),
                KeyLevelZone(
                    id = "xau_imb",
                    title = "Breakout Imbalance Fill",
                    lowPrice = 3613.0,
                    highPrice = 3620.0,
                    role = "Lower Support",
                    type = ZoneType.IMBALANCE,
                    note = "Origin of the big green breakout candle; thin structure above this pocket."
                ),
                KeyLevelZone(
                    id = "xau_low",
                    title = "Session Range Low",
                    lowPrice = 3598.0,
                    highPrice = 3602.0,
                    role = "Range Floor",
                    type = ZoneType.SESSION_EXTREME,
                    note = "Earlier consolidation floor if full retracement completes."
                )
            ),
            bullishScenario = TradeScenario(
                id = "xau_bull",
                title = "Bullish Bounce Signal",
                bias = TradeBias.BULLISH,
                triggerCondition = "Hold 3631–3635 after reopen, wait for normal spreads, and confirm with a strong M15 bullish engulfing candle or higher low.",
                entryPrice = 3635.00,
                stopLossPrice = 3627.50,
                takeProfitPrice = 3658.00,
                invalidationNote = "Invalidated on a clean M15 candle close below 3630."
            ),
            bearishScenario = TradeScenario(
                id = "xau_bear",
                title = "Bearish Continuation Signal",
                bias = TradeBias.BEARISH,
                triggerCondition = "Clean M15 candle close below 3630 confirms sellers still control momentum from the 3689 sell-off.",
                entryPrice = 3629.50,
                stopLossPrice = 3637.50,
                takeProfitPrice = 3614.00,
                invalidationNote = "Invalidated if price reclaims 3640–3647 overhead resistance."
            ),
            chartPatternPoints = listOf(
                3618f, 3605f, 3612f, 3633f, 3601f, 3624f, 3636f, 3613f,
                3642f, 3674f, 3689f, 3671f, 3659f, 3647f, 3639f, 3632f, 3634.69f
            ),
            priceDecimals = 2,
            isSampleResponse = true
        )

        fun eurUsdH1Analysis(): ChartAnalysis = ChartAnalysis(
            id = "preset_eurusd_h1",
            pair = "EURUSD",
            timeframe = "H1",
            currentPrice = 1.0842,
            marketStatus = MarketStatus.OPEN,
            marketStatusNote = "London/NY overlap active — tight spreads and normal execution liquidity.",
            trendBias = TradeBias.BULLISH,
            signalSummary = "BUY bias above 1.0820 London flip · SELL signal only if 1.0820 support breaks.",
            priceActionPhases = listOf(
                PriceActionPhase(
                    title = "Asian Range Accumulation",
                    rangeLabel = "1.0795 – 1.0820",
                    summary = "Tight overnight compression below 1.0820 before London open sweep.",
                    bias = TradeBias.NEUTRAL
                ),
                PriceActionPhase(
                    title = "Liquidity Sweep & Reclaim",
                    rangeLabel = "1.0788 → 1.0856",
                    summary = "Quick stop-hunt below 1.0795 snapped back above 1.0825 on volume.",
                    bias = TradeBias.BULLISH
                )
            ),
            keyLevels = listOf(
                KeyLevelZone("eu_res", "Weekly Supply Cap", 1.0875, 1.0890, "Major Target", ZoneType.RESISTANCE, "Prior swing high supply."),
                KeyLevelZone("eu_sup", "London Breakout Flip", 1.0820, 1.0832, "Key Support", ZoneType.SUPPORT, "Retest zone for continuation longs."),
                KeyLevelZone("eu_low", "Sweep Low", 1.0785, 1.0795, "Invalidation", ZoneType.SESSION_EXTREME, "Session liquidity sweep bottom.")
            ),
            bullishScenario = TradeScenario(
                id = "eu_bull",
                title = "Bullish Continuation Signal",
                bias = TradeBias.BULLISH,
                triggerCondition = "Higher low on H1 pullback into 1.0825–1.0832 flip zone.",
                entryPrice = 1.0830,
                stopLossPrice = 1.0798,
                takeProfitPrice = 1.0885,
                invalidationNote = "H1 close back inside Asian range below 1.0810."
            ),
            bearishScenario = TradeScenario(
                id = "eu_bear",
                title = "Failed Breakout Short Signal",
                bias = TradeBias.BEARISH,
                triggerCondition = "Rejection wick at 1.0860 followed by close under 1.0820.",
                entryPrice = 1.0818,
                stopLossPrice = 1.0848,
                takeProfitPrice = 1.0788,
                invalidationNote = "Clean H1 acceptance above 1.0855."
            ),
            chartPatternPoints = listOf(
                1.0812f, 1.0802f, 1.0818f, 1.0796f, 1.0788f, 1.0824f,
                1.0848f, 1.0856f, 1.0838f, 1.0842f
            ),
            priceDecimals = 4,
            isSampleResponse = true
        )

        fun gbpUsdH4Analysis(): ChartAnalysis = ChartAnalysis(
            id = "preset_gbpusd_h4",
            pair = "GBPUSD",
            timeframe = "H4",
            currentPrice = 1.2685,
            marketStatus = MarketStatus.OPEN,
            marketStatusNote = "London session active — watch UK/US rate differential headlines around NY open.",
            trendBias = TradeBias.BEARISH,
            signalSummary = "SELL WATCH under 1.2715 supply rejection · BUY recovery only on H4 reclaim above 1.2730.",
            priceActionPhases = listOf(
                PriceActionPhase(
                    title = "Distribution Ceiling",
                    rangeLabel = "1.2720 – 1.2765",
                    summary = "Repeated upper wicks at 1.2750–1.2765 signaled institutional supply absorption.",
                    bias = TradeBias.BEARISH
                ),
                PriceActionPhase(
                    title = "Channel Breakdown",
                    rangeLabel = "1.2740 → 1.2672",
                    summary = "H4 bearish engulfing broke minor ascending trendline, retesting 1.2700 from below.",
                    bias = TradeBias.BEARISH
                )
            ),
            keyLevels = listOf(
                KeyLevelZone("gu_res_high", "Weekly Supply High", 1.2745, 1.2765, "Invalidation", ZoneType.SESSION_EXTREME, "Swing high cap protecting bearish structure."),
                KeyLevelZone("gu_res_flip", "Broken Trendline Flip", 1.2705, 1.2720, "Active Supply", ZoneType.RESISTANCE, "Prior support turned resistance for short entries."),
                KeyLevelZone("gu_sup_target", "H4 Demand Pocket", 1.2615, 1.2635, "Downside Target", ZoneType.SUPPORT, "Untapped demand cluster from prior impulse.")
            ),
            bullishScenario = TradeScenario(
                id = "gu_bull",
                title = "Support Reclaim Long Signal",
                bias = TradeBias.BULLISH,
                triggerCondition = "H4 close back above 1.2725 clearing the broken trendline zone.",
                entryPrice = 1.2728,
                stopLossPrice = 1.2688,
                takeProfitPrice = 1.2795,
                invalidationNote = "Failure to hold 1.2700 on retest."
            ),
            bearishScenario = TradeScenario(
                id = "gu_bear",
                title = "Supply Retest Short Signal",
                bias = TradeBias.BEARISH,
                triggerCondition = "Bearish pin bar or engulfing rejection inside 1.2705–1.2720.",
                entryPrice = 1.2708,
                stopLossPrice = 1.2748,
                takeProfitPrice = 1.2625,
                invalidationNote = "Invalidated on H4 candle close above 1.2745."
            ),
            chartPatternPoints = listOf(
                1.2710f, 1.2738f, 1.2762f, 1.2742f, 1.2755f, 1.2724f,
                1.2695f, 1.2674f, 1.2692f, 1.2685f
            ),
            priceDecimals = 4,
            isSampleResponse = true
        )

        fun usdJpyH1Analysis(): ChartAnalysis = ChartAnalysis(
            id = "preset_usdjpy_h1",
            pair = "USDJPY",
            timeframe = "H1",
            currentPrice = 152.45,
            marketStatus = MarketStatus.OPEN,
            marketStatusNote = "Tokyo/London crossover liquidity — monitor US10Y yield correlation.",
            trendBias = TradeBias.BULLISH,
            signalSummary = "BUY continuation above 152.00 round-number support · SELL reversal below 151.75.",
            priceActionPhases = listOf(
                PriceActionPhase(
                    title = "Higher-Low Base",
                    rangeLabel = "151.30 – 152.10",
                    summary = "Clean ascending base forming higher lows into 152.00 psychological resistance.",
                    bias = TradeBias.NEUTRAL
                ),
                PriceActionPhase(
                    title = "152.00 Breakout Hold",
                    rangeLabel = "152.00 → 152.68",
                    summary = "Impulse break above 152.00 followed by controlled flag pullback to 152.45.",
                    bias = TradeBias.BULLISH
                )
            ),
            keyLevels = listOf(
                KeyLevelZone("uj_res", "Prior Swing High", 153.20, 153.45, "Target Zone", ZoneType.RESISTANCE, "Major upside liquidity pool."),
                KeyLevelZone("uj_sup", "152.00 Flip Zone", 151.95, 152.15, "Key Support", ZoneType.SUPPORT, "Psychological round number acting as demand."),
                KeyLevelZone("uj_imb", "Tokyo Open Imbalance", 151.40, 151.65, "Lower Support", ZoneType.IMBALANCE, "Origin of breakout leg.")
            ),
            bullishScenario = TradeScenario(
                id = "uj_bull",
                title = "Flag Continuation Long Signal",
                bias = TradeBias.BULLISH,
                triggerCondition = "Hold 152.10–152.25 on H1 pullback with bullish momentum candle.",
                entryPrice = 152.20,
                stopLossPrice = 151.75,
                takeProfitPrice = 153.30,
                invalidationNote = "H1 close back under 151.90."
            ),
            bearishScenario = TradeScenario(
                id = "uj_bear",
                title = "False Break Reversal Short Signal",
                bias = TradeBias.BEARISH,
                triggerCondition = "Decisive H1 breakdown below 151.90 invalidating the 152.00 breakout.",
                entryPrice = 151.85,
                stopLossPrice = 152.30,
                takeProfitPrice = 151.10,
                invalidationNote = "Reclaim above 152.35."
            ),
            chartPatternPoints = listOf(
                151.35f, 151.70f, 151.55f, 151.95f, 151.82f, 152.25f,
                152.68f, 152.52f, 152.38f, 152.45f
            ),
            priceDecimals = 2,
            isSampleResponse = true
        )

        fun btcUsdM30Analysis(): ChartAnalysis = ChartAnalysis(
            id = "preset_btcusd_m30",
            pair = "BTCUSD",
            timeframe = "M30",
            currentPrice = 94280.0,
            marketStatus = MarketStatus.OPEN,
            marketStatusNote = "24/7 crypto spot & perp market active — monitor funding and NY session volatility.",
            trendBias = TradeBias.BULLISH,
            signalSummary = "BUY retest at 93,900–94,150 · SELL deviation sweep below 93,900.",
            priceActionPhases = listOf(
                PriceActionPhase(
                    title = "Range Compression",
                    rangeLabel = "92,400 – 94,100",
                    summary = "Multi-hour coiled structure building liquidity above 94,100.",
                    bias = TradeBias.NEUTRAL
                ),
                PriceActionPhase(
                    title = "Range High Breakout",
                    rangeLabel = "94,100 → 94,850",
                    summary = "Impulse push testing upper resistance with shallow pullback to 94,280.",
                    bias = TradeBias.BULLISH
                )
            ),
            keyLevels = listOf(
                KeyLevelZone("btc_res", "Psychological Supply", 95400.0, 95800.0, "Target Zone", ZoneType.RESISTANCE, "Round-number resistance cluster."),
                KeyLevelZone("btc_sup", "Breakout Retest", 93900.0, 94150.0, "Support Flip", ZoneType.SUPPORT, "Prior range ceiling now acting as support."),
                KeyLevelZone("btc_imb", "Mid-Range Pocket", 92800.0, 93200.0, "Imbalance", ZoneType.IMBALANCE, "Unfilled M30 impulse pocket.")
            ),
            bullishScenario = TradeScenario(
                id = "btc_bull",
                title = "Breakout Retest Long Signal",
                bias = TradeBias.BULLISH,
                triggerCondition = "Hold 93,900–94,150 on M30 close with rising volume.",
                entryPrice = 94150.0,
                stopLossPrice = 93550.0,
                takeProfitPrice = 95550.0,
                invalidationNote = "M30 close back below 93,800."
            ),
            bearishScenario = TradeScenario(
                id = "btc_bear",
                title = "Deviation Sweep Short Signal",
                bias = TradeBias.BEARISH,
                triggerCondition = "Failure back below 93,900 sweeps toward 93,000 imbalance.",
                entryPrice = 93850.0,
                stopLossPrice = 94450.0,
                takeProfitPrice = 92850.0,
                invalidationNote = "Reclaim above 94,500."
            ),
            chartPatternPoints = listOf(
                92700f, 93400f, 92450f, 93600f, 94050f, 93250f,
                94120f, 94850f, 94360f, 94280f
            ),
            priceDecimals = 0,
            isSampleResponse = true
        )
    }
}
