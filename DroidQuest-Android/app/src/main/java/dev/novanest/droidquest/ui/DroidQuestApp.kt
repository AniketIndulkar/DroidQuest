package dev.novanest.droidquest.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.novanest.droidquest.content.ContentErrorKind
import dev.novanest.droidquest.content.ContentLoadState
import dev.novanest.droidquest.ui.screens.ChallengeScreen
import dev.novanest.droidquest.ui.screens.HomeScreen
import dev.novanest.droidquest.ui.screens.LessonScreen
import dev.novanest.droidquest.ui.screens.QuestMapScreen
import dev.novanest.droidquest.ui.screens.RegionDetailScreen
import dev.novanest.droidquest.ui.screens.RevisionScreen
import dev.novanest.droidquest.ui.screens.SearchScreen
import dev.novanest.droidquest.ui.screens.SettingsScreen
import dev.novanest.droidquest.ui.screens.StarredScreen
import dev.novanest.droidquest.ui.screens.TopicDetailScreen
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.Screen
import dev.novanest.droidquest.ui.state.isTopLevel
import dev.novanest.droidquest.ui.theme.DQ

private data class NavTab(val screen: Screen, val glyph: String, val label: String)

private val NAV_TABS = listOf(
    NavTab(Screen.HOME, "⌂", "Home"),
    NavTab(Screen.MAP, "◆", "Map"),
    NavTab(Screen.SEARCH, "⚲", "Search"),
    NavTab(Screen.STARRED, "★", "Starred"),
    NavTab(Screen.SETTINGS, "⚙", "Settings"),
)

@Composable
fun DroidQuestApp(vm: DroidQuestViewModel) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val openUrl: (String) -> Unit = { url ->
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
    }

    Box(Modifier.fillMaxSize().background(DQ.ScreenBg)) {
        when (val load = ui.loadState) {
            is ContentLoadState.Loading -> LoadingScreen()
            is ContentLoadState.Error -> ErrorScreen(load.kind, load.message) { vm.loadContent() }
            is ContentLoadState.Success -> {
                val content = load.content
                val screen = ui.nav.screen
                val aiVisible = screen != Screen.REVISION && screen != Screen.SETTINGS
                Column(Modifier.fillMaxSize().windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.systemBars)) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        when (screen) {
                            Screen.HOME -> HomeScreen(vm, content, ui)
                            Screen.MAP -> QuestMapScreen(vm, content, ui)
                            Screen.REGION -> RegionDetailScreen(vm, content, ui)
                            Screen.TOPIC -> TopicDetailScreen(vm, content, ui, openUrl)
                            Screen.LESSON -> LessonScreen(vm, content, ui, openUrl)
                            Screen.REVISION -> RevisionScreen(vm, content, ui)
                            Screen.CHALLENGE -> ChallengeScreen(vm, content, ui)
                            Screen.SEARCH -> SearchScreen(vm, content, ui)
                            Screen.STARRED -> StarredScreen(vm, content, ui)
                            Screen.SETTINGS -> SettingsScreen(vm, content, ui)
                        }
                        if (aiVisible) {
                            AiHelper(
                                open = ui.nav.aiOpen,
                                onToggle = { vm.toggleAI() },
                                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 30.dp),
                            )
                        }
                    }
                    if (screen.isTopLevel) BottomNav(screen) { vm.goTo(it) }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = DQ.Green)
        Text("Loading curriculum…", color = DQ.text(0.6f), fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun ErrorScreen(kind: ContentErrorKind, message: String, onRetry: () -> Unit) {
    val heading = when (kind) {
        ContentErrorKind.MISSING_CONTENT -> "Content files are missing"
        ContentErrorKind.MALFORMED_JSON -> "Content is malformed"
        ContentErrorKind.UNSUPPORTED_VERSION -> "Content version unsupported"
        ContentErrorKind.HASH_MISMATCH -> "Content integrity check failed"
        ContentErrorKind.UNKNOWN -> "Content failed to load"
    }
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("⚠", color = DQ.Amber, fontSize = 40.sp, modifier = Modifier.padding(bottom = 12.dp))
        Text(heading, color = DQ.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(message, color = DQ.text(0.55f), fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        Text(
            "Retry", color = DQ.Ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center,
            modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(DQ.Green).clickable { onRetry() }.padding(horizontal = 40.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun BottomNav(active: Screen, onSelect: (Screen) -> Unit) {
    Column(Modifier.fillMaxWidth().background(DQ.ScreenBg)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(DQ.Border))
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NAV_TABS.forEach { tab ->
                val color = if (active == tab.screen) DQ.Green else DQ.text(0.4f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.widthIn(min = 44.dp).clip(RoundedCornerShape(8.dp)).clickable { onSelect(tab.screen) }.padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(tab.glyph, color = color, fontSize = 19.sp)
                    Text(tab.label, color = color, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AiHelper(open: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (open) {
            Column(
                Modifier.width(230.dp).clip(RoundedCornerShape(16.dp)).background(DQ.CardAlt).border(1.dp, DQ.white(0.1f), RoundedCornerShape(16.dp)).padding(14.dp),
            ) {
                Text("AI Helper", color = DQ.text(0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Text("Optional hints live here. AI assistance is a network extra — all learning works fully offline without it.", color = DQ.TextPrimary, fontSize = 13.sp, lineHeight = 19.5.sp)
            }
        }
        Box(
            Modifier.size(46.dp).clip(CircleShape).background(DQ.CardAlt).border(1.dp, DQ.white(0.12f), CircleShape).clickable(onClick = onToggle),
            contentAlignment = Alignment.Center,
        ) { Text("✦", color = DQ.BlueLight, fontSize = 17.sp) }
    }
}
