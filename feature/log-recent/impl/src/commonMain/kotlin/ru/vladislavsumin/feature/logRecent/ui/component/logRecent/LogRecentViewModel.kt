package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.map
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.feature.logRecent.domain.LogRecent
import ru.vladislavsumin.feature.logRecent.domain.LogRecentInteractorInternal
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.Notification
import ru.vladislavsumin.qa.feature.notifications.ui.component.notifications.NotificationsUiInteractor
import kotlin.io.path.exists

@GenerateFactory
@Stable
internal class LogRecentViewModel(
    private val logRecentInteractor: LogRecentInteractorInternal,
    @ByCreate private val notificationsUiInteractor: NotificationsUiInteractor,
) : ViewModel() {
    val state = logRecentInteractor.observeRecents()
        .map { recents -> LogRecentViewState(recents) }
        .stateIn(LogRecentViewState.STUB)

    fun checkRecentCanBeOpened(recent: LogRecent): Boolean {
        val isExists = recent.path.exists()
        if (!isExists) {
            launch {
                notificationsUiInteractor.showNotification(
                    Notification(
                        text = "File not found: ${recent.path}",
                        servility = Notification.Servility.Error,
                    ),
                )
                logRecentInteractor.removeRecent(recent)
            }
        }
        return isExists
    }

    fun onClickRemoveRecent(recent: LogRecent) {
        launch { logRecentInteractor.removeRecent(recent) }
    }
}
