package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CandlestickChart
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import kotlin.math.abs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AnalystMessage
import com.example.data.model.ChartAnalysis
import com.example.data.model.KeyLevelZone
import com.example.data.model.MarketStatus
import com.example.data.model.PriceActionPhase
import com.example.data.model.PriceSnapTarget
import com.example.data.model.TradeBias
import com.example.data.model.TradeScenario
import com.example.data.model.ZoneType
import com.example.ui.AnalysisUiState
import com.example.ui.PlannerUiState
import com.example.ui.components.MarketCautionBanner
import com.example.ui.components.PriceActionCanvas
import com.example.ui.components.SampleAiNoticeBanner
import com.example.ui.theme.AmberCaution
import com.example.ui.theme.BullionGold
import com.example.ui.theme.BullionGoldContainer
import com.example.ui.theme.CrimsonBear
import com.example.ui.theme.CrimsonBearContainer
import com.example.ui.theme.EmeraldBull
import com.example.ui.theme.EmeraldBullContainer
import com.example.ui.theme.MonoLabelSmall
import com.example.ui.theme.MonoPriceLarge
import com.example.ui.theme.MonoPriceMedium
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.OnBullionGold
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary

@Composable
fun AnalystScreen(
    analysis: ChartAnalysis,
    analysisState: AnalysisUiState,
    presets: List<ChartAnalysis>,
    plannerState: PlannerUiState,
    messages: List<AnalystMessage>,
    isChatLoading: Boolean,
    onSelectPreset: (String) -> Unit,
    onAnalyzeScreenshot: (Uri?, String, String, String) -> Unit,
    onLoadScenarioToPlanner: (TradeScenario) -> Unit,
    onSnapPriceToPlanner: (Double, PriceSnapTarget) -> Unit,
    onAskFollowUp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var showScanDialog by remember { mutableStateOf(false) }
    var pairHintInput by remember { mutableStateOf(analysis.pair) }
    var tfHintInput by remember { mutableStateOf(analysis.timeframe) }
    var contextHintInput by remember { mutableStateOf("") }
    var questionInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingImageUri = uri
            pairHintInput = analysis.pair
            tfHintInput = analysis.timeframe
            showScanDialog = true
        }
    }

    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            containerColor = ObsidianSurface,
            title = { Text("Analyze Chart Screenshot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Add optional pair and timeframe hints for the technical breakdown.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextSecondary
                    )
                    OutlinedTextField(
                        value = pairHintInput,
                        onValueChange = { pairHintInput = it.uppercase() },
                        label = { Text("Instrument / Pair") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("scan_pair_input")
                    )
                    OutlinedTextField(
                        value = tfHintInput,
                        onValueChange = { tfHintInput = it.uppercase() },
                        label = { Text("Timeframe (e.g., M15, H1)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("scan_tf_input")
                    )
                    OutlinedTextField(
                        value = contextHintInput,
                        onValueChange = { contextHintInput = it },
                        label = { Text("Focus / Question (optional)") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("scan_context_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showScanDialog = false
                        onAnalyzeScreenshot(pendingImageUri, pairHintInput, tfHintInput, contextHintInput)
                    },
                    modifier = Modifier.testTag("confirm_scan_button")
                ) {
                    Text("Run Analysis")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Chart Header + Screenshot Upload & Quick Pair Analyze CTA
            item {
                HeroChartCard(
                    analysis = analysis,
                    isLoading = analysisState is AnalysisUiState.Loading,
                    onPickScreenshot = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onOpenQuickAnalyze = {
                        pendingImageUri = null
                        pairHintInput = analysis.pair
                        tfHintInput = analysis.timeframe
                        showScanDialog = true
                    }
                )
            }

            // 2. Preset Market Switcher
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val selected = preset.id == analysis.id
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectPreset(preset.id) },
                            label = {
                                Text(
                                    text = "${preset.pair} · ${preset.timeframe}",
                                    style = MonoLabelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BullionGoldContainer,
                                selectedLabelColor = BullionGold
                            ),
                            modifier = Modifier.testTag("preset_chip_${preset.pair.lowercase()}")
                        )
                    }
                }
            }

            // 3. Required In-App Sample Preview Notice
            item {
                SampleAiNoticeBanner()
            }

            // 4. Market Status & Rollover Caution
            item {
                if (analysis.marketStatus != MarketStatus.OPEN) {
                    MarketCautionBanner(
                        statusTitle = analysis.marketStatus.label,
                        statusDetail = analysis.marketStatusNote
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(EmeraldBullContainer.copy(alpha = 0.55f))
                            .border(1.dp, EmeraldBull.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = "Market status",
                            tint = EmeraldBull,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${analysis.marketStatus.label} — ${analysis.marketStatusNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTextPrimary
                        )
                    }
                }
            }

            // 4b. Error State Banner (if chart analysis fails)
            if (analysisState is AnalysisUiState.Error) {
                item {
                    MarketCautionBanner(
                        statusTitle = "Analysis Error",
                        statusDetail = analysisState.message
                    )
                }
            }

            // 5. Interactive Price Action & Key Zone Visualizer
            item {
                PriceActionCanvas(
                    analysis = analysis,
                    plannerEntry = plannerState.entryValue.takeIf { plannerState.pair == analysis.pair },
                    plannerStopLoss = plannerState.stopLossValue.takeIf { plannerState.pair == analysis.pair },
                    plannerTakeProfit = plannerState.takeProfitValue.takeIf { plannerState.pair == analysis.pair }
                )
            }

            // 6. Buy & Sell Signal Overview + Scenarios to Watch
            item {
                SectionHeader(
                    title = "Buy & Sell Signal Analysis",
                    subtitle = "Signal bias & trigger plans · Tap Plan to calculate position size"
                )
            }

            item {
                SignalOverviewCard(analysis = analysis)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScenarioCard(
                        scenario = analysis.bullishScenario,
                        decimals = analysis.priceDecimals,
                        testTag = "load_bullish_scenario",
                        onLoadToPlanner = { onLoadScenarioToPlanner(analysis.bullishScenario) }
                    )
                    ScenarioCard(
                        scenario = analysis.bearishScenario,
                        decimals = analysis.priceDecimals,
                        testTag = "load_bearish_scenario",
                        onLoadToPlanner = { onLoadScenarioToPlanner(analysis.bearishScenario) }
                    )
                }
            }

            // 7. Key Levels & Zones (Tap to Snap Entry / SL / TP)
            item {
                SectionHeader(
                    title = "Key Support & Resistance Zones",
                    subtitle = "Snap any zone midpoint directly to your trade planner"
                )
            }

            items(analysis.keyLevels, key = { it.id }) { zone ->
                KeyLevelCard(
                    zone = zone,
                    decimals = analysis.priceDecimals,
                    onSnap = { target -> onSnapPriceToPlanner(zone.midPrice, target) }
                )
            }

            // 8. Price Action Breakdown Phases
            item {
                SectionHeader(
                    title = "Price Action Breakdown",
                    subtitle = "Sequential structure leading into current price"
                )
            }

            items(analysis.priceActionPhases) { phase ->
                PriceActionPhaseCard(phase = phase)
            }

            // 9. Interactive AI Trade Desk Q&A
            item {
                SectionHeader(
                    title = "Trade Desk Q&A",
                    subtitle = "Ask about entries, stop placement, or spread conditions"
                )
            }

            item {
                AnalystChatPanel(
                    messages = messages,
                    isLoading = isChatLoading,
                    questionInput = questionInput,
                    onQuestionChange = { questionInput = it },
                    onQuickPrompt = { prompt ->
                        onAskFollowUp(prompt)
                    },
                    onSend = {
                        if (questionInput.isNotBlank()) {
                            onAskFollowUp(questionInput)
                            questionInput = ""
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HeroChartCard(
    analysis: ChartAnalysis,
    isLoading: Boolean,
    onPickScreenshot: () -> Unit,
    onOpenQuickAnalyze: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(22.dp))
    ) {
        if (analysis.imageUri != null) {
            AsyncImage(
                model = analysis.imageUri,
                contentDescription = "Analyzed chart screenshot",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(190.dp),
                alpha = 0.42f
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_chart_hero_1795476156781),
                contentDescription = "ChartPulse hero chart art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(190.dp),
                alpha = 0.55f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ObsidianBg.copy(alpha = 0.25f),
                            ObsidianSurface.copy(alpha = 0.94f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CandlestickChart,
                            contentDescription = null,
                            tint = BullionGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = analysis.pair,
                            style = MaterialTheme.typography.headlineMedium,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = analysis.timeframe,
                            style = MonoLabelSmall,
                            color = BullionGold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BullionGoldContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = String.format("%.${analysis.priceDecimals}f", analysis.currentPrice),
                        style = MonoPriceLarge,
                        color = SlateTextPrimary
                    )
                }

                val statusColor = when (analysis.marketStatus) {
                    MarketStatus.OPEN -> EmeraldBull
                    MarketStatus.CLOSED_ROLLOVER -> AmberCaution
                    MarketStatus.LOW_LIQUIDITY -> AmberCaution
                }
                Text(
                    text = if (analysis.marketStatus == MarketStatus.OPEN) "LIVE" else "CLOSED",
                    style = MonoLabelSmall,
                    color = statusColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.16f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenQuickAnalyze,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("analyze_pair_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueryStats,
                        contentDescription = "Analyze pair",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.analyze_pair))
                }

                Button(
                    onClick = onPickScreenshot,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BullionGold,
                        contentColor = OnBullionGold
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("pick_chart_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = OnBullionGold,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.AddPhotoAlternate,
                            contentDescription = "Pick chart screenshot",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.pick_chart_screenshot))
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalOverviewCard(analysis: ChartAnalysis) {
    val biasColor = when (analysis.trendBias) {
        TradeBias.BULLISH -> EmeraldBull
        TradeBias.BEARISH -> CrimsonBear
        TradeBias.NEUTRAL -> BullionGold
    }
    val biasBadge = when (analysis.trendBias) {
        TradeBias.BULLISH -> "BUY WATCH"
        TradeBias.BEARISH -> "SELL WATCH"
        TradeBias.NEUTRAL -> "RANGE NEUTRAL"
    }
    val nearestSupport = analysis.keyLevels.firstOrNull { it.type == ZoneType.SUPPORT }
    val nearestResistance = analysis.keyLevels.firstOrNull { it.type == ZoneType.RESISTANCE }
    val dec = analysis.priceDecimals

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSurface)
            .border(1.dp, biasColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SIGNAL BIAS (${analysis.pair} · ${analysis.timeframe})",
                style = MaterialTheme.typography.labelMedium,
                color = SlateTextSecondary
            )
            Text(
                text = biasBadge,
                style = MonoLabelSmall,
                color = biasColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(biasColor.copy(alpha = 0.16f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        if (analysis.signalSummary.isNotBlank()) {
            Text(
                text = analysis.signalSummary,
                style = MaterialTheme.typography.bodySmall,
                color = SlateTextPrimary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            nearestSupport?.let { sup ->
                Text(
                    text = "SUP: ${String.format("%.${dec}f", sup.lowPrice)}–${String.format("%.${dec}f", sup.highPrice)}",
                    style = MonoLabelSmall,
                    color = EmeraldBull
                )
            }
            nearestResistance?.let { res ->
                Text(
                    text = "RES: ${String.format("%.${dec}f", res.lowPrice)}–${String.format("%.${dec}f", res.highPrice)}",
                    style = MonoLabelSmall,
                    color = CrimsonBear
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = SlateTextPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = SlateTextSecondary
        )
    }
}

@Composable
private fun ScenarioCard(
    scenario: TradeScenario,
    decimals: Int,
    testTag: String,
    onLoadToPlanner: () -> Unit
) {
    val isBull = scenario.bias == TradeBias.BULLISH
    val accent = if (isBull) EmeraldBull else CrimsonBear
    val container = if (isBull) EmeraldBullContainer.copy(alpha = 0.35f) else CrimsonBearContainer.copy(alpha = 0.35f)
    val icon = if (isBull) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown
    val stopDist = abs(scenario.entryPrice - scenario.stopLossPrice).coerceAtLeast(0.00001)
    val targetDist = abs(scenario.takeProfitPrice - scenario.entryPrice)
    val rr = targetDist / stopDist

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ObsidianSurface)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(container)
                        .padding(7.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = scenario.bias.label,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SlateTextPrimary
                )
            }
            Text(
                text = "${if (isBull) "BUY" else "SELL"} · 1:${String.format("%.1f", rr)}",
                style = MonoLabelSmall,
                color = accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(container)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        Text(
            text = scenario.triggerCondition,
            style = MaterialTheme.typography.bodyMedium,
            color = SlateTextSecondary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PriceMiniMetric("ENTRY", scenario.entryPrice, decimals, SlateTextPrimary)
            PriceMiniMetric("STOP LOSS", scenario.stopLossPrice, decimals, CrimsonBear)
            PriceMiniMetric("TARGET", scenario.takeProfitPrice, decimals, EmeraldBull)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = scenario.invalidationNote,
                style = MaterialTheme.typography.bodySmall,
                color = SlateTextMuted,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedButton(
                onClick = onLoadToPlanner,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag(testTag)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Plan")
            }
        }
    }
}

@Composable
private fun PriceMiniMetric(label: String, value: Double, decimals: Int, valueColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
        Text(
            text = String.format("%.${decimals}f", value),
            style = MonoPriceMedium,
            color = valueColor
        )
    }
}

@Composable
private fun KeyLevelCard(
    zone: KeyLevelZone,
    decimals: Int,
    onSnap: (PriceSnapTarget) -> Unit
) {
    val accent = when (zone.type) {
        ZoneType.SUPPORT -> EmeraldBull
        ZoneType.RESISTANCE -> CrimsonBear
        ZoneType.IMBALANCE -> BullionGold
        ZoneType.SESSION_EXTREME -> SlateTextSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zone.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = SlateTextPrimary
                )
                Text(
                    text = zone.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = accent
                )
            }
            Text(
                text = "${String.format("%.${decimals}f", zone.lowPrice)} – ${String.format("%.${decimals}f", zone.highPrice)}",
                style = MonoPriceMedium,
                color = accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Text(
            text = zone.note,
            style = MaterialTheme.typography.bodySmall,
            color = SlateTextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PriceSnapTarget.entries.forEach { target ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurfaceElevated)
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                        .clickable { onSnap(target) }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .testTag("snap_${zone.id}_${target.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Set ${target.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceActionPhaseCard(phase: PriceActionPhase) {
    val badgeColor = when (phase.bias) {
        TradeBias.BULLISH -> EmeraldBull
        TradeBias.BEARISH -> CrimsonBear
        TradeBias.NEUTRAL -> BullionGold
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoGraph,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier.padding(top = 2.dp).size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phase.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = SlateTextPrimary
                )
                Text(
                    text = phase.rangeLabel,
                    style = MonoLabelSmall,
                    color = badgeColor
                )
            }
            Text(
                text = phase.summary,
                style = MaterialTheme.typography.bodySmall,
                color = SlateTextSecondary
            )
        }
    }
}

@Composable
private fun AnalystChatPanel(
    messages: List<AnalystMessage>,
    isLoading: Boolean,
    questionInput: String,
    onQuestionChange: (String) -> Unit,
    onQuickPrompt: (String) -> Unit,
    onSend: () -> Unit
) {
    val quickPrompts = listOf(
        "Plan Buy Signal",
        "Plan Sell Signal",
        "Support & Resistance Levels",
        "Where should Stop Loss go?",
        "Check Rollover Spread Risk"
    )

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
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { onQuickPrompt(prompt) },
                    label = { Text(prompt, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.testTag("quick_prompt_${prompt.hashCode()}")
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            messages.takeLast(6).forEach { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (msg.isUser) BullionGoldContainer.copy(alpha = 0.65f)
                            else ObsidianSurfaceElevated
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (msg.isUser) BullionGold else SlateTextPrimary
                    )
                }
            }
        }

        AnimatedVisibility(visible = isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("Analyzing setup...", style = MaterialTheme.typography.bodySmall, color = SlateTextSecondary)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = questionInput,
                onValueChange = onQuestionChange,
                placeholder = { Text(stringResource(R.string.ask_analyst_placeholder)) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("analyst_question_input")
            )
            IconButton(
                onClick = onSend,
                enabled = questionInput.isNotBlank() && !isLoading,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BullionGold)
                    .testTag("ask_analyst_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Ask analyst",
                    tint = OnBullionGold
                )
            }
        }
    }
}
