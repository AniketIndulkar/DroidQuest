package dev.novanest.droidquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.hexColor

@Composable
fun StarredScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val starredLessons = ui.progress.starredLessonIds.mapNotNull { content.lesson(it) }
    val groups = starredLessons.groupBy { it.categoryId }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp)) {
        Text("Starred Lessons", color = DQ.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 4.dp))
        Text("${starredLessons.size} lesson${if (starredLessons.size == 1) "" else "s"} pinned", color = DQ.text(0.5f), fontSize = 13.sp, modifier = Modifier.padding(bottom = 18.dp))

        if (starredLessons.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 60.dp, horizontal = 20.dp), contentAlignment = Alignment.Center) {
                Text("Star a lesson from its overview to pin it here for quick review.", color = DQ.text(0.4f), fontSize = 13.5.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                groups.forEach { (categoryId, lessons) ->
                    val cat = content.category(categoryId)
                    val color = cat?.let { hexColor(it.theme.color) } ?: DQ.Green
                    Column {
                        Text((cat?.title ?: categoryId).uppercase(), color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            lessons.forEach { lesson ->
                                val node = content.roadmap.nodes.firstOrNull { it.lessonId == lesson.id }
                                Row(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(14.dp))
                                        .clickable { vm.openTopic(lesson.id, node?.id, lesson.categoryId) }.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(lesson.title, color = DQ.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Week ${lesson.week} · ~${lesson.estimatedLearningMinutes} min", color = DQ.text(0.45f), fontSize = 11.sp)
                                    }
                                    Text("★", color = DQ.Amber, fontSize = 18.sp, modifier = Modifier.clickable { vm.toggleStar(lesson.id) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
