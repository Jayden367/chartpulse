package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ChartAnalysis
import com.example.data.model.TradeScenario
import com.example.domain.ContractSpec
import com.example.domain.PositionMetrics
import com.example.domain.TradeDirection
import com.example.ui.PlannerUiState
import com.example.ui.theme.BullionGold
import com.example.ui.theme.BullionGoldContainer
import com.example.ui.theme.CrimsonBear
import com.example.ui.theme.CrimsonBearContainer
import com.example.ui.theme.EmeraldBull
import com.example.ui.theme.EmeraldBullContainer
import com.example.ui.theme.MonoLabelSmall
import com.example.ui.theme.MonoPriceLarge
import com.example.ui.theme.MonoPriceMedium
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.OnBullionGold
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import java.util.Locale

@Composable
fun PlannerScreen(
    plannerState: PlannerUiState,
    currentAnalysis: ChartAnalysis,
    presets: List<ChartAnalysis>,
    onDirectionChange: (TradeDirection) -> Unit,
    onPairChange: (String) -> Unit,
    onTimeframeChange: (String) -> Unit,
    onSelectPreset: (String) -> Unit,
    onContractSpecChange: (ContractSpec) -> Unit,
    onBalanceChange: (String) -> Unit,
    onRiskPercentChange: (String) -> Unit,
    onLotSizeChange: (String) -> Unit,
    onEntryChange: (String) -> Unit,
    onStopLossChange: (String) -> Unit,
    onTakeProfitChange: (String) -> Unit,
    onApplyRiskRewardTarget: (Double) -> Unit,
    onCalculate: () -> Unit,
    onNotesChange: (String) -> Unit,
    onSpreadToggle: (Boolean) -> Unit,
    onCandleToggle: (Boolean) -> Unit,
    onApplyScenario: (TradeScenario) -> Unit,
    onSaveSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = plannerState.metrics
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Live Risk / Reward & Lot Size Telemetry Card
            TelemetryCard(
                pair = plannerState.pair,
                timeframe = plannerState.timeframe,
                direction = plannerState.direction,
                contractSpec = plannerState.contractSpec,
                metrics = metrics,
                decimals = plannerState.contractSpec.priceDecimals,
                calculationStatusMessage = plannerState.calculationStatusMessage
            )

            // 2. Editable Position & Risk Calculator Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "POSITION & RISK CALCULATOR",
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DirectionButton(
                        label = "BUY (LONG)",
                        selected = plannerState.direction == TradeDirection.LONG,
                        activeColor = EmeraldBull,
                        activeContainer = EmeraldBullContainer,
                        modifier = Modifier.weight(1f).testTag("direction_long"),
                        onClick = { onDirectionChange(TradeDirection.LONG) }
                    )
                    DirectionButton(
                        label = "SELL (SHORT)",
                        selected = plannerState.direction == TradeDirection.SHORT,
                        activeColor = CrimsonBear,
                        activeContainer = CrimsonBearContainer,
                        modifier = Modifier.weight(1f).testTag("direction_short"),
                        onClick = { onDirectionChange(TradeDirection.SHORT) }
                    )
                }

                NumericPlannerField(
                    value = plannerState.entryInput,
                    onValueChange = onEntryChange,
                    label = "Entry Price",
                    testTag = "entry_price_input",
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NumericPlannerField(
                        value = plannerState.stopLossInput,
                        onValueChange = onStopLossChange,
                        label = "Stop Loss",
                        testTag = "stop_loss_input",
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                        modifier = Modifier.weight(1f)
                    )
                    NumericPlannerField(
                        value = plannerState.takeProfitInput,
                        onValueChange = onTakeProfitChange,
                        label = "Take Profit",
                        testTag = "take_profit_input",
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NumericPlannerField(
                        value = plannerState.balanceInput,
                        onValueChange = onBalanceChange,
                        label = "Account Balance ($)",
                        testTag = "balance_input",
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                        modifier = Modifier.weight(1f)
                    )
                    NumericPlannerField(
                        value = plannerState.riskPercentInput,
                        onValueChange = onRiskPercentChange,
                        label = "Risk Percentage (%)",
                        testTag = "risk_percent_input",
                        imeAction = ImeAction.Next,
                        onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NumericPlannerField(
                        value = plannerState.lotSizeInput,
                        onValueChange = onLotSizeChange,
                        label = "Lot Size",
                        testTag = "lot_size_input",
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onCalculate()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PlannerTextField(
                        value = plannerState.pair,
                        onValueChange = onPairChange,
                        label = "Pair / Symbol",
                        testTag = "planner_pair_input",
                        modifier = Modifier.weight(1f)
                    )
                    PlannerTextField(
                        value = plannerState.timeframe,
                        onValueChange = onTimeframeChange,
                        label = "TF",
                        testTag = "planner_tf_input",
                        modifier = Modifier.weight(0.65f)
                    )
                }

                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onCalculate()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("calculate_position_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Calculate,
                        contentDescription = null,
                        tint = BullionGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calculate",
                        style = MaterialTheme.typography.titleSmall,
                        color = BullionGold
                    )
                }

                // Quick Risk & Auto TP helpers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Risk:", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                    listOf("0.5", "1.0", "2.0", "3.0").forEach { presetRisk ->
                        FilterChip(
                            selected = !plannerState.isCustomLotMode && plannerState.riskPercentInput == presetRisk,
                            onClick = { onRiskPercentChange(presetRisk) },
                            label = { Text("$presetRisk%", style = MonoLabelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BullionGoldContainer,
                                selectedLabelColor = BullionGold
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("risk_chip_${presetRisk.replace(".", "_")}")
                        )
                    }
                    Text("TP:", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                    listOf(1.5, 2.0, 3.0).forEach { rr ->
                        FilterChip(
                            selected = false,
                            onClick = { onApplyRiskRewardTarget(rr) },
                            label = { Text("1:${String.format(Locale.US, "%.1f", rr)}", style = MonoLabelSmall) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("auto_rr_${rr.toString().replace(".", "_")}")
                        )
                    }
                }
            }

            // 3. Pair Presets, Signals & Contract Spec
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Signals:",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateTextSecondary
                    )
                    FilterChip(
                        selected = false,
                        onClick = { onApplyScenario(currentAnalysis.bullishScenario) },
                        label = { Text("${currentAnalysis.pair} Buy Signal") },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("import_bullish_chip")
                    )
                    FilterChip(
                        selected = false,
                        onClick = { onApplyScenario(currentAnalysis.bearishScenario) },
                        label = { Text("${currentAnalysis.pair} Sell Signal") },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("import_bearish_chip")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pairs:",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateTextSecondary
                    )
                    presets.forEach { preset ->
                        FilterChip(
                            selected = plannerState.pair == preset.pair,
                            onClick = { onSelectPreset(preset.id) },
                            label = { Text(preset.pair, style = MonoLabelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BullionGoldContainer,
                                selectedLabelColor = BullionGold
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("planner_pair_${preset.pair.lowercase()}")
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ContractSpec.entries.forEach { spec ->
                        FilterChip(
                            selected = plannerState.contractSpec == spec,
                            onClick = { onContractSpecChange(spec) },
                            label = { Text(spec.displayName, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BullionGoldContainer,
                                selectedLabelColor = BullionGold
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("spec_${spec.name.lowercase()}")
                        )
                    }
                }

                PlannerTextField(
                    value = plannerState.notesInput,
                    onValueChange = onNotesChange,
                    label = "Execution Trigger & Notes",
                    testTag = "planner_notes_input",
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 4. Pre-Trade Discipline Checklist
            val rrHealthy = metrics.isValid && metrics.riskRewardRatio >= 1.5
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "PRE-ENTRY DISCIPLINE CHECKLIST",
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateTextSecondary
                )
                ChecklistRow(
                    label = "Spreads normalized after rollover / market open",
                    checked = plannerState.spreadNormalized,
                    onCheckedChange = onSpreadToggle,
                    testTag = "check_spreads"
                )
                ChecklistRow(
                    label = "${plannerState.timeframe} confirmation candle closed at key level",
                    checked = plannerState.candleConfirmed,
                    onCheckedChange = onCandleToggle,
                    testTag = "check_candle"
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (rrHealthy) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                        contentDescription = "Risk reward validation",
                        tint = if (rrHealthy) EmeraldBull else BullionGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (rrHealthy) {
                            "Reward-to-Risk is >= 1.50R (${String.format(Locale.US, "%.2f", metrics.riskRewardRatio)}R)"
                        } else {
                            "Aim for >= 1.50R reward-to-risk before committing capital"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (rrHealthy) EmeraldBull else SlateTextSecondary
                    )
                }
            }

            // 5. Save CTA (stores setup and opens Journal history)
            Button(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSaveSetup()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BullionGold,
                    contentColor = OnBullionGold
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_setup_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.BookmarkAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.save_to_journal),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun NumericPlannerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    testTag: String,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { incoming ->
            val filtered = filterPlannerNumeric(incoming.text)
            val clampedEnd = incoming.selection.end.coerceIn(0, filtered.length)
            textFieldValue = incoming.copy(
                text = filtered,
                selection = TextRange(clampedEnd)
            )
            if (filtered != value) {
                onValueChange(filtered)
            }
        },
        label = { Text(label) },
        singleLine = true,
        textStyle = MonoPriceMedium.copy(color = SlateTextPrimary),
        colors = plannerInputColors(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        modifier = modifier.testTag(testTag)
    )
}

@Composable
private fun PlannerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    testTag: String,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { incoming ->
            textFieldValue = incoming
            if (incoming.text != value) {
                onValueChange(incoming.text)
            }
        },
        label = { Text(label) },
        singleLine = singleLine,
        maxLines = maxLines,
        colors = plannerInputColors(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        modifier = modifier.testTag(testTag)
    )
}

private fun filterPlannerNumeric(raw: String): String {
    val normalized = raw.replace(',', '.')
    return normalized.filterIndexed { idx, c -> c.isDigit() || (c == '.' && normalized.indexOf('.') == idx) }
}

@Composable
private fun plannerInputColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = SlateTextPrimary,
    unfocusedTextColor = SlateTextPrimary,
    cursorColor = BullionGold,
    focusedBorderColor = BullionGold,
    unfocusedBorderColor = ObsidianBorder,
    focusedContainerColor = ObsidianSurfaceElevated,
    unfocusedContainerColor = ObsidianSurfaceElevated,
    focusedLabelColor = BullionGold,
    unfocusedLabelColor = SlateTextSecondary
)

@Composable
private fun TelemetryCard(
    pair: String,
    timeframe: String,
    direction: TradeDirection,
    contractSpec: ContractSpec,
    metrics: PositionMetrics,
    decimals: Int,
    calculationStatusMessage: String?
) {
    val dirColor = if (direction == TradeDirection.LONG) EmeraldBull else CrimsonBear
    val distSummary = if (metrics.isValid) {
        when (contractSpec) {
            ContractSpec.FX_STANDARD -> {
                val slPips = metrics.stopDistance * 10000.0
                val tpPips = metrics.targetDistance * 10000.0
                "${String.format(Locale.US, "%.0f", slPips)}/${String.format(Locale.US, "%.0f", tpPips)}p"
            }
            ContractSpec.FX_JPY -> {
                val slPips = metrics.stopDistance * 100.0
                val tpPips = metrics.targetDistance * 100.0
                "${String.format(Locale.US, "%.0f", slPips)}/${String.format(Locale.US, "%.0f", tpPips)}p"
            }
            else -> "${String.format(Locale.US, "%.${decimals}f", metrics.stopDistance)}/${String.format(Locale.US, "%.${decimals}f", metrics.targetDistance)}"
        }
    } else "--"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${pair.ifBlank { "CUSTOM" }} · ${timeframe.ifBlank { "M15" }}",
                    style = MaterialTheme.typography.titleMedium,
                    color = SlateTextPrimary
                )
                Text(
                    text = if (metrics.isValid) "Position Size & Risk Live" else metrics.validationHint ?: "Check Levels",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (metrics.isValid) SlateTextSecondary else CrimsonBear
                )
            }
            Text(
                text = if (direction == TradeDirection.LONG) "BUY" else "SELL",
                style = MonoLabelSmall,
                color = dirColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(dirColor.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("RISK : REWARD", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                Text(
                    text = if (metrics.isValid) "1 : ${String.format(Locale.US, "%.2f", metrics.riskRewardRatio)}" else "--",
                    style = MonoPriceLarge,
                    color = BullionGold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("LOT SIZE", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                Text(
                    text = if (metrics.isValid) "${String.format(Locale.US, "%.2f", metrics.recommendedLots)} lots" else "--",
                    style = MonoPriceLarge,
                    color = SlateTextPrimary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianSurfaceElevated)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricTile(
                label = "RISK ($)",
                value = if (metrics.isValid) "-$${String.format(Locale.US, "%.2f", metrics.riskAmountUsd)}" else "--",
                color = CrimsonBear
            )
            MetricTile(
                label = "PROFIT ($)",
                value = if (metrics.isValid) "+$${String.format(Locale.US, "%.2f", metrics.rewardAmountUsd)}" else "--",
                color = EmeraldBull
            )
            MetricTile(
                label = if (contractSpec == ContractSpec.FX_STANDARD || contractSpec == ContractSpec.FX_JPY) "SL/TP PIPS" else "SL/TP DIST",
                value = distSummary,
                color = SlateTextPrimary
            )
        }

        if (!calculationStatusMessage.isNullOrBlank()) {
            Text(
                text = calculationStatusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = if (metrics.isValid) BullionGold else CrimsonBear,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (metrics.isValid) BullionGoldContainer else CrimsonBearContainer)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("planner_calc_summary")
            )
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
        Text(text = value, style = MonoPriceMedium, color = color)
    }
}

@Composable
private fun DirectionButton(
    label: String,
    selected: Boolean,
    activeColor: Color,
    activeContainer: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) activeContainer else ObsidianSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (selected) activeColor else ObsidianBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MonoLabelSmall,
            color = if (selected) activeColor else SlateTextSecondary
        )
    }
}

@Composable
private fun ChecklistRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable { onCheckedChange(!checked) }
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = BullionGold, checkmarkColor = OnBullionGold)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SlateTextPrimary
        )
    }
}
