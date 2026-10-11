package com.example.probuild.features.dashboard.domain

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.probuild.navigation.Screen

data class DashboardAction(
    val titleRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val screen: Screen
)
