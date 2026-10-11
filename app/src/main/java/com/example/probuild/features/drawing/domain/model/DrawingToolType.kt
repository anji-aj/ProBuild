package com.example.probuild.features.drawing.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DrawingToolType(val displayName: String) {
    FREEHAND_PEN("Freehand Pen"),
    PENCIL("Pencil"),
    HIGHLIGHTER("Highlighter"),
    ERASER("Eraser"),
    LINE("Line"),
    RECTANGLE("Rectangle"),
    SQUARE("Square"),
    CIRCLE("Circle"),
    ELLIPSE("Ellipse"),
    TRIANGLE("Triangle"),
    ARROW("Arrow"),
    POLYLINE("Polyline"),
    POLYGON("Polygon"),
    ARC("Arc"),
    WALL("Wall"),
    DOOR("Door Symbol"),
    WINDOW("Window Symbol"),
    CUBOID("3D Box"),
    TEXT("Text Annotation"),
    DIMENSION("Dimension Line"),
    SELECTION("Select & Move"),
    PAN_ZOOM("Pan & Zoom")
}
