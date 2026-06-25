package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import androidx.compose.runtime.Stable
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.ByCreate
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory

@GenerateFactory
@Stable
internal class DeviceControlViewModel(
    @ByCreate private val params: AdbDeviceScreenParams,
    private val interactor: DeviceParameterInteractor,
) : ViewModel() {

    private val _state = MutableStateFlow<DeviceControlViewState>(DeviceControlViewState.Loading)
    val state: StateFlow<DeviceControlViewState> = _state.asStateFlow()

    init {
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            loadParameters()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun onToggle(parameterId: String, value: Boolean) {
        val current = _state.value
        if (current !is DeviceControlViewState.Content) return
        val updated = current.parameters.map { param ->
            if (param is DeviceParameter.Toggle && param.id == parameterId) {
                param.copy(isChecked = value, isLoading = true)
            } else {
                param
            }
        }
        _state.value = DeviceControlViewState.Content(updated)
        launch {
            try {
                interactor.setToggle(params.deviceName, parameterId, value)
            } catch (e: Exception) {
                adbDeviceLogger.e(e) { "Failed to set toggle '$parameterId' to $value" }
            }
            loadParameters()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun loadParameters() {
        _state.value = try {
            val parameters = interactor.readParameters(params.deviceName)
            DeviceControlViewState.Content(parameters)
        } catch (e: Exception) {
            adbDeviceLogger.e(e) { "Failed to load parameters for device '${params.deviceName}'" }
            DeviceControlViewState.Error
        }
    }
}

internal sealed interface DeviceControlViewState {
    data object Loading : DeviceControlViewState
    data class Content(val parameters: List<DeviceParameter>) : DeviceControlViewState
    data object Error : DeviceControlViewState
}
