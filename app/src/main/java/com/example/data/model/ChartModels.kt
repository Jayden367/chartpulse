package com.example.data.model

enum class MarketStatus(val label: String) {
    OPEN("Market Open"),
    CLOSED_ROLLOVER("Market Closed · Rollover"),
    LOW_LIQUIDITY("Low Liquidity Window")
}

enum class TradeBias(val label: String) {
    BULLISH("Bullish"),
    BEARISH("Bearish"),
    NEUTRAL("Neutral")
}

enum class ZoneType(val label: String) {
    SUPPORT("Support Flip"),
    RESISTANCE("Resistance"),
    IMBALANCE("Imbalance Fill"),
    SESSION_EXTREME("Session Extreme")
}

enum class PriceSnapTarget(val label: String) {
    ENTRY("Entry"),
    STOP_LOSS("Stop Loss"),
    TAKE_PROFIT("Take Profit")
}

data class PriceActionPhase(
    val title: String,
    val rangeLabel: String,
    val summary: String,
    val bias: TradeBias
)

data class KeyLevelZone(
    val id: String,
    val title: String,
    val lowPrice: Double,
    val highPrice: Double,
    val role: String,
    val type: ZoneType,
    val note: String
) {
    val midPrice: Double
        get() = (lowPrice + highPrice) / 2.0
}

data class TradeScenario(
    val id: String,
    val title: String,
    val bias: TradeBias,
    val triggerCondition: String,
    val entryPrice: Double,
    val stopLossPrice: Double,
    val takeProfitPrice: Double,
    val invalidationNote: String
)

data class ChartAnalysis(
    val id: String,
    val pair: String,
    val timeframe: String,
    val currentPrice: Double,
    val marketStatus: MarketStatus,
    val marketStatusNote: String,
    val trendBias: TradeBias = TradeBias.NEUTRAL,
    val signalSummary: String = "",
    val priceActionPhases: List<PriceActionPhase>,
    val keyLevels: List<KeyLevelZone>,
    val bullishScenario: TradeScenario,
    val bearishScenario: TradeScenario,
    val chartPatternPoints: List<Float>,
    val priceDecimals: Int = 2,
    val imageUri: String? = null,
    val isSampleResponse: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class AnalystMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val isSampleNotice: Boolean = false
)
