package ru.vladislavsumin.qa.feature.adbDevice.ui.screen.adbDevice

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ru.vladislavsumin.core.adb.client.AdbClient

internal interface DeviceParameterInteractor {
    suspend fun readParameters(deviceName: String): List<DeviceParameter>
    suspend fun setToggle(deviceName: String, parameterId: String, value: Boolean)
}

internal class DeviceParameterInteractorImpl(private val adbClient: AdbClient) : DeviceParameterInteractor {

    override suspend fun readParameters(deviceName: String): List<DeviceParameter> = coroutineScope {
        awaitAll(
            async { readTheme(deviceName) },
            async { readWifi(deviceName) },
            async { readMobileData(deviceName) },
            async { readStayAwake(deviceName) },
            async { readShowTaps(deviceName) },
            async { readPointerLocation(deviceName) },
        )
    }

    @Suppress("CyclomaticComplexMethod")
    override suspend fun setToggle(deviceName: String, parameterId: String, value: Boolean) {
        val command = when (parameterId) {
            ID_THEME -> "cmd uimode night ${if (value) "yes" else "no"}"
            ID_WIFI -> "svc wifi ${if (value) "enable" else "disable"}"
            ID_MOBILE_DATA -> "svc data ${if (value) "enable" else "disable"}"
            ID_STAY_AWAKE -> "settings put global stay_on_while_plugged_in ${if (value) 7 else 0}"
            ID_SHOW_TAPS -> "settings put system show_touches ${if (value) 1 else 0}"
            ID_POINTER_LOCATION -> "settings put system pointer_location ${if (value) 1 else 0}"
            else -> return
        }
        val result = adbClient.executeShellCommand(deviceName, command)
        if (result is AdbClient.AdbResult.Err) {
            adbDeviceLogger.e(result.t) { "ADB set toggle '$parameterId'=$value failed" }
            throw result.t
        }
    }

    private suspend fun readTheme(deviceName: String): DeviceParameter {
        val raw = adbClient.executeShellCommand(
            deviceName,
            "settings get secure ui_night_mode",
        ).unwrap()
        val isNight = raw.trim() == "2"
        return DeviceParameter.Toggle(
            id = ID_THEME,
            name = "Dark theme",
            isChecked = isNight,
        )
    }

    private suspend fun readWifi(deviceName: String): DeviceParameter {
        val raw = adbClient.executeShellCommand(
            deviceName,
            "settings get global wifi_on",
        ).unwrap()
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

    private suspend fun readStayAwake(deviceName: String): DeviceParameter {
        val raw = adbClient.executeShellCommand(
            deviceName,
            "settings get global stay_on_while_plugged_in",
        ).unwrap()
        val isOn = raw.trim().toIntOrNull()?.let { it > 0 } ?: false
        return DeviceParameter.Toggle(
            id = ID_STAY_AWAKE,
            name = "Stay awake",
            isChecked = isOn,
        )
    }

    private suspend fun readShowTaps(deviceName: String): DeviceParameter {
        val raw =
            adbClient.executeShellCommand(deviceName, "settings get system show_touches").unwrap()
        val isOn = raw.trim() == "1"
        return DeviceParameter.Toggle(
            id = ID_SHOW_TAPS,
            name = "Show taps",
            isChecked = isOn,
        )
    }

    private suspend fun readPointerLocation(deviceName: String): DeviceParameter {
        val raw = adbClient.executeShellCommand(deviceName, "settings get system pointer_location")
            .unwrap()
        val isOn = raw.trim() == "1"
        return DeviceParameter.Toggle(
            id = ID_POINTER_LOCATION,
            name = "Pointer location",
            isChecked = isOn,
        )
    }

    companion object {
        internal const val ID_THEME = "theme"
        internal const val ID_WIFI = "wifi"
        internal const val ID_MOBILE_DATA = "mobile_data"
        internal const val ID_STAY_AWAKE = "stay_awake"
        internal const val ID_SHOW_TAPS = "show_taps"
        internal const val ID_POINTER_LOCATION = "pointer_location"
    }
}
