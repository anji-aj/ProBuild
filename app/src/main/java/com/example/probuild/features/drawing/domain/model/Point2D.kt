package com.example.probuild.features.drawing.domain.model

import kotlinx.serialization.Serializable
import kotlin.math.hypot

@Serializable
data class Point2D(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun distanceTo(other: Point2D): Float {
        return hypot(other.x - x, other.y - y)
    }

    fun minus(other: Point2D): Point2D = Point2D(x - other.x, y - other.y)
    fun plus(other: Point2D): Point2D = Point2D(x + other.x, y + other.y)
    fun times(factor: Float): Point2D = Point2D(x * factor, y * factor)
}
