package com.example.probuild.features.drawing.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface DrawingElement {
    val id: String
    val isSelected: Boolean

    @Serializable
    data class FreehandStroke(
        override val id: String,
        val points: List<Point2D>,
        val colorArgb: Int,
        val strokeWidth: Float,
        val alpha: Float = 1.0f,
        val toolType: DrawingToolType = DrawingToolType.FREEHAND_PEN,
        val isRecognizedShape: Boolean = false,
        override val isSelected: Boolean = false
    ) : DrawingElement

    @Serializable
    data class GeometricShape(
        override val id: String,
        val shapeType: DrawingToolType,
        val points: List<Point2D>,
        val colorArgb: Int,
        val strokeWidth: Float,
        val isFilled: Boolean = false,
        val labelText: String? = null,
        val originalStrokePoints: List<Point2D>? = null,
        override val isSelected: Boolean = false
    ) : DrawingElement

    @Serializable
    data class TextElement(
        override val id: String,
        val position: Point2D,
        val text: String,
        val fontSize: Float = 16f,
        val colorArgb: Int,
        val rotationDegrees: Float = 0f,
        override val isSelected: Boolean = false
    ) : DrawingElement

    @Serializable
    data class DimensionElement(
        override val id: String,
        val startPoint: Point2D,
        val endPoint: Point2D,
        val labelText: String,
        val realWorldDistance: Double? = null,
        val unitSymbol: String = "m",
        val colorArgb: Int,
        val strokeWidth: Float = 2.0f,
        val isCalibrated: Boolean = false,
        override val isSelected: Boolean = false
    ) : DrawingElement
}
