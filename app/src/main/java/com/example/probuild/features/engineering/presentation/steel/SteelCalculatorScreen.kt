package com.example.probuild.features.engineering.presentation.steel

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
import com.example.probuild.ui.components.CalculationResultCard
import com.example.probuild.ui.components.NumericInputField
import com.example.probuild.ui.components.UnitSelector
import com.example.probuild.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteelCalculatorScreen(
    calculationId: String? = null,
    onBackClick: () -> Unit,
    onNavigateToProjectCreation: () -> Unit,
    viewModel: SteelCalculatorViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
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
                        Text("Steel Weight", style = MaterialTheme.typography.titleMedium)
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
                NumericInputField(
                    value = uiState.barDiameter,
                    onValueChange = viewModel::onDiameterChange,
                    label = "Bar Diameter",
                    error = uiState.diameterError,
                    suffix = {
                        UnitSelector(
                            selectedUnit = uiState.diameterUnit,
                            onUnitSelected = viewModel::onDiameterUnitChange,
                            allowedUnits = DimensionUnit.diameterUnits
                        )
                    }
                )

                NumericInputField(
                    value = uiState.barLength,
                    onValueChange = viewModel::onLengthChange,
                    label = "Bar Length",
                    error = uiState.lengthError,
                    suffix = {
                        UnitSelector(
                            selectedUnit = uiState.lengthUnit,
                            onUnitSelected = viewModel::onLengthUnitChange,
                            allowedUnits = DimensionUnit.commonLengthUnits
                        )
                    }
                )

                NumericInputField(
                    value = uiState.numberOfBars,
                    onValueChange = viewModel::onCountChange,
                    label = "Number of Bars",
                    error = uiState.countError
                )

                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                if (uiState.resultWeightKg != null) {
                    CalculationResultCard(
                        title = "Estimated Steel Weight",
                        primaryResult = "%.2f kg".format(uiState.resultWeightKg),
                        secondaryResult = "Volume: %.4f m³".format(uiState.resultVolumeM3),
                        inputSummary = "${uiState.resultTotalBars ?: 0} bars of ${uiState.barDiameter}${uiState.diameterUnit.symbol} × ${uiState.barLength}${uiState.lengthUnit.symbol}"
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
            Text(
                text = "No Active Project",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "A project context is required to save calculations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
            Button(onClick = onAddProjectClick) {
                Text("Create Project")
            }
        }
    }
}
