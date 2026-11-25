package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Podcasts
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.podcast.PodcastUiState
import com.example.podcast.PodcastViewModel
import com.example.ui.components.GroundingSanctuarySheet
import com.example.ui.components.UnlockJourneyDialog
import com.example.ui.screens.JourneyScreen
import com.example.ui.screens.PodcastEpisodePlayerView
import com.example.ui.screens.PodcastMiniPlayerBar
import com.example.ui.screens.PodcastsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.LocalSacredColors
import com.example.util.SacredCalendar

object SacredRoutes {
    const val TODAY = "today"
    const val PODCASTS = "podcasts"
    const val JOURNEY = "journey"
    const val SETTINGS = "settings"
}

private data class SacredNavTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val navTabs = listOf(
    SacredNavTab(
        route = SacredRoutes.TODAY,
        labelRes = R.string.nav_today,
        selectedIcon = Icons.Rounded.WbSunny,
        unselectedIcon = Icons.Outlined.WbSunny,
        testTag = "nav_tab_today"
    ),
    SacredNavTab(
        route = SacredRoutes.PODCASTS,
        labelRes = R.string.nav_podcasts,
        selectedIcon = Icons.Rounded.Podcasts,
        unselectedIcon = Icons.Outlined.Podcasts,
        testTag = "nav_tab_podcasts"
    ),
    SacredNavTab(
        route = SacredRoutes.JOURNEY,
        labelRes = R.string.nav_journey,
        selectedIcon = Icons.Rounded.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        testTag = "nav_tab_journey"
    ),
    SacredNavTab(
        route = SacredRoutes.SETTINGS,
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_tab_settings"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SacredStepsApp(
    uiState: SacredStepsUiState,
    viewModel: DailyAffirmationViewModel,
    podcastUiState: PodcastUiState,
    podcastViewModel: PodcastViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: SacredRoutes.TODAY
    val snackbarHostState = remember { SnackbarHostState() }
    val sacredColors = LocalSacredColors.current

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeSnackbar()
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.statusBars,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    title = {
                        val currentAff = uiState.currentAffirmation
                        if (currentRoute == SacredRoutes.TODAY && currentAff != null) {
                            val dateInfo = remember(currentAff.dayNumber) {
                                SacredCalendar.fromDayOfYear365(currentAff.dayNumber)
                            }
                            Column {
                                Text(
                                    text = "${dateInfo.weekdayFull}, ${dateInfo.monthName} ${dateInfo.dayOfMonth} · Week ${currentAff.weekNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = sacredColors.dawnGold
                                )
                                Text(
                                    text = "Day ${currentAff.dayNumber} of 365",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    },
                    actions = {
                        // 1-tap Grounding / SOS pill accessible from EVERY screen
                        Surface(
                            onClick = { viewModel.openGrounding() },
                            shape = MaterialTheme.shapes.large,
                            color = sacredColors.sageContainer,
                            border = BorderStroke(1.dp, sacredColors.sage.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .testTag("sos_breathe_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Spa,
                                    contentDescription = "Open grounding sanctuary",
                                    tint = sacredColors.sage,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.action_ground_yourself),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (!useRail) {
                    Column {
                        AnimatedVisibility(
                            visible = podcastUiState.selectedTrack != null && !podcastUiState.isFullPlayerExpanded
                        ) {
                            PodcastMiniPlayerBar(
                                uiState = podcastUiState,
                                onTogglePlayPause = { podcastViewModel.togglePlayPause() },
                                onExpandPlayer = { podcastViewModel.expandPlayer() },
                                onDismiss = { podcastViewModel.dismissPlayer() }
                            )
                        }
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            navTabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(tab.labelRes),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = sacredColors.dawnGoldContainer,
                                        unselectedIconColor = sacredColors.subtleText,
                                        unselectedTextColor = sacredColors.subtleText
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        navTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = { Text(stringResource(tab.labelRes)) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = SacredRoutes.TODAY,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(SacredRoutes.TODAY) {
                            TodayScreen(
                                affirmation = uiState.currentAffirmation,
                                userProgress = uiState.userProgress,
                                journalEntries = uiState.journalEntriesForCurrentDay,
                                isUnlockSubmitting = uiState.isUnlockSubmitting,
                                unlockErrorMessage = uiState.unlockErrorMessage,
                                onSelectDay = { day -> viewModel.selectDay(day) },
                                onSelectHeartMood = { mood -> viewModel.selectHeartMood(mood) },
                                onToggleWalked = { viewModel.toggleCurrentDayWalked() },
                                onSaveJournal = { day, text -> viewModel.saveJournalEntry(day, text) },
                                onDeleteJournal = { id -> viewModel.deleteJournalEntry(id) },
                                onSubmitUnlock = { email, firstName, espProvider ->
                                    viewModel.submitUnlockEmail(email, firstName, espProvider)
                                },
                                onOpenUnlock = { viewModel.openUnlockDialog() }
                            )
                        }
                        composable(SacredRoutes.PODCASTS) {
                            PodcastsScreen(
                                uiState = podcastUiState,
                                onSelectMonth = { monthNum -> podcastViewModel.selectMonth(monthNum) },
                                onOpenTrack = { track, autoPlay -> podcastViewModel.openTrack(track, autoPlay) },
                                onSwapLeadHost = { podcastViewModel.swapAlternatingLeadHost() }
                            )
                        }
                        composable(SacredRoutes.JOURNEY) {
                            JourneyScreen(
                                affirmations = uiState.affirmations,
                                userProgress = uiState.userProgress,
                                journaledDays = uiState.journaledDays,
                                monthlyJournals = uiState.monthlyJournals,
                                monthlyJournalResponses = uiState.monthlyJournalResponses,
                                onSelectDay = { day ->
                                    viewModel.selectDay(day) {
                                        navigateToTab(SacredRoutes.TODAY)
                                    }
                                },
                                onSaveMonthlyJournalResponse = { monthNum, entryKey, content ->
                                    viewModel.saveMonthlyJournalResponse(monthNum, entryKey, content)
                                },
                                onOpenUnlock = { viewModel.openUnlockDialog() }
                            )
                        }
                        composable(SacredRoutes.SETTINGS) {
                            SettingsScreen(
                                userProgress = uiState.userProgress,
                                journalEntryCount = uiState.allJournalEntries.size,
                                onOpenUnlock = { viewModel.openUnlockDialog() },
                                onUpdateNotifications = { enabled, hour, minute ->
                                    viewModel.updateNotificationSchedule(enabled, hour, minute)
                                },
                                onSetDarkModeOverride = { override ->
                                    viewModel.setDarkModeOverride(override)
                                },
                                onPrepareExport = { callback ->
                                    viewModel.prepareJournalExport(callback)
                                }
                            )
                        }
                    }

                    if (useRail) {
                        AnimatedVisibility(
                            visible = podcastUiState.selectedTrack != null && !podcastUiState.isFullPlayerExpanded
                        ) {
                            PodcastMiniPlayerBar(
                                uiState = podcastUiState,
                                onTogglePlayPause = { podcastViewModel.togglePlayPause() },
                                onExpandPlayer = { podcastViewModel.expandPlayer() },
                                onDismiss = { podcastViewModel.dismissPlayer() },
                                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                            )
                        }
                    }
                }
            }
        }

        // Full-Screen Expandable Podcast Player Overlay (accessible from any tab via MiniPlayerBar)
        val expandedTrack = podcastUiState.selectedTrack
        AnimatedVisibility(
            visible = expandedTrack != null && podcastUiState.isFullPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            if (expandedTrack != null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        PodcastEpisodePlayerView(
                            track = expandedTrack,
                            uiState = podcastUiState,
                            onMinimize = { podcastViewModel.minimizePlayer() },
                            onTogglePlayPause = { podcastViewModel.togglePlayPause() },
                            onSeek = { posMs -> podcastViewModel.seekTo(posMs) },
                            onSkipBack15 = { podcastViewModel.skipBack15() },
                            onSkipForward15 = { podcastViewModel.skipForward15() },
                            onSetSpeed = { speed -> podcastViewModel.setPlaybackSpeed(speed) },
                            onPreviousTurn = { podcastViewModel.playPreviousTurn() },
                            onNextTurn = { podcastViewModel.playNextTurn() },
                            onSelectTurn = { idx -> podcastViewModel.playTurn(idx) },
                            onSwapLeadHost = { podcastViewModel.swapAlternatingLeadHost() },
                            onPrepareOffline = { podcastViewModel.prepareFullTrackOffline() }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showGroundingSheet) {
        GroundingSanctuarySheet(
            onDismiss = { viewModel.dismissGrounding() }
        )
    }

    if (uiState.showUnlockDialog) {
        UnlockJourneyDialog(
            targetDay = uiState.pendingUnlockTargetDay,
            isSubmitting = uiState.isUnlockSubmitting,
            errorMessage = uiState.unlockErrorMessage,
            onSubmit = { email, firstName, espProvider ->
                viewModel.submitUnlockEmail(email, firstName, espProvider)
            },
            onDismiss = { viewModel.dismissUnlockDialog() }
        )
    }
}
