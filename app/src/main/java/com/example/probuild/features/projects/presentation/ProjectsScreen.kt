package com.example.probuild.features.projects.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.probuild.R
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.projects.domain.model.ProjectStatus
import com.example.probuild.features.projects.presentation.components.ProjectCard
import com.example.probuild.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onProjectClick: (String) -> Unit,
    onAddProjectClick: () -> Unit,
    viewModel: ProjectsViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Management", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { isSortMenuExpanded = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    SortMenu(
                        expanded = isSortMenuExpanded,
                        onDismiss = { isSortMenuExpanded = false },
                        currentOrder = state.sortOrder,
                        onOrderSelected = {
                            viewModel.onSortOrderChanged(it)
                            isSortMenuExpanded = false
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddProjectClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Project")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.padding(horizontal = Dimens.PaddingMedium)
            )

            FilterSection(
                selectedFilters = state.filterStatus,
                onFilterToggle = { viewModel.toggleFilterStatus(it) }
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.projects.isEmpty()) {
                EmptyState(
                    query = state.searchQuery,
                    onClearFilters = {
                        viewModel.onSearchQueryChanged("")
                        // Clear filter statuses if needed
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Avoid FAB overlap
                ) {
                    items(state.projects, key = { it.id }) { project ->
                        ProjectCard(
                            project = project,
                            onClick = { onProjectClick(project.id) },
                            onFavoriteToggle = { viewModel.toggleFavorite(project.id) },
                            modifier = Modifier.padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search projects...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun FilterSection(
    selectedFilters: Set<ProjectStatus>,
    onFilterToggle: (ProjectStatus) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall),
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
    ) {
        items(ProjectStatus.entries.toTypedArray()) { status ->
            FilterChip(
                selected = selectedFilters.contains(status),
                onClick = { onFilterToggle(status) },
                label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                leadingIcon = if (selectedFilters.contains(status)) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun SortMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentOrder: SortOrder,
    onOrderSelected: (SortOrder) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        SortOrder.entries.forEach { order ->
            DropdownMenuItem(
                text = { Text(order.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                onClick = { onOrderSelected(order) },
                trailingIcon = if (currentOrder == order) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
}

@Composable
private fun EmptyState(
    query: String,
    onClearFilters: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (query.isEmpty()) Icons.Default.Inventory else Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
            Text(
                text = if (query.isEmpty()) "No projects yet" else "No matching projects",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (query.isEmpty()) "Create your first project to get started." else "Try adjusting your search or filters.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                TextButton(onClick = onClearFilters) {
                    Text("Clear All Filters")
                }
            }
        }
    }
}
