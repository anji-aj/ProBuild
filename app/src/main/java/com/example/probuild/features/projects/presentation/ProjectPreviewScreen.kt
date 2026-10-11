package com.example.probuild.features.projects.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.diary.domain.model.DiaryEntry
import com.example.probuild.features.engineering.domain.model.Calculation
import com.example.probuild.features.engineering.domain.model.CalculationType
import com.example.probuild.features.projects.domain.model.Project
import com.example.probuild.ui.components.StatusChip
import com.example.probuild.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectPreviewScreen(
    projectId: String,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
    onCalculatorClick: (CalculationType, String?) -> Unit = { _, _ -> },
    onDiaryEntryClick: (String?) -> Unit = {}
) {
    var project by remember { mutableStateOf<Project?>(null) }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var calculations by remember { mutableStateOf<List<Calculation>>(emptyList()) }
    var diaryEntries by remember { mutableStateOf<List<DiaryEntry>>(emptyList()) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(projectId) {
        ServiceLocator.projectsRepository.getProjects().collectLatest { projects ->
            project = projects.find { it.id == projectId }
        }
    }

    LaunchedEffect(projectId) {
        ServiceLocator.preferenceManager.selectedProjectId.collectLatest { id ->
            selectedProjectId = id
        }
    }

    LaunchedEffect(projectId) {
        ServiceLocator.engineeringRepository.getCalculationsByProject(projectId).collectLatest { list ->
            calculations = list
        }
    }

    LaunchedEffect(projectId) {
        ServiceLocator.siteDiaryRepository.observeEntriesByProject(projectId).collectLatest { list ->
            diaryEntries = list
        }
    }

    val isActiveProject = selectedProjectId == projectId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Project Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    project?.let { p ->
                        IconButton(onClick = { onEditClick(p.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Project")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Project",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        project?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Main Project Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.PaddingMedium, vertical = Dimens.PaddingSmall),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(Dimens.PaddingMedium)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = p.projectName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (p.clientName.isNotBlank()) {
                                    Text(
                                        text = "Client: ${p.clientName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            StatusChip(status = p.status)
                        }

                        if (p.location.isNotBlank()) {
                            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = p.location,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

                        // Progress Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Construction Progress",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(p.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { p.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                        )

                        Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

                        // Key Metadata Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)
                        ) {
                            InfoBadge(
                                label = "Stage",
                                value = p.projectStage.name.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.weight(1f)
                            )
                            InfoBadge(
                                label = "Type",
                                value = p.projectType.name.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.weight(1f)
                            )
                            InfoBadge(
                                label = "Priority",
                                value = p.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (p.budget.isNotBlank() || p.startDate.isNotBlank() || p.expectedEndDate.isNotBlank()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = Dimens.PaddingSmall),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (p.budget.isNotBlank()) {
                                    Column {
                                        Text("Budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$ ${p.budget}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (p.startDate.isNotBlank() || p.expectedEndDate.isNotBlank()) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Timeline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${p.startDate.ifEmpty { "Start" }} → ${p.expectedEndDate.ifEmpty { "End" }}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        if (p.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
                            Text(
                                text = p.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                        // Active Project Action
                        if (isActiveProject) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Currently Active Project Context",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        ServiceLocator.preferenceManager.saveSelectedProjectId(p.id)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Set as Active Project")
                            }
                        }
                    }
                }

                // Tabs for Calculations and Site Diary
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Calculations (${calculations.size})", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Site Diary (${diaryEntries.size})", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) }
                    )
                }

                when (selectedTabIndex) {
                    0 -> {
                        if (calculations.isEmpty()) {
                            EmptySectionPlaceholder(
                                message = "No calculations saved for this project yet.",
                                actionText = "Open Workspace",
                                onActionClick = { onCalculatorClick(CalculationType.CONCRETE, null) }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(Dimens.PaddingMedium),
                                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)
                            ) {
                                items(calculations, key = { it.id }) { calc ->
                                    Card(
                                        onClick = { onCalculatorClick(calc.type, calc.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        ListItem(
                                            leadingContent = {
                                                Icon(
                                                    imageVector = calc.type.icon,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            headlineContent = { Text(calc.displayName, fontWeight = FontWeight.Bold) },
                                            supportingContent = { Text(calc.type.title) },
                                            trailingContent = {
                                                Text(
                                                    text = "${calc.resultVal} ${calc.resultUnit}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        if (diaryEntries.isEmpty()) {
                            EmptySectionPlaceholder(
                                message = "No diary entries logged for this project yet.",
                                actionText = "Add Site Diary Entry",
                                onActionClick = { onDiaryEntryClick(null) }
                            )
                        } else {
                            val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(Dimens.PaddingMedium),
                                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)
                            ) {
                                items(diaryEntries, key = { it.id }) { entry ->
                                    Card(
                                        onClick = { onDiaryEntryClick(entry.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(Dimens.PaddingMedium)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = dateFormat.format(Date(entry.date)),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Surface(
                                                    shape = MaterialTheme.shapes.small,
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        text = "${entry.labourCount} Workers",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                            if (entry.workCompleted.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = entry.workCompleted,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        project?.let { p ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Delete Project?") },
                text = {
                    Text("Are you sure you want to delete '${p.projectName}'? All associated calculations, diary entries, and photos will be deleted.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            coroutineScope.launch {
                                ServiceLocator.projectsRepository.deleteProject(p.id)
                                onBackClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun InfoBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptySectionPlaceholder(
    message: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.PaddingLarge),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
            Button(onClick = onActionClick) {
                Text(actionText)
            }
        }
    }
}
