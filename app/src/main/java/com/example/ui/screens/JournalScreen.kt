package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.TradeSetupEntity
import com.example.domain.PositionCalculator
import com.example.domain.TradeDirection
import com.example.ui.JournalFilter
import com.example.ui.theme.BullionGold
import com.example.ui.theme.BullionGoldContainer
import com.example.ui.theme.CrimsonBear
import com.example.ui.theme.CrimsonBearContainer
import com.example.ui.theme.EmeraldBull
import com.example.ui.theme.EmeraldBullContainer
import com.example.ui.theme.MonoLabelSmall
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
fun JournalScreen(
    setups: List<TradeSetupEntity>,
    allSetups: List<TradeSetupEntity>,
    currentFilter: JournalFilter,
    onFilterChange: (JournalFilter) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onUpdateNotes: (Long, String) -> Unit,
    onRecordTrade: (
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
    ) -> Unit,
    onLoadIntoPlanner: (TradeSetupEntity) -> Unit,
    onDeleteSetup: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingNotesSetup by remember { mutableStateOf<TradeSetupEntity?>(null) }
    var draftNotes by remember { mutableStateOf("") }
    var showAddTradeForm by remember { mutableStateOf(false) }

    var recSymbol by remember { mutableStateOf("XAUUSD") }
    var recTimeframe by remember { mutableStateOf("M15") }
    var recDirection by remember { mutableStateOf(TradeDirection.LONG) }
    var recEntry by remember { mutableStateOf("3635.00") }
    var recSl by remember { mutableStateOf("3627.50") }
    var recTp by remember { mutableStateOf("3658.00") }
    var recLots by remember { mutableStateOf("0.10") }
    var recProfitLoss by remember { mutableStateOf("") }
    var recStatus by remember { mutableStateOf("ACTIVE") }
    var recNotes by remember { mutableStateOf("") }
    var formValidationError by remember { mutableStateOf<String?>(null) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val inputColors = OutlinedTextFieldDefaults.colors(
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

    if (showAddTradeForm) {
        AlertDialog(
            onDismissRequest = {
                formValidationError = null
                showAddTradeForm = false
            },
            containerColor = ObsidianSurface,
            title = { Text("Add Trade to Journal") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("XAUUSD", "EURUSD", "GBPUSD", "USDJPY").forEach { quickPair ->
                            FilterChip(
                                selected = recSymbol == quickPair,
                                onClick = { recSymbol = quickPair },
                                label = { Text(quickPair, style = MonoLabelSmall) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = recSymbol,
                            onValueChange = { recSymbol = it.uppercase().take(12) },
                            label = { Text("Symbol") },
                            singleLine = true,
                            colors = inputColors,
                            modifier = Modifier
                                .weight(1.3f)
                                .onFocusChanged { if (it.isFocused) keyboardController?.show() }
                                .testTag("record_pair_input")
                        )
                        OutlinedTextField(
                            value = recTimeframe,
                            onValueChange = { recTimeframe = it.uppercase().take(6) },
                            label = { Text("TF") },
                            singleLine = true,
                            colors = inputColors,
                            modifier = Modifier
                                .weight(0.8f)
                                .onFocusChanged { if (it.isFocused) keyboardController?.show() }
                                .testTag("record_tf_input")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = recDirection == TradeDirection.LONG,
                            onClick = { recDirection = TradeDirection.LONG },
                            label = { Text("BUY") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldBullContainer,
                                selectedLabelColor = EmeraldBull
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("record_dir_buy")
                        )
                        FilterChip(
                            selected = recDirection == TradeDirection.SHORT,
                            onClick = { recDirection = TradeDirection.SHORT },
                            label = { Text("SELL") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonBearContainer,
                                selectedLabelColor = CrimsonBear
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("record_dir_sell")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = recEntry,
                            onValueChange = {
                                recEntry = filterDecimalInput(it)
                                formValidationError = null
                            },
                            label = { Text("Entry Price") },
                            singleLine = true,
                            colors = inputColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { if (it.isFocused) keyboardController?.show() }
                                .testTag("record_entry_input")
                        )
                        OutlinedTextField(
                            value = recLots,
                            onValueChange = {
                                recLots = filterDecimalInput(it)
                                formValidationError = null
                            },
                            label = { Text("Lot Size") },
                            singleLine = true,
                            colors = inputColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { if (it.isFocused) keyboardController?.show() }
                                .testTag("record_lots_input")
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = recSl,
                            onValueChange = {
                                recSl = filterDecimalInput(it)
                                formValidationError = null
                            },
                            label = { Text("Stop Loss") },
                            singleLine = true,
                            colors = inputColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { if (it.isFocused) keyboardController?.show() }
                                .testTag("record_sl_input")
                        )
                        OutlinedTextField(
                            value = recTp,
                            onValueChange = {
                                recTp = filterDecimalInput(it)
                                formValidationError = null
                            },
                            label = { Text("Take Profit") },
                            singleLine = true,
                            colors = inputColors,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { if (it.isFocused) keyboardController?.show() }
                                .testTag("record_tp_input")
                        )
                    }

                    OutlinedTextField(
                        value = recProfitLoss,
                        onValueChange = { recProfitLoss = filterSignedDecimalInput(it) },
                        label = { Text("Profit/Loss ($)") },
                        placeholder = { Text("e.g. 230.00 or -75.00") },
                        singleLine = true,
                        colors = inputColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) keyboardController?.show() }
                            .testTag("record_pnl_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("PLANNED", "ACTIVE", "WON", "LOST").forEach { st ->
                            FilterChip(
                                selected = recStatus == st,
                                onClick = { recStatus = st },
                                label = { Text(st, style = MonoLabelSmall) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }

                    OutlinedTextField(
                        value = recNotes,
                        onValueChange = { recNotes = it },
                        label = { Text("Notes") },
                        maxLines = 3,
                        colors = inputColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { if (it.isFocused) keyboardController?.show() }
                            .testTag("record_notes_input")
                    )

                    if (!formValidationError.isNullOrBlank()) {
                        Text(
                            text = formValidationError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonBear,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CrimsonBearContainer)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val symbolVal = recSymbol.trim()
                        val entryVal = recEntry.toDoubleOrNull() ?: 0.0
                        val slVal = recSl.toDoubleOrNull() ?: 0.0
                        val tpVal = recTp.toDoubleOrNull() ?: 0.0
                        val lotsVal = recLots.toDoubleOrNull() ?: 0.0
                        val pnlVal = recProfitLoss.trim().takeIf { it.isNotEmpty() && it != "-" && it != "+" }?.toDoubleOrNull()

                        if (symbolVal.isBlank() || entryVal <= 0.0 || slVal <= 0.0 || tpVal <= 0.0 || lotsVal <= 0.0) {
                            formValidationError = "Enter a Symbol and positive values for Entry Price, Stop Loss, Take Profit, and Lot Size."
                            return@Button
                        }

                        onRecordTrade(
                            symbolVal,
                            recTimeframe,
                            recDirection,
                            entryVal,
                            slVal,
                            tpVal,
                            lotsVal,
                            pnlVal,
                            recStatus,
                            recNotes
                        )
                        recProfitLoss = ""
                        recNotes = ""
                        formValidationError = null
                        showAddTradeForm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BullionGold,
                        contentColor = OnBullionGold
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("save_recorded_trade_button")
                ) {
                    Text("Save Trade")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        formValidationError = null
                        showAddTradeForm = false
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    editingNotesSetup?.let { target ->
        AlertDialog(
            onDismissRequest = { editingNotesSetup = null },
            containerColor = ObsidianSurface,
            title = { Text("Edit Notes · ${target.pair}") },
            text = {
                OutlinedTextField(
                    value = draftNotes,
                    onValueChange = { draftNotes = it },
                    label = { Text("Trade Execution & Review Notes") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("edit_journal_notes_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateNotes(target.id, draftNotes)
                        editingNotesSetup = null
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("save_journal_notes_button")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingNotesSetup = null },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Journal Summary Strip
            item {
                val activeCount = allSetups.count { it.status == "ACTIVE" }
                val wonCount = allSetups.count { it.status == "WON" }
                val closedCount = allSetups.count { it.status == "WON" || it.status == "LOST" }
                val winRateText = if (closedCount > 0) "${(wonCount * 100) / closedCount}%" else "--"
                val pnlEntries = allSetups.mapNotNull { it.profitLossUsd }
                val netPnlText = if (pnlEntries.isNotEmpty()) {
                    val sum = pnlEntries.sum()
                    if (sum >= 0) "+$${String.format(Locale.US, "%.2f", sum)}"
                    else "-$${String.format(Locale.US, "%.2f", kotlin.math.abs(sum))}"
                } else "--"
                val netPnlColor = when {
                    pnlEntries.isEmpty() -> SlateTextPrimary
                    pnlEntries.sum() >= 0 -> EmeraldBull
                    else -> CrimsonBear
                }
                val avgRr = if (allSetups.isNotEmpty()) {
                    "1:${String.format(Locale.US, "%.1f", allSetups.map { it.riskRewardRatio }.average())}"
                } else "--"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    JournalStatTile("TRADES", "${allSetups.size}", SlateTextPrimary)
                    JournalStatTile("ACTIVE", "$activeCount", BullionGold)
                    JournalStatTile("NET P&L", netPnlText, netPnlColor)
                    JournalStatTile("AVG R:R", avgRr, BullionGold)
                    JournalStatTile("WIN %", winRateText, SlateTextPrimary)
                }
            }

            // 1b. Add Trade CTA
            item {
                Button(
                    onClick = {
                        formValidationError = null
                        showAddTradeForm = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BullionGold,
                        contentColor = OnBullionGold
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("journal_add_trade_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Trade", style = MaterialTheme.typography.titleMedium)
                }
            }

            // 2. Journal History Header & Status Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "JOURNAL HISTORY",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateTextSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JournalFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = currentFilter == filter,
                                onClick = { onFilterChange(filter) },
                                label = { Text(filter.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BullionGoldContainer,
                                    selectedLabelColor = BullionGold
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("journal_filter_${filter.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            if (setups.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BookmarkBorder,
                            contentDescription = null,
                            tint = BullionGold,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No trades in this view",
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = "Tap Add Trade above to log a trade directly, or save a plan from the Planner tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTextSecondary
                        )
                    }
                }
            } else {
                items(setups, key = { it.id }) { setup ->
                    JournalSetupCard(
                        setup = setup,
                        onUpdateStatus = { nextStatus -> onUpdateStatus(setup.id, nextStatus) },
                        onEditNotes = {
                            draftNotes = setup.notes
                            editingNotesSetup = setup
                        },
                        onLoadIntoPlanner = { onLoadIntoPlanner(setup) },
                        onDelete = { onDeleteSetup(setup.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun JournalStatTile(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
        Text(text = value, style = MonoPriceMedium, color = color)
    }
}

@Composable
private fun JournalSetupCard(
    setup: TradeSetupEntity,
    onUpdateStatus: (String) -> Unit,
    onEditNotes: () -> Unit,
    onLoadIntoPlanner: () -> Unit,
    onDelete: () -> Unit
) {
    val isBuy = setup.direction == "LONG" || setup.direction == "BUY"
    val dirLabel = if (isBuy) "BUY" else "SELL"
    val dirColor = if (isBuy) EmeraldBull else CrimsonBear
    val dirBg = if (isBuy) EmeraldBullContainer else CrimsonBearContainer
    val decimals = PositionCalculator.inferContractSpec(setup.pair).priceDecimals
    val statuses = listOf("PLANNED", "ACTIVE", "WON", "LOST")

    val pnlValue = setup.profitLossUsd
    val pnlText = if (pnlValue != null) {
        if (pnlValue >= 0) "+$${String.format(Locale.US, "%.2f", pnlValue)}"
        else "-$${String.format(Locale.US, "%.2f", kotlin.math.abs(pnlValue))}"
    } else "Open / Planned"
    val pnlColor = when {
        pnlValue == null -> SlateTextSecondary
        pnlValue >= 0 -> EmeraldBull
        else -> CrimsonBear
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${setup.pair} · ${setup.timeframe}",
                    style = MaterialTheme.typography.titleMedium,
                    color = SlateTextPrimary
                )
                Text(
                    text = dirLabel,
                    style = MonoLabelSmall,
                    color = dirColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(dirBg)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
            Text(
                text = "1:${String.format(Locale.US, "%.2f", setup.riskRewardRatio)} · ${String.format(Locale.US, "%.2f", setup.lotSize)} lots",
                style = MonoLabelSmall,
                color = BullionGold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("ENTRY", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                Text(String.format(Locale.US, "%.${decimals}f", setup.entryPrice), style = MonoPriceMedium, color = SlateTextPrimary)
            }
            Column {
                Text("STOP LOSS", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                Text(String.format(Locale.US, "%.${decimals}f", setup.stopLoss), style = MonoPriceMedium, color = CrimsonBear)
            }
            Column {
                Text("TAKE PROFIT", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
                Text(String.format(Locale.US, "%.${decimals}f", setup.takeProfit), style = MonoPriceMedium, color = EmeraldBull)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianSurfaceElevated.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PROFIT / LOSS", style = MaterialTheme.typography.labelSmall, color = SlateTextMuted)
            Text(pnlText, style = MonoPriceMedium, color = pnlColor)
        }

        if (setup.notes.isNotBlank()) {
            Text(
                text = setup.notes,
                style = MaterialTheme.typography.bodySmall,
                color = SlateTextSecondary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statuses.forEach { status ->
                val active = setup.status == status
                val statusColor = when (status) {
                    "WON" -> EmeraldBull
                    "LOST" -> CrimsonBear
                    "ACTIVE" -> BullionGold
                    else -> SlateTextSecondary
                }
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) statusColor.copy(alpha = 0.2f) else ObsidianSurfaceElevated)
                        .border(
                            1.dp,
                            if (active) statusColor else ObsidianBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onUpdateStatus(status) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("status_${setup.id}_${status.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status,
                        style = MonoLabelSmall,
                        color = if (active) statusColor else SlateTextMuted
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onLoadIntoPlanner,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("journal_load_${setup.id}")
            ) {
                Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Load in Planner")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEditNotes,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("journal_edit_notes_${setup.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = "Edit setup notes",
                        tint = BullionGold
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("journal_delete_${setup.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete trade setup",
                        tint = SlateTextMuted
                    )
                }
            }
        }
    }
}

private fun filterDecimalInput(raw: String): String {
    val normalized = raw.replace(',', '.')
    return normalized.filterIndexed { idx, c -> c.isDigit() || (c == '.' && normalized.indexOf('.') == idx) }
}

private fun filterSignedDecimalInput(raw: String): String {
    val normalized = raw.replace(',', '.')
    return normalized.filterIndexed { idx, c ->
        c.isDigit() ||
            ((c == '-' || c == '+') && idx == 0) ||
            (c == '.' && normalized.indexOf('.') == idx)
    }
}
