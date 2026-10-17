package com.irv205.apimockinterceptor.ui.gesture

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Observes pointer drags without consuming them (so normal scrolling keeps working)
 * and fires [onLDetected] when the user draws an "L" shape: a mostly-vertical
 * stroke downwards followed by a mostly-horizontal stroke to the right.
 */
fun Modifier.lDebugGesture(onLDetected: () -> Unit): Modifier = pointerInput(Unit) {
    val minSize = 96.dp.toPx()
    awaitPointerEventScope {
        while (true) {
            val down = awaitFirstDown(requireUnconsumed = false)
            val pointerId = down.id
            val points = mutableListOf(down.position)
            var isUp = false
            while (!isUp) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId }
                if (change == null) {
                    isUp = true
                } else {
                    points.add(change.position)
                    if (change.changedToUpIgnoreConsumed()) isUp = true
                }
            }
            if (isLShape(points, minSize)) onLDetected()
        }
    }
}

internal fun isLShape(points: List<Offset>, minSize: Float): Boolean {
    if (points.size < 10) return false

    val start = points.first()
    val end = points.last()
    val corner = points.maxByOrNull { it.y } ?: return false

    val width = points.maxOf { it.x } - points.minOf { it.x }
    val height = points.maxOf { it.y } - points.minOf { it.y }

    if (width < minSize || height < minSize) return false

    val verticalLeg = abs(corner.x - start.x) < height * 0.4f &&
        (corner.y - start.y) > height * 0.6f

    val horizontalLeg = abs(end.y - corner.y) < width * 0.4f &&
        (end.x - corner.x) > width * 0.6f

    return verticalLeg && horizontalLeg
}
