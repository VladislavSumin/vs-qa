package ru.vladislavsumin.qa.feature.settings.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.vladislavsumin.core.ui.filePicker.DirectoryPickerDialog
import ru.vladislavsumin.feature.settings.impl.generated.resources.Res
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_dump_path_custom
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_dump_path_custom_hint
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_dump_path_temp
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_dump_path_title
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_language_english
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_language_russian
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_language_system
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_language_title
import ru.vladislavsumin.feature.settings.impl.generated.resources.settings_liquid_glass_title
import ru.vladislavsumin.qa.feature.settings.domain.AppLanguage
import ru.vladislavsumin.qa.feature.settings.domain.DumpPathOption

@Composable
internal fun SettingsScreenContent(viewModel: SettingsScreenViewModel, modifier: Modifier = Modifier) {
    val selectedLanguage by viewModel.language.collectAsState(AppLanguage.SYSTEM)
    val dumpPathOption by viewModel.dumpPathOption.collectAsState(DumpPathOption.Temp)
    val isLiquidGlass by viewModel.isLiquidGlass.collectAsState(true)

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(Res.string.settings_language_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Column(Modifier.selectableGroup()) {
            LanguageOption(
                AppLanguage.SYSTEM,
                Res.string.settings_language_system,
                selectedLanguage,
                viewModel::onSelectLanguage,
            )
            LanguageOption(
                AppLanguage.RUSSIAN,
                Res.string.settings_language_russian,
                selectedLanguage,
                viewModel::onSelectLanguage,
            )
            LanguageOption(
                AppLanguage.ENGLISH,
                Res.string.settings_language_english,
                selectedLanguage,
                viewModel::onSelectLanguage,
            )
        }

        Spacer(Modifier.height(24.dp))

        LiquidGlassSection(isLiquidGlass, viewModel::onToggleLiquidGlass)

        Spacer(Modifier.height(24.dp))

        DumpPathSection(dumpPathOption, viewModel)
    }
}

@Composable
private fun DumpPathSection(dumpPathOption: DumpPathOption, viewModel: SettingsScreenViewModel) {
    var showPicker by remember { mutableStateOf(false) }

    Text(
        text = stringResource(Res.string.settings_dump_path_title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    Column(Modifier.selectableGroup()) {
        DumpPathRow(
            Res.string.settings_dump_path_temp,
            dumpPathOption is DumpPathOption.Temp,
            viewModel::onSelectDumpPathTemp,
        )
        DumpPathRow(
            Res.string.settings_dump_path_custom,
            dumpPathOption is DumpPathOption.Custom,
            viewModel::onSelectDumpPathCustom,
        )
    }
    if (dumpPathOption is DumpPathOption.Custom) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = dumpPathOption.path,
                onValueChange = viewModel::onDumpCustomPathChange,
                label = { Text(stringResource(Res.string.settings_dump_path_custom_hint)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(onClick = { showPicker = true }) {
                Text("...")
            }
        }
    }
    if (showPicker) {
        DirectoryPickerDialog(
            title = stringResource(Res.string.settings_dump_path_title),
            onCloseRequest = { path ->
                showPicker = false
                if (path != null) viewModel.onDumpCustomPathChange(path)
            },
        )
    }
}

@Composable
private fun DumpPathRow(labelRes: StringResource, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = stringResource(labelRes), modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun LanguageOption(
    language: AppLanguage,
    titleRes: StringResource,
    selectedLanguage: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(language) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = language == selectedLanguage,
            onClick = { onSelect(language) },
        )
        Text(
            text = stringResource(titleRes),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun LiquidGlassSection(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!enabled) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.settings_liquid_glass_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}
