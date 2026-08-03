package ru.vladislavsumin.core.ui.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset

/**
 * Позиция курсора в экранных координатах Compose-пикселей (то же пространство,
 * что у [androidx.compose.ui.layout.LayoutCoordinates.localToScreen]),
 * либо null если указатель недоступен.
 *
 * На десктопе отслеживается глобально (за пределами компонентов), поэтому эффекты
 * могут реагировать на курсор даже за границами кнопки. На платформах без указателя
 * (Android) всегда null.
 */
@Composable
expect fun rememberGlobalPointerPosition(): State<Offset?>
