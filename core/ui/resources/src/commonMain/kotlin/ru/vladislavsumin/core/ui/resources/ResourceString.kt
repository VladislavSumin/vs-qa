package ru.vladislavsumin.core.ui.resources

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface ResourceString {
    @ConsistentCopyVisibility
    data class Plain internal constructor(internal val text: String) : ResourceString

    @ConsistentCopyVisibility
    data class Resource internal constructor(
        internal val res: StringResource,
        internal val args: List<Any> = emptyList(),
    ) : ResourceString
}

fun String.asResourceString(): ResourceString = ResourceString.Plain(this)

fun StringResource.asResourceString(vararg args: Any): ResourceString = ResourceString.Resource(this, args.toList())

@Composable
fun ResourceString.resolve(): String = when (this) {
    is ResourceString.Plain -> text
    is ResourceString.Resource -> stringResource(res, *args.toTypedArray())
}
