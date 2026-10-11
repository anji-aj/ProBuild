package com.example.probuild.features.engineering.presentation.unit_converter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.engineering.domain.logic.EngineeringUnitConverter
import com.example.probuild.ui.components.CalculationResultCard
import com.example.probuild.ui.components.NumericInputField
import com.example.probuild.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    calculationId: String? = null,
    onBackClick: () -> Unit,
    onNavigateToProjectCreation: () -> Unit,
    viewModel: UnitConverterViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(calculationId) {
        viewModel.loadCalculation(calculationId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Unit Converter", style = MaterialTheme.typography.titleMedium)
                        uiState.selectedProject?.let {
                            Text(
                                text = "Project: ${it.projectName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.selectedProject == null) {
            NoProjectState(
                onAddProjectClick = onNavigateToProjectCreation,
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
            ) {
                // Category Selector
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedCategory.ordinal,
                    edgePadding = 0.dp,
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    divider = {}
                ) {
                    EngineeringUnitConverter.Category.entries.forEach { category ->
                        Tab(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) },
                            text = { Text(category.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                // Input Value
                NumericInputField(
                    value = uiState.inputValue,
                    onValueChange = viewModel::onInputValueChange,
                    label = "Value to Convert",
                    error = uiState.error
                )

                // Unit Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)
                ) {
                    UnitDropdown(
                        label = "From",
                        selectedSymbol = uiState.fromUnitSymbol,
                        units = EngineeringUnitConverter().unitsByCategory[uiState.selectedCategory] ?: emptyList(),
                        onUnitSelected = viewModel::onFromUnitChange,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = viewModel::swapUnits,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap")
                    }

                    UnitDropdown(
                        label = "To",
                        selectedSymbol = uiState.toUnitSymbol,
                        units = EngineeringUnitConverter().unitsByCategory[uiState.selectedCategory] ?: emptyList(),
                        onUnitSelected = viewModel::onToUnitChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                // Live Result
                if (uiState.convertedResult != null) {
                    CalculationResultCard(
                        title = "Converted Result",
                        primaryResult = "%.4f %s".format(uiState.convertedResult, uiState.toUnitSymbol),
                        secondaryResult = "",
                        inputSummary = "${uiState.inputValue} ${uiState.fromUnitSymbol} → ${uiState.toUnitSymbol}"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = viewModel::saveCalculation,
                    modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget),
                    enabled = uiState.isCalculateEnabled,
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                    Text("Save to History")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    label: String,
    selectedSymbol: String,
    units: List<EngineeringUnitConverter.Unit>,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedSymbol,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = MaterialTheme.shapes.large
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.symbol) },
                    onClick = {
                        onUnitSelected(unit.symbol)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
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
