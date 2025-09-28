package ru.vladislavsumin.qa.feature.notifications.ui.component.notifications

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.ui.button.QaIconButton
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme

@GenerateFactory(NotificationsComponentFactory::class)
internal class NotificationsComponentImpl(
    viewModelFactory: NotificationsViewModelFactory,
    context: ComponentContext,
) : Component(context), NotificationsComponent {
    private val viewModel = viewModel { viewModelFactory.create() }

    override val notificationsUiInteractor: NotificationsUiInteractor = viewModel

    @Composable
    override fun Render(modifier: Modifier) {
        val state by viewModel.state.collectAsState()
        LazyColumn(modifier) {
            items(state.notifications, { it.id }) {
                Card(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .defaultMinSize(minWidth = 256.dp),
                    colors = CardDefaults.cardColors().copy(
                        containerColor = when (it.servility) {
                            Notification.Servility.Error -> QaTheme.colorScheme.logError.background
                        },
                    ),
                ) {
                    Row(Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
                        Text(it.text, Modifier.weight(1f))
                        QaIconButton(onClick = { viewModel.onClickCloseNotification(it.id) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "close")
                        }
                    }
                }
            }
        }
    }
}
