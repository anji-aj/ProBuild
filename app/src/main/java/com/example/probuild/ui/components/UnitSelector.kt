package com.example.probuild.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.probuild.features.engineering.domain.model.DimensionUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelector(
    selectedUnit: DimensionUnit,
    onUnitSelected: (DimensionUnit) -> Unit,
    modifier: Modifier = Modifier,
    allowedUnits: List<DimensionUnit> = DimensionUnit.entries,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedUnit.symbol,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                modifier = Modifier
                    .width(90.dp)
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                trailingIcon = { 
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.large,
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                allowedUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.symbol) },
                        onClick = {
                            onUnitSelected(unit)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}
