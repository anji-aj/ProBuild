package com.example.probuild.features.dashboard.presentation

import com.example.probuild.navigation.Screen

sealed interface DashboardUiEvent {
    data class Navigate(val screen: Screen) : DashboardUiEvent
}
