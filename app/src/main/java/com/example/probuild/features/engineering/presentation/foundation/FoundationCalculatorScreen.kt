package com.example.probuild.features.engineering.presentation.foundation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.engineering.domain.model.DimensionUnit
import com.example.probuild.features.engineering.domain.logic.FoundationCalculator
import com.example.probuild.ui.components.CalculationResultCard
import com.example.probuild.ui.components.NumericInputField
import com.example.probuild.ui.components.UnitSelector
import com.example.probuild.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundationCalculatorScreen(
    calculationId: String? = null,
    onBackClick: () -> Unit,
    onNavigateToProjectCreation: () -> Unit,
    viewModel: FoundationCalculatorViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
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
                        Text("Foundation Takeoff", style = MaterialTheme.typography.titleMedium)
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
                // Safety Estimation Notice
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.PaddingSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                        Text(
                            text = "Estimated quantities only. Not for structural design.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Foundation Type Selector
                Text(
                    text = "Foundation Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    FoundationCalculator.FoundationType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = uiState.selectedType == type,
                            onClick = { viewModel.onTypeSelected(type) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = FoundationCalculator.FoundationType.entries.size)
                        ) {
                            Text(type.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                // Foundation Dimensions Section
                SectionHeader("Dimensions")
                val isSquare = uiState.selectedType == FoundationCalculator.FoundationType.SQUARE
                NumericInputField(
                    value = uiState.length,
                    onValueChange = viewModel::onLengthChange,
                    label = if (isSquare) "Side Length" else "Length",
                    error = uiState.lengthError,
                    suffix = { UnitSelector(uiState.lengthUnit, viewModel::onLengthUnitChange, allowedUnits = DimensionUnit.commonLengthUnits) }
                )
                
                if (!isSquare) {
                    NumericInputField(
                        value = uiState.width,
                        onValueChange = viewModel::onWidthChange,
                        label = "Width",
                        error = uiState.widthError,
                        suffix = { UnitSelector(uiState.widthUnit, viewModel::onWidthUnitChange, allowedUnits = DimensionUnit.commonLengthUnits) }
                    )
                }

                NumericInputField(
                    value = uiState.thickness,
                    onValueChange = viewModel::onThicknessChange,
                    label = "Thickness",
                    error = uiState.thicknessError,
                    suffix = { UnitSelector(uiState.thicknessUnit, viewModel::onThicknessUnitChange, allowedUnits = DimensionUnit.commonLengthUnits) }
                )

                // Main Reinforcement Section
                SectionHeader("Main Reinforcement")
                NumericInputField(
                    value = uiState.mainDiameter,
                    onValueChange = viewModel::onMainDiameterChange,
                    label = "Bar Diameter",
                    error = uiState.mainDiameterError,
                    suffix = { UnitSelector(uiState.mainDiameterUnit, viewModel::onMainDiameterUnitChange, allowedUnits = DimensionUnit.diameterUnits) }
                )
                NumericInputField(
                    value = uiState.mainSpacing,
                    onValueChange = viewModel::onMainSpacingChange,
                    label = "Bar Spacing",
                    error = uiState.mainSpacingError,
                    suffix = { UnitSelector(uiState.mainSpacingUnit, viewModel::onMainSpacingUnitChange, allowedUnits = DimensionUnit.entries) }
                )

                // Distribution Reinforcement Section
                SectionHeader("Distribution Reinforcement")
                NumericInputField(
                    value = uiState.distDiameter,
                    onValueChange = viewModel::onDistDiameterChange,
                    label = "Bar Diameter",
                    error = uiState.distDiameterError,
                    suffix = { UnitSelector(uiState.distDiameterUnit, viewModel::onDistDiameterUnitChange, allowedUnits = DimensionUnit.diameterUnits) }
                )
                NumericInputField(
                    value = uiState.distSpacing,
                    onValueChange = viewModel::onDistSpacingChange,
                    label = "Bar Spacing",
                    error = uiState.distSpacingError,
                    suffix = { UnitSelector(uiState.distSpacingUnit, viewModel::onDistSpacingUnitChange, allowedUnits = DimensionUnit.entries) }
                )

                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                // Results
                if (uiState.totalSteelWeightKg != null) {
                    CalculationResultCard(
                        title = "Estimated Materials",
                        primaryResult = "%.2f kg Total Steel".format(uiState.totalSteelWeightKg),
                        secondaryResult = "Concrete: %.2f m³".format(uiState.concreteVolumeM3),
                        inputSummary = "${uiState.mainBarCount} main + ${uiState.distributionBarCount} distribution bars"
                    )
                    
                    // Detailed Breakdown
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(Dimens.PaddingSmall)) {
                            Text("Takeoff Breakdown", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            ResultRow("Main Steel Weight", "%.2f kg".format(uiState.mainSteelWeightKg))
                            ResultRow("Main Bar Count", "${uiState.mainBarCount} pcs")
                            ResultRow("Dist. Steel Weight", "%.2f kg".format(uiState.distributionSteelWeightKg))
                            ResultRow("Dist. Bar Count", "${uiState.distributionBarCount} pcs")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

                Button(
                    onClick = viewModel::saveCalculation,
                    modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget),
                    enabled = uiState.isCalculateEnabled,
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                    Text("Calculate and Save")
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = Dimens.PaddingSmall)
    )
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
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
