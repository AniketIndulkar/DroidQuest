package dev.novanest.droidquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.LoadedContent
import dev.novanest.droidquest.content.model.CategoryStatus
import dev.novanest.droidquest.content.model.SearchDocumentDto
import dev.novanest.droidquest.ui.state.DroidQuestUiState
import dev.novanest.droidquest.ui.state.DroidQuestViewModel
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.hexColor

@Composable
fun SearchScreen(vm: DroidQuestViewModel, content: LoadedContent, ui: DroidQuestUiState) {
    val q = ui.nav.query.trim().lowercase()
    val filter = ui.nav.tagFilter
    val catTitleById = content.categories.associate { it.id to it.title }

    val results = content.search.documents.filter { doc ->
        (filter == "All" || catTitleById[doc.categoryId] == filter) &&
            (q.isEmpty() ||
                doc.title.lowercase().contains(q) ||
                doc.text.contains(q) ||
                doc.type.contains(q) ||
                doc.tags.any { it.lowercase().contains(q) })
    }
    val cats = listOf("All") + content.categoriesInOrder().map { it.title }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 36.dp)) {
        Text("Search", color = DQ.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 14.dp))

        BasicTextField(
            value = ui.nav.query,
            onValueChange = { vm.setQuery(it) },
            textStyle = TextStyle(color = DQ.TextPrimary, fontSize = 14.sp),
            cursorBrush = SolidColor(DQ.Green),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(100)).background(DQ.Card).border(1.dp, DQ.white(0.08f), RoundedCornerShape(100)).padding(horizontal = 18.dp, vertical = 13.dp),
            decorationBox = { inner ->
                if (ui.nav.query.isEmpty()) Text("Search lessons, quizzes, challenges, glossary…", color = DQ.text(0.4f), fontSize = 14.sp)
                inner()
            },
        )
        Box(Modifier.padding(bottom = 14.dp))

        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cats.forEach { c ->
                val active = filter == c
                Text(c, color = if (active) DQ.Ink else DQ.text(0.65f), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                    modifier = Modifier.clip(RoundedCornerShape(100)).background(if (active) DQ.Green else DQ.text(0.08f)).clickable { vm.setTagFilter(c) }.padding(horizontal = 14.dp, vertical = 8.dp))
            }
        }

        Text("${results.size} result${if (results.size == 1) "" else "s"}", color = DQ.text(0.4f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 10.dp))

        if (results.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 20.dp), contentAlignment = Alignment.Center) {
                Text("No results. Try another term or filter.", color = DQ.text(0.4f), fontSize = 13.5.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                results.take(60).forEach { doc -> SearchResultRow(vm, content, doc) }
            }
        }
    }
}

@Composable
private fun SearchResultRow(vm: DroidQuestViewModel, content: LoadedContent, doc: SearchDocumentDto) {
    val cat = content.category(doc.categoryId)
    val color = cat?.let { hexColor(it.theme.color) } ?: DQ.Green
    val locked = cat?.status == CategoryStatus.PLANNED
    val typeLabel = doc.type.replaceFirstChar { it.uppercase() }

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DQ.Card).border(1.dp, DQ.Border, RoundedCornerShape(14.dp))
            .clickable(enabled = !locked) { route(vm, content, doc) }.alpha(if (locked) 0.5f else 1f).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
            Text(glyphFor(doc.type), color = color, fontSize = 15.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(doc.title, color = DQ.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("$typeLabel · ${cat?.title ?: ""}", color = DQ.text(0.45f), fontSize = 11.sp, maxLines = 1)
        }
        if (locked) Text("Locked", color = DQ.text(0.4f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(100)).background(DQ.text(0.08f)).padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

private fun glyphFor(type: String): String = when (type) {
    "lesson" -> "▸"
    "quiz" -> "★"
    "challenge" -> "⚑"
    "glossary" -> "§"
    "category" -> "◆"
    else -> "•"
}

private fun route(vm: DroidQuestViewModel, content: LoadedContent, doc: SearchDocumentDto) {
    when (val r = dev.novanest.droidquest.domain.SearchRouter.route(content, doc)) {
        is dev.novanest.droidquest.domain.SearchRoute.Lesson -> vm.openTopic(r.lessonId, r.nodeId, r.categoryId)
        is dev.novanest.droidquest.domain.SearchRoute.Quiz -> vm.startQuiz(r.quizId)
        is dev.novanest.droidquest.domain.SearchRoute.Challenge -> vm.openChallenge(r.challengeId)
        is dev.novanest.droidquest.domain.SearchRoute.Category -> vm.openCategory(r.categoryId)
        dev.novanest.droidquest.domain.SearchRoute.None -> Unit
    }
}
