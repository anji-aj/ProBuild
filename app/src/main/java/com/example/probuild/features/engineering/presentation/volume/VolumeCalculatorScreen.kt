package com.example.probuild.features.engineering.presentation.volume

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
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
import com.example.probuild.features.engineering.domain.logic.VolumeCalculator
import com.example.probuild.ui.components.CalculationResultCard
import com.example.probuild.ui.components.NumericInputField
import com.example.probuild.ui.components.UnitSelector
import com.example.probuild.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeCalculatorScreen(
    calculationId: String? = null,
    onBackClick: () -> Unit,
    onNavigateToProjectCreation: () -> Unit,
    viewModel: VolumeCalculatorViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
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
                        Text("Volume Calculator", style = MaterialTheme.typography.titleMedium)
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
                // Shape Selector
                Text(
                    text = "Select Shape",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                // Using chunked to handle 4 shapes in a readable way if needed, 
                // but for now, a scrollable row or segmented button is fine.
                // Let's use SegmentedButtonRow as it's standard in this project.
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    VolumeCalculator.Shape.entries.forEachIndexed { index, shape ->
                        SegmentedButton(
                            selected = uiState.selectedShape == shape,
                            onClick = { viewModel.onShapeSelected(shape) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = VolumeCalculator.Shape.entries.size)
                        ) {
                            Text(
                                text = when(shape) {
                                    VolumeCalculator.Shape.RECTANGULAR_PRISM -> "Prism"
                                    else -> shape.name.lowercase().replaceFirstChar { it.uppercase() }
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                // Dynamic Inputs
                val (label1, label2, label3) = when (uiState.selectedShape) {
                    VolumeCalculator.Shape.RECTANGULAR_PRISM -> Triple("Length", "Width", "Height")
                    VolumeCalculator.Shape.CUBE -> Triple("Side", null, null)
                    VolumeCalculator.Shape.CYLINDER -> Triple("Radius", "Height", null)
                    VolumeCalculator.Shape.SPHERE -> Triple("Radius", null, null)
                }

                NumericInputField(
                    value = uiState.dim1,
                    onValueChange = viewModel::onDim1Change,
                    label = label1,
                    error = uiState.dim1Error,
                    suffix = {
                        UnitSelector(
                            selectedUnit = uiState.dim1Unit,
                            onUnitSelected = viewModel::onDim1UnitChange,
                            allowedUnits = DimensionUnit.commonLengthUnits
                        )
                    }
                )

                if (label2 != null) {
                    NumericInputField(
                        value = uiState.dim2,
                        onValueChange = viewModel::onDim2Change,
                        label = label2,
                        error = uiState.dim2Error,
                        suffix = {
                            UnitSelector(
                                selectedUnit = uiState.dim2Unit,
                                onUnitSelected = viewModel::onDim2UnitChange,
                                allowedUnits = DimensionUnit.commonLengthUnits
                            )
                        }
                    )
                }

                if (label3 != null) {
                    NumericInputField(
                        value = uiState.dim3,
                        onValueChange = viewModel::onDim3Change,
                        label = label3,
                        error = uiState.dim3Error,
                        suffix = {
                            UnitSelector(
                                selectedUnit = uiState.dim3Unit,
                                onUnitSelected = viewModel::onDim3UnitChange,
                                allowedUnits = DimensionUnit.commonLengthUnits
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                if (uiState.resultM3 != null) {
                    CalculationResultCard(
                        title = "Estimated Volume",
                        primaryResult = "%.3f m³".format(uiState.resultM3),
                        secondaryResult = "%.2f ft³".format(uiState.resultFt3),
                        inputSummary = getSummary(uiState)
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
                    Text("Calculate and Save")
                }
            }
        }
    }
}

private fun getSummary(state: VolumeCalculatorUiState): String {
    val s = state.selectedShape
    return when (s) {
        VolumeCalculator.Shape.RECTANGULAR_PRISM -> 
            "${state.dim1}${state.dim1Unit.symbol} × ${state.dim2}${state.dim2Unit.symbol} × ${state.dim3}${state.dim3Unit.symbol}"
        VolumeCalculator.Shape.CYLINDER -> 
            "r:${state.dim1}${state.dim1Unit.symbol}, h:${state.dim2}${state.dim2Unit.symbol}"
        else -> "${state.dim1}${state.dim1Unit.symbol}"
    }
}

@Composable
private fun NoProjectState(
    onAddProjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Dimens.PaddingLarge)
        ) {
            Text("No Active Project", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
            Button(onClick = onAddProjectClick) {
                Text("Create Project")
            }
        }
    }
}
