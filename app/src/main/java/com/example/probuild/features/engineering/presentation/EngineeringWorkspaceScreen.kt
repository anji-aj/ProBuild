package com.example.probuild.features.engineering.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.probuild.R
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationCategory
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.projects.domain.model.Project
import com.example.probuild.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineeringWorkspaceScreen(
    onNavigateToProjectCreation: () -> Unit,
    onCalculatorClick: (CalculationType, String?) -> Unit,
    viewModel: EngineeringWorkspaceViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Engineering Workspace", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.projects.isEmpty()) {
            NoProjectEmptyState(
                onAddProjectClick = onNavigateToProjectCreation,
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ProjectSelector(
                    projects = uiState.projects,
                    selectedProject = uiState.selectedProject,
                    onProjectSelected = { viewModel.onProjectSelected(it) },
                    modifier = Modifier.padding(horizontal = Dimens.PaddingMedium)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Dimens.PaddingLarge)
                ) {
                    // Recent Calculations Section
                    if (uiState.history.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Recent Calculations")
                        }
                        items(uiState.history.take(3)) { calculation ->
                            RecentCalculationItem(
                                calculation = calculation,
                                onDelete = { viewModel.deleteCalculation(calculation.id) },
                                onClick = { onCalculatorClick(calculation.type, calculation.id) },
                                modifier = Modifier.padding(horizontal = Dimens.PaddingMedium, vertical = 4.dp)
                            )
                        }
                    }

                    // Favorites Section
                    if (uiState.favorites.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Favorites")
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = Dimens.PaddingMedium),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)
                            ) {
                                items(uiState.favorites.toList()) { type ->
                                    CalculatorCard(
                                        type = type,
                                        isFavorite = true,
                                        onFavoriteToggle = { viewModel.toggleCalculatorFavorite(type) },
                                        onClick = { onCalculatorClick(type, null) },
                                        modifier = Modifier.width(160.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Categorized Sections
                    item {
                        CategorySection(
                            title = "Structural",
                            types = CalculationType.entries.filter { it.category == CalculationCategory.STRUCTURAL },
                            favorites = uiState.favorites,
                            onFavoriteToggle = { viewModel.toggleCalculatorFavorite(it) },
                            onCalculatorClick = { type -> onCalculatorClick(type, null) }
                        )
                    }

                    item {
                        CategorySection(
                            title = "Quantity",
                            types = CalculationType.entries.filter { it.category == CalculationCategory.QUANTITY },
                            favorites = uiState.favorites,
                            onFavoriteToggle = { viewModel.toggleCalculatorFavorite(it) },
                            onCalculatorClick = { type -> onCalculatorClick(type, null) }
                        )
                    }

                    item {
                        CategorySection(
                            title = "Utilities",
                            types = CalculationType.entries.filter { it.category == CalculationCategory.UTILITIES },
                            favorites = uiState.favorites,
                            onFavoriteToggle = { viewModel.toggleCalculatorFavorite(it) },
                            onCalculatorClick = { type -> onCalculatorClick(type, null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(Dimens.PaddingMedium)
    )
}

@Composable
private fun CategorySection(
    title: String,
    types: List<CalculationType>,
    favorites: Set<CalculationType>,
    onFavoriteToggle: (CalculationType) -> Unit,
    onCalculatorClick: (CalculationType) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = Dimens.PaddingSmall)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall)
        )
        
        val chunks = types.chunked(2)
        chunks.forEach { rowTypes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.PaddingMedium),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
            ) {
                rowTypes.forEach { type ->
                    CalculatorCard(
                        type = type,
                        isFavorite = favorites.contains(type),
                        onFavoriteToggle = { onFavoriteToggle(type) },
                        onClick = { onCalculatorClick(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowTypes.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
        }
    }
}

@Composable
private fun CalculatorCard(
    type: CalculationType,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.PaddingSmall),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingExtraSmall))
                Text(
                    text = type.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentCalculationItem(
    calculation: Calculation,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        ListItem(
            headlineContent = { Text(calculation.displayName, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text("${calculation.resultVal} ${calculation.resultUnit}", style = MaterialTheme.typography.bodySmall) },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectSelector(
    projects: List<Project>,
    selectedProject: Project?,
    onProjectSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(vertical = Dimens.PaddingSmall)) {
        Text(
            text = "Active Project Context",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedProject?.projectName ?: "Select Project",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                projects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.projectName) },
                        onClick = {
                            onProjectSelected(project.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoProjectEmptyState(
    onAddProjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Dimens.PaddingLarge)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
            Text(
                text = "No Projects Found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
            Text(
                text = "Every calculation must belong to a project. Please create a project to access the workspace tools.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
            Button(onClick = onAddProjectClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                Text("Create Your First Project")
            }
        }
    }
}
