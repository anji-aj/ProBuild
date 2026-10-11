package com.example.probuild.features.projects.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.projects.domain.model.*
import com.example.probuild.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertProjectScreen(
    projectId: String? = null,
    onBackClick: () -> Unit,
    viewModel: UpsertProjectViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
) {
    val projectName by viewModel.projectName.collectAsStateWithLifecycle()
    val clientName by viewModel.clientName.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val budget by viewModel.budget.collectAsStateWithLifecycle()
    val projectType by viewModel.projectType.collectAsStateWithLifecycle()
    val priority by viewModel.priority.collectAsStateWithLifecycle()
    val projectStage by viewModel.projectStage.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val startDate by viewModel.startDate.collectAsStateWithLifecycle()
    val expectedEndDate by viewModel.expectedEndDate.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    LaunchedEffect(viewModel.isSaved) {
        viewModel.isSaved.collectLatest { saved ->
            if (saved) onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (projectId == null) "New Project" else "Edit Project") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveProject() },
                        enabled = projectName.isNotBlank(),
                        contentPadding = PaddingValues(horizontal = Dimens.PaddingMedium)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
        ) {
            OutlinedTextField(
                value = projectName,
                onValueChange = { viewModel.onProjectNameChange(it) },
                label = { Text("Project Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = clientName,
                onValueChange = { viewModel.onClientNameChange(it) },
                label = { Text("Client Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = location,
                onValueChange = { viewModel.onLocationChange(it) },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)) {
                OutlinedTextField(
                    value = budget,
                    onValueChange = { viewModel.onBudgetChange(it) },
                    label = { Text("Budget") },
                    modifier = Modifier.weight(1f),
                    prefix = { Text("$ ") },
                    singleLine = true
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("Progress: ${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = progress,
                        onValueChange = { viewModel.onProgressChange(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Divider()

            Text("Project Categories", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            EnumDropdown(
                label = "Project Type",
                options = ProjectType.entries.toTypedArray(),
                selected = projectType,
                onSelected = { viewModel.onProjectTypeChange(it) }
            )

            EnumDropdown(
                label = "Priority",
                options = Priority.entries.toTypedArray(),
                selected = priority,
                onSelected = { viewModel.onPriorityChange(it) }
            )

            EnumDropdown(
                label = "Project Stage",
                options = ProjectStage.entries.toTypedArray(),
                selected = projectStage,
                onSelected = { viewModel.onProjectStageChange(it) }
            )

            EnumDropdown(
                label = "Status",
                options = ProjectStatus.entries.toTypedArray(),
                selected = status,
                onSelected = { viewModel.onStatusChange(it) }
            )

            Divider()

            Text("Dates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { viewModel.onStartDateChange(it) },
                    label = { Text("Start Date") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    placeholder = { Text("YYYY-MM-DD") }
                )
                OutlinedTextField(
                    value = expectedEndDate,
                    onValueChange = { viewModel.onEndDateChange(it) },
                    label = { Text("End Date") },
                    modifier = Modifier.weight(1f),
                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    placeholder = { Text("YYYY-MM-DD") }
                )
            }
            
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Enum<T>> EnumDropdown(
    label: String,
    options: Array<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.PaddingSmall), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
