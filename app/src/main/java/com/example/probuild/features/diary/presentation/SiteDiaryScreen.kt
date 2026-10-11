package com.example.probuild.features.diary.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.diary.domain.model.DiaryEntry
import com.example.probuild.features.diary.domain.model.DiarySummary
import com.example.probuild.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDiaryScreen(
    onNavigateToEntry: (String?) -> Unit,
    onNavigateToProjectCreation: () -> Unit,
    viewModel: DiaryViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSortMenu by remember { mutableStateOf(false) }
    var showDatePickerRange by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Site Diary", fontWeight = FontWeight.Bold)
                        uiState.selectedProject?.let {
                            Text(
                                text = "Project: ${it.projectName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        DateSortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.displayName) },
                                onClick = {
                                    viewModel.onSortOrderSelected(order)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (uiState.sortOrder == order) Icon(Icons.Default.Check, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.selectedProject != null) {
                FloatingActionButton(onClick = viewModel::openTodaysEntry) {
                    Icon(Icons.Default.Add, contentDescription = "Today's Entry")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.selectedProject == null) {
            NoProjectState(onAddProjectClick = onNavigateToProjectCreation, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Statistics Bar
                StatisticsBar(summary = uiState.summary)

                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)
                )

                // Date Filter Chips
                DateFilterRow(
                    selectedFilter = uiState.selectedDateFilter,
                    onFilterSelected = viewModel::onDateFilterSelected,
                    onCustomClick = { showDatePickerRange = true }
                )

                if (uiState.entries.isEmpty()) {
                    EmptyDiaryState(
                        onAddEntryClick = { onNavigateToEntry(null) },
                        isSearching = uiState.searchQuery.isNotBlank() || uiState.selectedDateFilter != DateFilter.ALL
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                    ) {
                        items(uiState.entries) { entry ->
                            DiaryEntryItem(
                                entry = entry,
                                onClick = { onNavigateToEntry(entry.id) },
                                onDuplicate = { viewModel.duplicateEntry(entry.id) },
                                modifier = Modifier.padding(horizontal = Dimens.PaddingMedium, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePickerRange) {
        val state = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerRange = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onCustomDateRangeSelected(state.selectedStartDateMillis, state.selectedEndDateMillis)
                    showDatePickerRange = false
                }) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerRange = false }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(state = state, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatisticsBar(summary: DiarySummary) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(Dimens.PaddingMedium)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(label = "Total", value = "${summary.totalEntryCount}")
            StatItem(label = "This Week", value = "${summary.entriesThisWeek}")
            StatItem(label = "Labour", value = "${summary.totalLabourRecorded}")
            StatItem(label = "Days", value = "${summary.daysWithEntries}")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DateFilterRow(
    selectedFilter: DateFilter,
    onFilterSelected: (DateFilter) -> Unit,
    onCustomClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.PaddingMedium)
            .padding(bottom = Dimens.PaddingSmall),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)
    ) {
        DateFilter.entries.take(5).forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName) }
            )
        }
        FilterChip(
            selected = selectedFilter == DateFilter.CUSTOM,
            onClick = onCustomClick,
            label = { Text("Custom") },
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search work, materials, issues...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun DiaryEntryItem(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(Dimens.PaddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(entry.date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(16.dp))
                    }
                    if (entry.issues.isNotBlank()) {
                        Spacer(modifier = Modifier.width(Dimens.SpacingSmall))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Issues reported",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            
            Text(
                text = entry.workCompleted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingLarge)
            ) {
                InfoLabel(icon = Icons.Default.People, text = "${entry.labourCount}")
                if (entry.weather.isNotBlank()) {
                    InfoLabel(icon = Icons.Default.Cloud, text = entry.weather)
                }
            }
        }
    }
}

@Composable
private fun InfoLabel(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyDiaryState(
    onAddEntryClick: () -> Unit,
    isSearching: Boolean
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Dimens.PaddingLarge)
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.EditNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
            Text(
                text = if (isSearching) "No matching entries" else "No site diary entries yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isSearching) "Try a different search term." else "Record today's site activity to keep your project history organized.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (!isSearching) {
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                Button(onClick = onAddEntryClick) {
                    Text("Add Diary Entry")
                }
            }
        }
    }
}

@Composable
private fun NoProjectState(
    onAddProjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No Active Project", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
            Button(onClick = onAddProjectClick) {
                Text("Create Project")
            }
        }
    }
}
