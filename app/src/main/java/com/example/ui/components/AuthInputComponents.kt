package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import kotlin.math.hypot

@Composable
fun SecurePinKeypad(
    pinLength: Int = 4,
    enteredPin: String,
    onDigitPress: (Char) -> Unit,
    onDeletePress: () -> Unit,
    errorMessage: String? = null,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val keySize: Dp = if (isCompact) 60.dp else 72.dp
    val spacing: Dp = if (isCompact) 18.dp else 26.dp
    val dotSize: Dp = if (isCompact) 14.dp else 18.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 16.dp)
    ) {
        // PIN dot indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = if (isCompact) 6.dp else 12.dp)
        ) {
            for (i in 0 until pinLength) {
                val isFilled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(
                            if (errorMessage != null) MaterialTheme.colorScheme.error
                            else if (isFilled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (errorMessage != null) MaterialTheme.colorScheme.error
                            else if (isFilled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

        // 3x4 Keypad grid
        val keys = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf(null, '0', '<')
        )

        for (row in keys) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (key in row) {
                    when (key) {
                        null -> {
                            Spacer(modifier = Modifier.size(keySize))
                        }
                        '<' -> {
                            Surface(
                                onClick = onDeletePress,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(keySize)
                                    .testTag("keypad_delete_button")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(if (isCompact) 22.dp else 26.dp)
                                    )
                                }
                            }
                        }
                        else -> {
                            Surface(
                                onClick = { onDigitPress(key) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(keySize)
                                    .testTag("keypad_digit_$key")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = key.toString(),
                                        style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatternLockInput(
    onPatternComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    errorMessage: String? = null
) {
    var selectedPoints by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }
    var nodePositions by remember { mutableStateOf<Map<Int, Offset>>(emptyMap()) }
    val canvasSize = if (isCompact) 240.dp else 280.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(canvasSize)
                .testTag("pattern_lock_view")
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedPoints = emptyList()
                            currentTouchPoint = offset
                            findNodeAt(offset, nodePositions)?.let { node ->
                                selectedPoints = listOf(node)
                            }
                        },
                        onDrag = { change, _ ->
                            currentTouchPoint = change.position
                            findNodeAt(change.position, nodePositions)?.let { node ->
                                if (!selectedPoints.contains(node)) {
                                    selectedPoints = selectedPoints + node
                                }
                            }
                        },
                        onDragEnd = {
                            if (selectedPoints.size >= 4) {
                                val patternCode = selectedPoints.joinToString("-")
                                onPatternComplete(patternCode)
                            }
                            currentTouchPoint = null
                            selectedPoints = emptyList()
                        },
                        onDragCancel = {
                            currentTouchPoint = null
                            selectedPoints = emptyList()
                        }
                    )
                }
        ) {
            val lineColor = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            val dotColor = MaterialTheme.colorScheme.onSurfaceVariant
            val activeDotColor = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val stepX = width / 4f
                val stepY = height / 4f

                val positions = mutableMapOf<Int, Offset>()
                for (row in 0..2) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        positions[index] = Offset((col + 1) * stepX, (row + 1) * stepY)
                    }
                }
                nodePositions = positions

                // Draw connecting lines between selected points
                for (i in 0 until selectedPoints.size - 1) {
                    val p1 = positions[selectedPoints[i]]
                    val p2 = positions[selectedPoints[i + 1]]
                    if (p1 != null && p2 != null) {
                        drawLine(
                            color = lineColor,
                            start = p1,
                            end = p2,
                            strokeWidth = 10f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Draw line to current touch point
                if (selectedPoints.isNotEmpty() && currentTouchPoint != null) {
                    val lastPoint = positions[selectedPoints.last()]
                    if (lastPoint != null) {
                        drawLine(
                            color = lineColor.copy(alpha = 0.7f),
                            start = lastPoint,
                            end = currentTouchPoint!!,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Draw 9 nodes
                for (index in 0..8) {
                    val pos = positions[index] ?: continue
                    val isSelected = selectedPoints.contains(index)
                    drawCircle(
                        color = if (isSelected) activeDotColor else dotColor.copy(alpha = 0.5f),
                        radius = if (isSelected) 18f else 12f,
                        center = pos
                    )
                    if (isSelected) {
                        drawCircle(
                            color = activeDotColor.copy(alpha = 0.25f),
                            radius = 34f,
                            center = pos
                        )
                    }
                }
            }
        }
    }
}

private fun findNodeAt(touch: Offset, nodePositions: Map<Int, Offset>): Int? {
    val hitRadius = 45f
    for ((index, pos) in nodePositions) {
        val dist = hypot(touch.x - pos.x, touch.y - pos.y)
        if (dist <= hitRadius) {
            return index
        }
    }
    return null
}
