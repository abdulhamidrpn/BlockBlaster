package com.rpn.blockblaster.domain.model

import androidx.compose.ui.graphics.Color

data class Block(
    val shape: List<List<Boolean>>,
    val color: Color,
    val name: String = ""
) {
    val rows: Int get() = shape.size
    val cols: Int get() = if (shape.isEmpty()) 0 else shape[0].size
    val cellCount: Int get() = shape.sumOf { row -> row.count { it } }
}
