package com.example.domain

import kotlin.math.abs

enum class ContractSpec(
    val displayName: String,
    val dollarPerPointPerLot: Double,
    val priceDecimals: Int
) {
    GOLD_XAUUSD("Gold · XAUUSD ($100/pt)", 100.0, 2),
    FX_STANDARD("FX Major ($100k/lot)", 100000.0, 4),
    FX_JPY("FX JPY ($1k/lot)", 1000.0, 2),
    CRYPTO_INDEX("Crypto / Index ($1/pt)", 1.0, 2)
}

enum class TradeDirection(val label: String) {
    LONG("LONG"),
    SHORT("SHORT")
}

data class PositionMetrics(
    val isValid: Boolean,
    val validationHint: String?,
    val stopDistance: Double,
    val targetDistance: Double,
    val riskRewardRatio: Double,
    val riskAmountUsd: Double,
    val rewardAmountUsd: Double,
    val recommendedLots: Double,
    val impliedRiskPercent: Double = 0.0
)

object PositionCalculator {
    fun calculate(
        direction: TradeDirection,
        accountBalance: Double,
        riskPercent: Double,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double,
        contractSpec: ContractSpec
    ): PositionMetrics {
        val levelValidation = validatePriceLevels(
            direction = direction,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit = takeProfit
        )
        if (accountBalance <= 0.0 || riskPercent <= 0.0) {
            return invalidMetrics("Enter positive values for Account Balance and Risk %.")
        }
        if (levelValidation != null) {
            return invalidMetrics(levelValidation)
        }

        val stopDist = abs(entryPrice - stopLoss)
        val targetDist = abs(takeProfit - entryPrice)
        val rr = if (stopDist > 0.0) targetDist / stopDist else 0.0
        val riskUsd = accountBalance * (riskPercent / 100.0)
        val riskPerLot = stopDist * contractSpec.dollarPerPointPerLot
        val lots = if (riskPerLot > 0.0) riskUsd / riskPerLot else 0.0
        val rewardUsd = riskUsd * rr

        return PositionMetrics(
            isValid = true,
            validationHint = null,
            stopDistance = stopDist,
            targetDistance = targetDist,
            riskRewardRatio = rr,
            riskAmountUsd = riskUsd,
            rewardAmountUsd = rewardUsd,
            recommendedLots = lots,
            impliedRiskPercent = riskPercent
        )
    }

    fun calculateFromLotSize(
        direction: TradeDirection,
        accountBalance: Double,
        lotSize: Double,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double,
        contractSpec: ContractSpec
    ): PositionMetrics {
        if (lotSize <= 0.0) {
            return invalidMetrics("Enter a positive Lot Size or Risk %.")
        }
        val levelValidation = validatePriceLevels(
            direction = direction,
            entryPrice = entryPrice,
            stopLoss = stopLoss,
            takeProfit = takeProfit
        )
        if (levelValidation != null) {
            return invalidMetrics(levelValidation)
        }

        val stopDist = abs(entryPrice - stopLoss)
        val targetDist = abs(takeProfit - entryPrice)
        val rr = if (stopDist > 0.0) targetDist / stopDist else 0.0
        val riskUsd = lotSize * stopDist * contractSpec.dollarPerPointPerLot
        val rewardUsd = lotSize * targetDist * contractSpec.dollarPerPointPerLot
        val impliedRiskPct = if (accountBalance > 0.0) (riskUsd / accountBalance) * 100.0 else 0.0

        return PositionMetrics(
            isValid = true,
            validationHint = null,
            stopDistance = stopDist,
            targetDistance = targetDist,
            riskRewardRatio = rr,
            riskAmountUsd = riskUsd,
            rewardAmountUsd = rewardUsd,
            recommendedLots = lotSize,
            impliedRiskPercent = impliedRiskPct
        )
    }

    private fun validatePriceLevels(
        direction: TradeDirection,
        entryPrice: Double,
        stopLoss: Double,
        takeProfit: Double
    ): String? {
        if (entryPrice <= 0.0 || stopLoss <= 0.0 || takeProfit <= 0.0) {
            return "Enter positive values for Entry Price, Stop Loss, and Take Profit."
        }
        val slValid = when (direction) {
            TradeDirection.LONG -> stopLoss < entryPrice
            TradeDirection.SHORT -> stopLoss > entryPrice
        }
        if (!slValid) {
            return if (direction == TradeDirection.LONG) {
                "For a BUY (LONG) setup, Stop Loss must be below Entry Price."
            } else {
                "For a SELL (SHORT) setup, Stop Loss must be above Entry Price."
            }
        }
        val tpValid = when (direction) {
            TradeDirection.LONG -> takeProfit > entryPrice
            TradeDirection.SHORT -> takeProfit < entryPrice
        }
        if (!tpValid) {
            return if (direction == TradeDirection.LONG) {
                "For a BUY (LONG) setup, Take Profit must be above Entry Price."
            } else {
                "For a SELL (SHORT) setup, Take Profit must be below Entry Price."
            }
        }
        return null
    }

    private fun invalidMetrics(hint: String) = PositionMetrics(
        isValid = false,
        validationHint = hint,
        stopDistance = 0.0,
        targetDistance = 0.0,
        riskRewardRatio = 0.0,
        riskAmountUsd = 0.0,
        rewardAmountUsd = 0.0,
        recommendedLots = 0.0,
        impliedRiskPercent = 0.0
    )

    fun calculateTakeProfitForRR(
        direction: TradeDirection,
        entryPrice: Double,
        stopLoss: Double,
        targetRR: Double
    ): Double? {
        if (entryPrice <= 0.0 || stopLoss <= 0.0 || targetRR <= 0.0) return null
        val stopDist = when (direction) {
            TradeDirection.LONG -> if (stopLoss < entryPrice) entryPrice - stopLoss else return null
            TradeDirection.SHORT -> if (stopLoss > entryPrice) stopLoss - entryPrice else return null
        }
        return when (direction) {
            TradeDirection.LONG -> entryPrice + (stopDist * targetRR)
            TradeDirection.SHORT -> (entryPrice - (stopDist * targetRR)).coerceAtLeast(0.0001)
        }
    }

    fun inferContractSpec(pair: String): ContractSpec {
        val upper = pair.uppercase()
        return when {
            upper.contains("XAU") || upper.contains("GOLD") -> ContractSpec.GOLD_XAUUSD
            upper.contains("JPY") -> ContractSpec.FX_JPY
            upper.contains("BTC") || upper.contains("ETH") || upper.contains("US30") || upper.contains("NAS") || upper.contains("SPX") -> ContractSpec.CRYPTO_INDEX
            else -> ContractSpec.FX_STANDARD
        }
    }
}
