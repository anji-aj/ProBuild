package com.example.probuild.features.engineering.presentation.brick

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
fun BrickCalculatorScreen(
    calculationId: String? = null,
    onBackClick: () -> Unit,
    onNavigateToProjectCreation: () -> Unit,
    viewModel: BrickCalculatorViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
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
                        Text("Brick Counter", style = MaterialTheme.typography.titleMedium)
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
                // Wall Dimensions Section
                SectionHeader("Wall Dimensions")
                NumericInputField(
                    value = uiState.wallLength,
                    onValueChange = viewModel::onWallLengthChange,
                    label = "Wall Length",
                    error = uiState.wallLengthError,
                    suffix = { UnitSelector(uiState.wallLengthUnit, viewModel::onWallLengthUnitChange, allowedUnits = DimensionUnit.commonLengthUnits) }
                )
                NumericInputField(
                    value = uiState.wallHeight,
                    onValueChange = viewModel::onWallHeightChange,
                    label = "Wall Height",
                    error = uiState.wallHeightError,
                    suffix = { UnitSelector(uiState.wallHeightUnit, viewModel::onWallHeightUnitChange, allowedUnits = DimensionUnit.commonLengthUnits) }
                )
                NumericInputField(
                    value = uiState.wallThickness,
                    onValueChange = viewModel::onWallThicknessChange,
                    label = "Wall Thickness",
                    error = uiState.wallThicknessError,
                    suffix = { UnitSelector(uiState.wallThicknessUnit, viewModel::onWallThicknessUnitChange, allowedUnits = DimensionUnit.commonLengthUnits) }
                )

                // Brick Dimensions Section
                SectionHeader("Brick Dimensions")
                NumericInputField(
                    value = uiState.brickLength,
                    onValueChange = viewModel::onBrickLengthChange,
                    label = "Brick Length",
                    error = uiState.brickLengthError,
                    suffix = { UnitSelector(uiState.brickLengthUnit, viewModel::onBrickLengthUnitChange, allowedUnits = DimensionUnit.diameterUnits) }
                )
                NumericInputField(
                    value = uiState.brickWidth,
                    onValueChange = viewModel::onBrickWidthChange,
                    label = "Brick Width",
                    error = uiState.brickWidthError,
                    suffix = { UnitSelector(uiState.brickWidthUnit, viewModel::onBrickWidthUnitChange, allowedUnits = DimensionUnit.diameterUnits) }
                )
                NumericInputField(
                    value = uiState.brickHeight,
                    onValueChange = viewModel::onBrickHeightChange,
                    label = "Brick Height",
                    error = uiState.brickHeightError,
                    suffix = { UnitSelector(uiState.brickHeightUnit, viewModel::onBrickHeightUnitChange, allowedUnits = DimensionUnit.diameterUnits) }
                )

                // Mortar & Wastage
                SectionHeader("Mortar & Wastage")
                NumericInputField(
                    value = uiState.mortarJoint,
                    onValueChange = viewModel::onMortarJointChange,
                    label = "Mortar Joint",
                    error = uiState.mortarJointError,
                    suffix = { UnitSelector(uiState.mortarJointUnit, viewModel::onMortarJointUnitChange, allowedUnits = DimensionUnit.diameterUnits) }
                )
                NumericInputField(
                    value = uiState.wastagePercentage,
                    onValueChange = viewModel::onWastageChange,
                    label = "Wastage Percentage",
                    error = uiState.wastageError,
                    suffix = { Text("%", modifier = Modifier.padding(end = 8.dp)) }
                )

                Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

                // Live Result
                if (uiState.totalBricks != null) {
                    CalculationResultCard(
                        title = "Estimated Total Bricks",
                        primaryResult = "${uiState.totalBricks} bricks",
                        secondaryResult = "Wall Volume: %.3f m³".format(uiState.wallVolume),
                        inputSummary = "Incl. ${uiState.wastagePercentage}% wastage and ${uiState.mortarJoint}${uiState.mortarJointUnit.symbol} joints"
                    )
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
