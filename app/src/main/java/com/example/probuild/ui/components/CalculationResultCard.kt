package com.example.probuild.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.probuild.ui.theme.Dimens

@Composable
fun CalculationResultCard(
    title: String,
    primaryResult: String,
    secondaryResult: String,
    inputSummary: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.PaddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(Dimens.PaddingSmall))
            
            Text(
                text = primaryResult,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (secondaryResult.isNotEmpty()) {
                Text(
                    text = secondaryResult,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (inputSummary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.PaddingMedium))

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimens.PaddingSmall),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = "Inputs: $inputSummary",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
