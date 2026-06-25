package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.vladislavsumin.core.adb.client.AdbClient

internal class DeviceParameterInteractorImpl(private val adbClient: AdbClient) : DeviceParameterInteractor {

    override suspend fun readParameters(deviceName: String): List<DeviceParameter> = coroutineScope {
        val themeDeferred = async { readTheme(deviceName) }
        val wifiDeferred = async { readWifi(deviceName) }
        val mobileDataDeferred = async { readMobileData(deviceName) }
        listOf(themeDeferred.await(), wifiDeferred.await(), mobileDataDeferred.await())
    }

    override suspend fun setToggle(deviceName: String, parameterId: String, value: Boolean) {
        val command = when (parameterId) {
            ID_THEME -> "cmd uimode night ${if (value) "yes" else "no"}"
            ID_WIFI -> "svc wifi ${if (value) "enable" else "disable"}"
            ID_MOBILE_DATA -> "svc data ${if (value) "enable" else "disable"}"
            else -> return
        }
        val result = adbClient.executeShellCommand(deviceName, command)
        if (result is AdbClient.AdbResult.Err) {
            adbDeviceLogger.e(result.t) { "ADB set toggle '$parameterId'=$value failed" }
            throw result.t
        }
    }

    private suspend fun readTheme(deviceName: String): DeviceParameter {
        val raw = adbClient.executeShellCommand(deviceName, "settings get secure ui_night_mode").unwrap()
        val isNight = raw.trim() == "2"
        return DeviceParameter.Toggle(
            id = ID_THEME,
            name = "Dark theme",
            isChecked = isNight,
        )
    }

    private suspend fun readWifi(deviceName: String): DeviceParameter {
        val raw = adbClient.executeShellCommand(deviceName, "settings get global wifi_on").unwrap()
        val isOn = raw.trim() == "1"
        return DeviceParameter.Toggle(
            id = ID_WIFI,
            name = "Wi‑Fi",
            isChecked = isOn,
        )
    }

    private suspend fun readMobileData(deviceName: String): DeviceParameter {
        // Android on multi-SIM devices stores mobile data state per slot:
        // mobile_data3, mobile_data4, mobile_data5, etc.
        // The legacy "mobile_data" (no digit) key stays 1 even when data is off via svc,
        // so we only check the per-slot keys. Data is enabled if any slot is active.
        val result = adbClient.executeShellCommand(
            deviceName,
            "settings list global | grep '^mobile_data[0-9]'",
        ).unwrap()
        val isOn = result.lineSequence().any { line -> line.trim().endsWith("=1") }
        return DeviceParameter.Toggle(
            id = ID_MOBILE_DATA,
            name = "Mobile data",
            isChecked = isOn,
        )
    }

    companion object {
        internal const val ID_THEME = "theme"
        internal const val ID_WIFI = "wifi"
        internal const val ID_MOBILE_DATA = "mobile_data"
    }
}
