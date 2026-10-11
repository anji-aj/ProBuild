package com.example.probuild.navigation

import kotlinx.serialization.Serializable

/**
 * Root navigation graphs.
 */
sealed interface Graph {
    @Serializable
    data object Root : Graph

    @Serializable
    data object Auth : Graph
    
    @Serializable
    data object Main : Graph
    
    @Serializable
    data object Onboarding : Graph
}

/**
 * Screen destinations within the Main graph.
 */
sealed interface Screen {
    @Serializable
    data object Dashboard : Screen
    
    @Serializable
    data object Projects : Screen
    
    @Serializable
    data object EngineeringWorkspace : Screen
    
    @Serializable
    data object SiteDiary : Screen
    
    @Serializable
    data class DiaryEntry(val id: String? = null) : Screen
    
    @Serializable
    data class PhotoPreview(val photoId: String) : Screen
    
    @Serializable
    data object MaterialTracker : Screen
    
    @Serializable
    data object CostEstimator : Screen
    
    @Serializable
    data object Reports : Screen

    @Serializable
    data class DrawingStudio(val drawingId: String? = null) : Screen
    
    @Serializable
    data object Equipment : Screen
    
    @Serializable
    data object Attendance : Screen
    
    @Serializable
    data object AiAssistant : Screen
    
    @Serializable
    data object Maps : Screen
    
    @Serializable
    data object Weather : Screen
    
    @Serializable
    data object Settings : Screen
    
    @Serializable
    data object Profile : Screen

    @Serializable
    data object Notifications : Screen

    @Serializable
    data class ProjectPreview(val id: String) : Screen

    @Serializable
    data class UpsertProject(val id: String? = null) : Screen

    @Serializable
    data class ConcreteCalculator(val id: String? = null) : Screen

    @Serializable
    data class BrickCalculator(val id: String? = null) : Screen

    @Serializable
    data class SteelCalculator(val id: String? = null) : Screen

    @Serializable
    data class AreaCalculator(val id: String? = null) : Screen

    @Serializable
    data class VolumeCalculator(val id: String? = null) : Screen

    @Serializable
    data class BeamCalculator(val id: String? = null) : Screen

    @Serializable
    data class ColumnCalculator(val id: String? = null) : Screen

    @Serializable
    data class SlabCalculator(val id: String? = null) : Screen

    @Serializable
    data class FoundationCalculator(val id: String? = null) : Screen

    @Serializable
    data class UnitConverter(val id: String? = null) : Screen
}
