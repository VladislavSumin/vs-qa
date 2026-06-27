package ru.vladislavsumin.feature.logViewer.ui.component.logs

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

actual fun Modifier.onRightClick(onRightClick: (Offset) -> Unit): Modifier = this
