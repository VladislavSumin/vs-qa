package ru.vladislavsumin.qa.feature.legalInfo.ui.screen.legalInfo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import ru.vladislavsumin.feature.legal_info.impl.generated.resources.Res

@Composable
internal fun LegalInfoScreenContent(modifier: Modifier = Modifier) {
    val libraries by produceLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }
    LibrariesContainer(libraries, modifier.fillMaxSize())
}
