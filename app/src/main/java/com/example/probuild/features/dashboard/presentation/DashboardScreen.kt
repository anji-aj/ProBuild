package com.example.probuild.features.dashboard.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.probuild.R
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.dashboard.domain.DashboardAction
import com.example.probuild.features.dashboard.presentation.components.*
import com.example.probuild.navigation.Screen
import com.example.probuild.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DashboardScreen(
    onNavigate: (Screen) -> Unit,
    scrollToTopTrigger: Int = 0,
    viewModel: DashboardViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Handle Home reselection to scroll to top
    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            listState.animateScrollToItem(0)
        }
    }

    // Handle One-Time Navigation Events
    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is DashboardUiEvent.Navigate -> onNavigate(event.screen)
            }
        }
    }

    AnimatedContent(
        targetState = uiState,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "DashboardTransition"
    ) { state ->
        when (state) {
            is DashboardUiState.Loading -> LoadingView()
            is DashboardUiState.Success -> DashboardContent(
                state = state,
                listState = listState,
                onNavigate = onNavigate,
                onActionClick = { viewModel.onActionClick(it) },
                onProjectClick = { viewModel.onProjectClick(it) },
                onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                onNotificationClick = { viewModel.onNotificationClick() },
                onProfileClick = { viewModel.onProfileClick() }
            )
            is DashboardUiState.Empty -> EmptyView(
                onCreateProjectClick = { onNavigate(Screen.Projects) }
            )
            is DashboardUiState.Error -> ErrorView(
                messageRes = state.messageRes,
                onRetry = { viewModel.loadDashboard() }
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onNavigate: (Screen) -> Unit,
    onActionClick: (DashboardAction) -> Unit,
    onProjectClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        topBar = {
            DashboardTopBar(
                greetingRes = state.greetingRes,
                notificationCount = state.unreadNotificationCount,
                searchQuery = state.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = Dimens.PaddingLarge)
        ) {
            // Project Overview Stats Card
            item {
                ProjectOverviewSummaryCard(
                    totalCount = state.allProjectsCount,
                    activeCount = state.activeProjectsCount,
                    completedCount = state.completedProjectsCount,
                    onClick = { onNavigate(Screen.Projects) }
                )
            }

            if (state.actions.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.section_quick_actions),
                        modifier = Modifier.padding(Dimens.PaddingMedium)
                    )
                }

                // Quick Actions Grid (2 columns)
                item {
                    val chunks = remember(state.actions) { state.actions.chunked(2) }
                    Column(
                        modifier = Modifier
                            .padding(horizontal = Dimens.PaddingMedium)
                            .fillMaxWidth()
                    ) {
                        chunks.forEach { rowActions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
                            ) {
                                rowActions.forEach { action ->
                                    QuickActionCard(
                                        action = action,
                                        onClick = { onActionClick(action) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowActions.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                        }
                    }
                }

                // Site Diary Summary
                item {
                    SectionHeader(
                        title = "Site Management",
                        modifier = Modifier.padding(Dimens.PaddingMedium)
                    )
                    DiarySummaryCard(
                        summary = state.diarySummary,
                        onClick = { onNavigate(Screen.SiteDiary) },
                        modifier = Modifier.padding(horizontal = Dimens.PaddingMedium)
                    )
                }
            }

            if (state.recentProjects.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = stringResource(R.string.section_recent_projects),
                        modifier = Modifier.padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)
                    )
                }

                items(state.recentProjects, key = { it.id }) { project ->
                    RecentProjectCard(
                        project = project,
                        onClick = { onProjectClick(project.id) },
                        modifier = Modifier.padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)
                    )
                }
            } else if (state.searchQuery.isEmpty()) {
                // Show Empty Projects placeholder only if not searching
                item {
                    SectionHeader(
                        title = stringResource(R.string.section_recent_projects),
                        modifier = Modifier.padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)
                    )
                }
                item {
                    EmptyProjectsPlaceholder(
                        onCreateProjectClick = { onActionClick(state.actions.first { it.titleRes == R.string.action_projects }) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyProjectsPlaceholder(
    onCreateProjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.PaddingMedium),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.ElevationLow)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.PaddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.empty_projects_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            Text(
                text = stringResource(R.string.empty_projects_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
            Button(onClick = onCreateProjectClick) {
                Text(text = stringResource(R.string.action_create_project))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
private fun ProjectOverviewSummaryCard(
    totalCount: Int,
    activeCount: Int,
    completedCount: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.PaddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Project Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClick) {
                    Text("View All")
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DashboardStatBadge(label = "Total", count = totalCount, color = MaterialTheme.colorScheme.primary)
                DashboardStatBadge(label = "Active", count = activeCount, color = MaterialTheme.colorScheme.secondary)
                DashboardStatBadge(label = "Completed", count = completedCount, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun DashboardStatBadge(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
