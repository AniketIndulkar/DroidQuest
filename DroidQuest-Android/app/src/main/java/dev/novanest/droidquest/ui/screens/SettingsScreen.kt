package dev.novanest.droidquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.ui.components.ToggleSwitch
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.state.UiDerive
import dev.novanest.droidquest.ui.theme.DQ

@Composable
fun SettingsScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val progress = ui.progress
    val s = progress.settings
    val level = UiDerive.currentLevelNumber(content, progress)

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp)) {
        Text("Settings", color = DQ.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 18.dp))

        // Profile
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(DQ.Blue.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Text("You", color = DQ.BlueLight, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
            Column(Modifier.weight(1f)) {
                Text("Learner", color = DQ.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Level $level · ${progress.totalXp} XP · ${progress.totalStars}★", color = DQ.text(0.45f), fontSize = 12.sp)
            }
        }
        Box(Modifier.height(20.dp))

        SectionHeader("Sync")
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(16.dp)).padding(vertical = 6.dp, horizontal = 4.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(DQ.text(0.1f)), contentAlignment = Alignment.Center) {
                    Text("GH", color = DQ.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Column(Modifier.weight(1f)) {
                    Text("GitHub Sync", color = DQ.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(if (s.githubConnected) "Connected · optional" else "Not connected · optional", color = DQ.text(0.45f), fontSize = 11.5.sp)
                }
                ToggleSwitch(s.githubConnected) { vm.setGithub(!s.githubConnected) }
            }
            Divider()
            Text("Back up progress now", color = DQ.Green, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clickable { vm.backupNow() }.padding(horizontal = 14.dp, vertical = 12.dp))
            Text("Local-first: progress lives on this device and syncs to GitHub only when connected.", color = DQ.text(0.35f), fontSize = 11.sp, lineHeight = 16.5.sp, modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp))
        }
        Box(Modifier.height(20.dp))

        SectionHeader("Learning")
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(16.dp))) {
            ToggleRow("Daily reminders", s.notifications) { vm.setNotifications(!s.notifications) }
            Divider()
            ToggleRow("Sound effects", s.sound) { vm.setSound(!s.sound) }
        }
        Box(Modifier.height(20.dp))

        SectionHeader("About")
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(16.dp))) {
            InfoRow("Curriculum version", content.curriculum.version)
            Divider()
            InfoRow("Levels", "${content.categories.size} (2 available)")
            Divider()
            InfoRow("Lessons", "${content.lessonsById.size}")
        }
        Box(Modifier.height(20.dp))

        Text("DroidQuest · ${content.curriculum.title}", color = DQ.text(0.3f), fontSize = 11.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text.uppercase(), color = DQ.text(0.4f), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(1.dp).background(DQ.Border))
}

@Composable
private fun ToggleRow(label: String, on: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = DQ.TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        ToggleSwitch(on, onToggle)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = DQ.TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = DQ.text(0.5f), fontSize = 13.sp)
    }
}
