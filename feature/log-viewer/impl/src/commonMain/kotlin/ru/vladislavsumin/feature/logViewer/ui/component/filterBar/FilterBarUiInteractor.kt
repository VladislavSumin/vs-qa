package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import kotlinx.coroutines.flow.Flow

internal interface FilterBarUiInteractor {
    val filterState: Flow<FilterRequestParser.ParserResult>
}
