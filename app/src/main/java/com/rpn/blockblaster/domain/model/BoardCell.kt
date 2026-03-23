package com.rpn.blockblaster.domain.model

import androidx.compose.ui.graphics.Color

data class BoardCell(
    val isFilled:    Boolean = false,
    val color:       Color?  = null,
    val isHighlight: Boolean = false,
    val isValid:     Boolean = true,
    val isBlasting:  Boolean = false
)
