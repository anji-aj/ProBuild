package com.example.probuild.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.probuild.features.projects.domain.model.ProjectStatus
import com.example.probuild.ui.theme.DeepSage
import com.example.probuild.ui.theme.ErrorRed
import com.example.probuild.ui.theme.GraphiteGray
import com.example.probuild.ui.theme.MutedGray
import com.example.probuild.ui.theme.SageContainer
import com.example.probuild.ui.theme.SoftGray
import com.example.probuild.ui.theme.WarmStoneContainer

@Composable
fun StatusChip(status: ProjectStatus) {
    val containerColor = when (status) {
        ProjectStatus.PLANNING -> WarmStoneContainer
        ProjectStatus.ACTIVE -> SageContainer
        ProjectStatus.COMPLETED -> SoftGray.copy(alpha = 0.5f)
        ProjectStatus.ON_HOLD -> Color(0xFFF7EFE5)
        ProjectStatus.CANCELLED -> Color(0xFFFDE8E8)
    }
    
    val contentColor = when (status) {
        ProjectStatus.PLANNING -> GraphiteGray
        ProjectStatus.ACTIVE -> DeepSage
        ProjectStatus.COMPLETED -> MutedGray
        ProjectStatus.ON_HOLD -> Color(0xFF8D6E63)
        ProjectStatus.CANCELLED -> ErrorRed
    }
    
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(0.5.dp, SoftGray)
    ) {
        Text(
            text = status.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
