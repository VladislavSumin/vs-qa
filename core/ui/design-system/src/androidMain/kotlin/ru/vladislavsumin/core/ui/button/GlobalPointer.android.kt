package ru.vladislavsumin.core.ui.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset

@Composable
actual fun rememberGlobalPointerPosition(): State<Offset?> = remember { mutableStateOf<Offset?>(null) }
