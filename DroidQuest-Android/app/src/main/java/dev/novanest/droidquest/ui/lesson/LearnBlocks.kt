package dev.novanest.droidquest.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.novanest.droidquest.content.model.CalloutTone
import dev.novanest.droidquest.content.model.FlowStep
import dev.novanest.droidquest.content.model.FurtherReadingDto
import dev.novanest.droidquest.content.model.LearnBlockDto
import dev.novanest.droidquest.ui.theme.DQ
import dev.novanest.droidquest.ui.theme.Mono

/** Render any Learn content block. Complete authored content — never collapsed to a summary. */
@Composable
fun LearnBlock(block: LearnBlockDto) {
    when (block) {
        is LearnBlockDto.Paragraph -> ParagraphBlock(block.text)
        is LearnBlockDto.Code -> CodeBlock(block.language, block.code, block.caption)
        is LearnBlockDto.Callout -> CalloutBlock(block.tone, block.title, block.text)
        is LearnBlockDto.Flow -> FlowBlock(block.title, block.steps)
        is LearnBlockDto.Table -> TableBlock(block.title, block.columns, block.rows)
        is LearnBlockDto.Listing -> ListBlock(block.title, block.items)
    }
}

@Composable
fun ParagraphBlock(text: String) {
    Text(text, color = DQ.text(0.82f), fontSize = 14.sp, lineHeight = 22.4.sp)
}

/** Code block with horizontal scrolling and a copy action. */
@Composable
fun CodeBlock(language: String, code: String, caption: String? = null) {
    val clipboard = LocalClipboardManager.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(language.uppercase(), color = DQ.text(0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontFamily = Mono)
            Text(
                "Copy", color = DQ.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { clipboard.setText(AnnotatedString(code)) }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DQ.Card)
                .border(1.dp, DQ.white(0.08f), RoundedCornerShape(12.dp))
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(code, color = DQ.TextPrimary, fontSize = 12.5.sp, fontFamily = Mono, lineHeight = 20.sp, softWrap = false)
        }
        if (caption != null) {
            Text(caption, color = DQ.text(0.45f), fontSize = 11.5.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
fun CalloutBlock(tone: CalloutTone, title: String, text: String) {
    val accent = when (tone) {
        CalloutTone.NOTE -> DQ.Blue
        CalloutTone.REMEMBER -> DQ.Green
        CalloutTone.WARNING -> DQ.Red
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.10f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 6.dp)) {
            Text(
                tone.name, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp,
                modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(accent.copy(alpha = 0.16f)).padding(horizontal = 6.dp, vertical = 2.dp),
            )
            Text(title, color = DQ.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(text, color = DQ.text(0.75f), fontSize = 13.sp, lineHeight = 19.5.sp)
    }
}

@Composable
fun FlowBlock(title: String, steps: List<FlowStep>) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = DQ.text(0.55f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, modifier = Modifier.padding(bottom = 8.dp))
        steps.forEachIndexed { i, step ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.width(26.dp).clip(RoundedCornerShape(100)).background(DQ.Blue.copy(alpha = 0.18f)).padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text("${i + 1}", color = DQ.BlueLight, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                    if (i < steps.lastIndex) Box(Modifier.width(2.dp).padding(vertical = 2.dp).background(DQ.white(0.12f)).height(18.dp))
                }
                Column(Modifier.weight(1f).padding(bottom = if (i < steps.lastIndex) 10.dp else 0.dp)) {
                    Text(step.label, color = DQ.TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    Text(step.detail, color = DQ.text(0.6f), fontSize = 12.5.sp, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun TableBlock(title: String, columns: List<String>, rows: List<List<String>>) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = DQ.text(0.55f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, modifier = Modifier.padding(bottom = 8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DQ.Card)
                .border(1.dp, DQ.white(0.08f), RoundedCornerShape(12.dp))
                .horizontalScroll(rememberScrollState()),
        ) {
            Column {
                Row(Modifier.background(DQ.white(0.04f))) {
                    columns.forEach { c ->
                        Text(c, color = DQ.text(0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(150.dp).padding(horizontal = 12.dp, vertical = 10.dp))
                    }
                }
                rows.forEachIndexed { ri, row ->
                    Row(Modifier.background(if (ri % 2 == 0) Color.Transparent else DQ.white(0.02f))) {
                        row.forEach { cell ->
                            Text(cell, color = DQ.text(0.8f), fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.width(150.dp).padding(horizontal = 12.dp, vertical = 9.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListBlock(title: String, items: List<String>) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, color = DQ.text(0.55f), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, modifier = Modifier.padding(bottom = 6.dp))
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 3.dp)) {
                Text("•", color = DQ.Green, fontSize = 13.sp)
                Text(item, color = DQ.text(0.78f), fontSize = 13.5.sp, lineHeight = 19.5.sp)
            }
        }
    }
}

/** Further-reading resource card that opens its URL in the system browser. */
@Composable
fun FurtherReadingCard(reading: FurtherReadingDto, onOpen: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DQ.Card)
            .border(1.dp, DQ.Border, RoundedCornerShape(12.dp))
            .clickable { onOpen(reading.url) }
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                reading.resourceType.replace('_', ' ').uppercase(), color = DQ.BlueLight, fontSize = 9.5.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp,
                modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(DQ.Blue.copy(alpha = 0.16f)).padding(horizontal = 6.dp, vertical = 2.dp),
            )
            Text(reading.publisher, color = DQ.text(0.45f), fontSize = 11.sp)
        }
        Text(reading.title, color = DQ.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 3.dp))
        Text(reading.whyRead, color = DQ.text(0.6f), fontSize = 12.5.sp, lineHeight = 18.sp)
        Text("Open ↗", color = DQ.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
    }
}
