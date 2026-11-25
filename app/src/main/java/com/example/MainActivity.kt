package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppRoute
import com.example.ui.ChartPulseViewModel
import com.example.ui.screens.AnalystScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.theme.BullionGold
import com.example.ui.theme.BullionGoldContainer
import com.example.ui.theme.ChartPulseTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary

private data class TabMeta(
    val route: AppRoute,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val topTestTag: String
)

private val AppTabs = listOf(
    TabMeta(
        route = AppRoute.ANALYSIS,
        labelRes = R.string.nav_analyst,
        selectedIcon = Icons.Rounded.Analytics,
        unselectedIcon = Icons.Outlined.Analytics,
        testTag = "nav_analyst",
        topTestTag = "top_nav_analyst"
    ),
    TabMeta(
        route = AppRoute.PLANNER,
        labelRes = R.string.nav_planner,
        selectedIcon = Icons.Rounded.Calculate,
        unselectedIcon = Icons.Outlined.Calculate,
        testTag = "nav_planner",
        topTestTag = "top_nav_planner"
    ),
    TabMeta(
        route = AppRoute.JOURNAL,
        labelRes = R.string.nav_journal,
        selectedIcon = Icons.Rounded.Book,
        unselectedIcon = Icons.Outlined.Book,
        testTag = "nav_journal",
        topTestTag = "top_nav_journal"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(ObsidianBg.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(ObsidianSurface.toArgb())
        )
        setContent {
            ChartPulseTheme(darkTheme = true) {
                ChartPulseApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartPulseApp() {
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
    val viewModel: ChartPulseViewModel = viewModel(factory = ChartPulseViewModel.Factory(application))

    val currentTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val currentAnalysis by viewModel.currentAnalysis.collectAsStateWithLifecycle()
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val plannerState by viewModel.plannerState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val journalSetups by viewModel.journalSetups.collectAsStateWithLifecycle()
    val allSetups by viewModel.allSetupsCount.collectAsStateWithLifecycle()
    val journalFilter by viewModel.journalFilter.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(plannerState.savedSnackbarMessage) {
        val msg = plannerState.savedSnackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(ObsidianBg)) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBg,
            contentWindowInsets = WindowInsets.systemBars,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().background(ObsidianBg)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = SlateTextPrimary
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = ObsidianBg
                        )
                    )
                    if (!useNavRail) {
                        TopNavigationSwitcher(
                            currentTab = currentTab,
                            onSelectTab = viewModel::selectTab
                        )
                    }
                    HorizontalDivider(color = ObsidianBorder, thickness = 1.dp)
                }
            },
            bottomBar = {
                if (!useNavRail) {
                    Column {
                        HorizontalDivider(color = ObsidianBorder, thickness = 1.dp)
                        NavigationBar(
                            containerColor = ObsidianSurface,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            AppTabs.forEach { item ->
                                val selected = currentTab == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(item.route) },
                                    alwaysShowLabel = true,
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = stringResource(item.labelRes)
                                        )
                                    },
                                    label = { Text(stringResource(item.labelRes)) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BullionGold,
                                        selectedTextColor = BullionGold,
                                        indicatorColor = BullionGoldContainer,
                                        unselectedIconColor = SlateTextMuted,
                                        unselectedTextColor = SlateTextMuted
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = ObsidianSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        AppTabs.forEach { item ->
                            val selected = currentTab == item.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(item.route) },
                                alwaysShowLabel = true,
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(item.labelRes)
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = BullionGold,
                                    selectedTextColor = BullionGold,
                                    indicatorColor = BullionGoldContainer,
                                    unselectedIconColor = SlateTextMuted,
                                    unselectedTextColor = SlateTextMuted
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (currentTab) {
                        AppRoute.ANALYSIS -> {
                            AnalystScreen(
                                analysis = currentAnalysis,
                                analysisState = analysisState,
                                presets = viewModel.presets,
                                plannerState = plannerState,
                                messages = messages,
                                isChatLoading = isChatLoading,
                                onSelectPreset = viewModel::selectPreset,
                                onAnalyzeScreenshot = viewModel::analyzeChartScreenshot,
                                onLoadScenarioToPlanner = { scenario ->
                                    viewModel.applyScenarioToPlanner(scenario)
                                    viewModel.selectTab(AppRoute.PLANNER)
                                },
                                onSnapPriceToPlanner = viewModel::snapPriceToPlanner,
                                onAskFollowUp = viewModel::askFollowUp
                            )
                        }
                        AppRoute.PLANNER -> {
                            BackHandler {
                                viewModel.selectTab(AppRoute.ANALYSIS)
                            }
                            PlannerScreen(
                                plannerState = plannerState,
                                currentAnalysis = currentAnalysis,
                                presets = viewModel.presets,
                                onDirectionChange = viewModel::updateDirection,
                                onPairChange = viewModel::updatePairInput,
                                onTimeframeChange = viewModel::updateTimeframeInput,
                                onSelectPreset = viewModel::selectPlannerPairPreset,
                                onContractSpecChange = viewModel::updateContractSpec,
                                onBalanceChange = viewModel::updateBalanceInput,
                                onRiskPercentChange = viewModel::updateRiskPercentInput,
                                onLotSizeChange = viewModel::updateLotSizeInput,
                                onEntryChange = viewModel::updateEntryInput,
                                onStopLossChange = viewModel::updateStopLossInput,
                                onTakeProfitChange = viewModel::updateTakeProfitInput,
                                onApplyRiskRewardTarget = viewModel::applyRiskRewardTarget,
                                onCalculate = viewModel::calculatePlannerPosition,
                                onNotesChange = viewModel::updateNotesInput,
                                onSpreadToggle = viewModel::toggleSpreadNormalized,
                                onCandleToggle = viewModel::toggleCandleConfirmed,
                                onApplyScenario = viewModel::applyScenarioToPlanner,
                                onSaveSetup = viewModel::savePlannerSetupToJournal
                            )
                        }
                        AppRoute.JOURNAL -> {
                            BackHandler {
                                viewModel.selectTab(AppRoute.ANALYSIS)
                            }
                            JournalScreen(
                                setups = journalSetups,
                                allSetups = allSetups,
                                currentFilter = journalFilter,
                                onFilterChange = viewModel::setJournalFilter,
                                onUpdateStatus = viewModel::updateSetupStatus,
                                onUpdateNotes = viewModel::updateSetupNotes,
                                onRecordTrade = viewModel::recordJournalTrade,
                                onLoadIntoPlanner = { setup ->
                                    viewModel.loadJournalSetupIntoPlanner(setup)
                                    viewModel.selectTab(AppRoute.PLANNER)
                                },
                                onDeleteSetup = viewModel::deleteSetup
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopNavigationSwitcher(
    currentTab: AppRoute,
    onSelectTab: (AppRoute) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AppTabs.forEach { item ->
            val selected = currentTab == item.route
            Surface(
                onClick = { onSelectTab(item.route) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected) BullionGoldContainer else ObsidianSurfaceElevated.copy(alpha = 0.45f),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) BullionGold.copy(alpha = 0.65f) else ObsidianBorder.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag(item.topTestTag)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = null,
                        tint = if (selected) BullionGold else SlateTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(item.labelRes),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        color = if (selected) BullionGold else SlateTextPrimary
                    )
                }
            }
        }
    }
}
