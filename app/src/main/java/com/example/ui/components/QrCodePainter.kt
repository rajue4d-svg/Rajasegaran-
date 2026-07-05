package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun StudentQrCode(
    studentId: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    qrColor: Color = Color.Black
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val sizePx = size.width
            val numModules = 21 // Version 1 QR code is 21x21 grid
            val moduleSize = sizePx / numModules

            // Draw finders (the three big squares)
            drawFinder(0f, 0f, moduleSize, qrColor) // Top-Left
            drawFinder((numModules - 7) * moduleSize, 0f, moduleSize, qrColor) // Top-Right
            drawFinder(0f, (numModules - 7) * moduleSize, moduleSize, qrColor) // Bottom-Left

            // Draw alignment pattern (for visuals)
            drawModule(14 * moduleSize, 14 * moduleSize, 2 * moduleSize, qrColor)

            // Hash the studentId to generate deterministic random modules
            val hash = studentId.hashCode()
            val random = java.util.Random(hash.toLong())

            for (row in 0 until numModules) {
                for (col in 0 until numModules) {
                    // Skip finders regions
                    if (row < 8 && col < 8) continue // Top-Left
                    if (row < 8 && col >= numModules - 8) continue // Top-Right
                    if (row >= numModules - 8 && col < 8) continue // Bottom-Left

                    // Deterministic squares
                    val draw = random.nextBoolean()
                    if (draw) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(col * moduleSize, row * moduleSize),
                            size = Size(moduleSize, moduleSize)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawFinder(x: Float, y: Float, moduleSize: Float, color: Color) {
    val size = moduleSize * 7
    // 1. Outer square (7x7 modules)
    drawRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(size, size)
    )
    // 2. Inner white block (5x5 modules)
    drawRect(
        color = Color.White,
        topLeft = Offset(x + moduleSize, y + moduleSize),
        size = Size(size - 2 * moduleSize, size - 2 * moduleSize)
    )
    // 3. Center black block (3x3 modules)
    drawRect(
        color = color,
        topLeft = Offset(x + 2 * moduleSize, y + 2 * moduleSize),
        size = Size(size - 4 * moduleSize, size - 4 * moduleSize)
    )
}

private fun DrawScope.drawModule(x: Float, y: Float, size: Float, color: Color) {
    drawRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(size, size)
    )
    drawRect(
        color = Color.White,
        topLeft = Offset(x + size / 4f, y + size / 4f),
        size = Size(size / 2f, size / 2f)
    )
}
