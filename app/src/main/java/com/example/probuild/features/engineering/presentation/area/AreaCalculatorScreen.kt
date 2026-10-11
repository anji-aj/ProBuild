package com.example.probuild.features.engineering.presentation.area

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
import com.example.probuild.features.engineering.domain.logic.AreaCalculator
import com.example.probuild.ui.components.CalculationResultCard
import com.example.probuild.ui.components.NumericInputField
import com.example.probuild.ui.components.UnitSelector
import com.example.probuild.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaCalculatorScreen(
    calculationId: String? = null,
    onBackClick: () -> Unit,
    onNavigateToProjectCreation: () -> Unit,
    viewModel: AreaCalculatorViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
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
                        Text("Area Calculator", style = MaterialTheme.typography.titleMedium)
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
                
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AreaCalculator.Shape.entries.forEachIndexed { index, shape ->
                        SegmentedButton(
                            selected = uiState.selectedShape == shape,
                            onClick = { viewModel.onShapeSelected(shape) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AreaCalculator.Shape.entries.size)
                        ) {
                            Text(shape.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                // Dynamic Inputs
                val (label1, label2) = when (uiState.selectedShape) {
                    AreaCalculator.Shape.RECTANGLE -> "Length" to "Width"
                    AreaCalculator.Shape.SQUARE -> "Side" to null
                    AreaCalculator.Shape.TRIANGLE -> "Base" to "Height"
                    AreaCalculator.Shape.CIRCLE -> "Radius" to null
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

                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                if (uiState.resultM2 != null) {
                    CalculationResultCard(
                        title = "Estimated Surface Area",
                        primaryResult = "%.2f m²".format(uiState.resultM2),
                        secondaryResult = "%.2f ft²".format(uiState.resultFt2),
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

private fun getSummary(state: AreaCalculatorUiState): String {
    val s = state.selectedShape
    return when (s) {
        AreaCalculator.Shape.RECTANGLE, AreaCalculator.Shape.TRIANGLE -> 
            "${state.dim1}${state.dim1Unit.symbol} × ${state.dim2}${state.dim2Unit.symbol}"
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
