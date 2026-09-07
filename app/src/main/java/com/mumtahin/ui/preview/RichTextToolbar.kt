package com.mumtahin.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Text alignment options exposed by [RichTextToolbar]. Kept here (not tied
 * to any rich-text library) so a future editor integration can map these
 * onto its own alignment enum with a single `when` at the call site.
 */
internal enum class RichTextAlign { LEFT, CENTER, RIGHT }

/**
 * UI-ONLY formatting toolbar for the A4 preview page — horizontally
 * scrollable, MD3 Expressive pill buttons. Deliberately has NO rich-text
 * logic, no markdown parsing, no editor library wired in: every action is
 * a plain callback. When a real rich-text editor is added later, plug its
 * calls into these callbacks (e.g. `onBoldClick = { editor.toggleBold() }`)
 * — nothing in this file needs to change.
 *
 * `isBoldActive` / `isItalicActive` / `currentAlignment` are read-only
 * "is this currently on" flags for highlighting the active button — pass
 * whatever the future editor reports for the current selection. They
 * default to off/left so this composable works standalone today.
 */
@Composable
internal fun RichTextToolbar(
    isBoldActive: Boolean = false,
    isItalicActive: Boolean = false,
    currentAlignment: RichTextAlign = RichTextAlign.LEFT,
    onBoldClick: () -> Unit = {},
    onItalicClick: () -> Unit = {},
    onAlignClick: (RichTextAlign) -> Unit = {},
    onIncreaseFontSizeClick: () -> Unit = {},
    onDecreaseFontSizeClick: () -> Unit = {},
    onIncreaseIndentClick: () -> Unit = {},
    onDecreaseIndentClick: () -> Unit = {},
    onInsertTableClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ১. Text Style — Bold, Italic
            ToolbarTextButton(label = "B", bold = true, active = isBoldActive, onClick = onBoldClick)
            ToolbarTextButton(label = "I", italic = true, active = isItalicActive, onClick = onItalicClick)

            ToolbarDivider()

            // ২. Text Alignment — Left, Center, Right
            ToolbarIconButton(active = currentAlignment == RichTextAlign.LEFT, onClick = { onAlignClick(RichTextAlign.LEFT) }) {
                AlignGlyph(RichTextAlign.LEFT)
            }
            ToolbarIconButton(active = currentAlignment == RichTextAlign.CENTER, onClick = { onAlignClick(RichTextAlign.CENTER) }) {
                AlignGlyph(RichTextAlign.CENTER)
            }
            ToolbarIconButton(active = currentAlignment == RichTextAlign.RIGHT, onClick = { onAlignClick(RichTextAlign.RIGHT) }) {
                AlignGlyph(RichTextAlign.RIGHT)
            }

            ToolbarDivider()

            // ৩. Font Size — A− / A+
            ToolbarTextButton(label = "A−", onClick = onDecreaseFontSizeClick)
            ToolbarTextButton(label = "A+", onClick = onIncreaseFontSizeClick)

            ToolbarDivider()

            // ৪. Indentation — decrease / increase
            ToolbarIconButton(onClick = onDecreaseIndentClick) { IndentGlyph(increase = false) }
            ToolbarIconButton(onClick = onIncreaseIndentClick) { IndentGlyph(increase = true) }

            ToolbarDivider()

            // ৫. Table
            ToolbarIconButton(onClick = onInsertTableClick) { TableGlyph() }
        }
    }
}

@Composable
private fun ToolbarDivider() {
    HorizontalDivider(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

/** Shared pill container for every toolbar button — active state gets a tonal fill. */
@Composable
private fun ToolbarButtonContainer(
    active: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (active) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun ToolbarTextButton(
    label: String,
    bold: Boolean = false,
    italic: Boolean = false,
    active: Boolean = false,
    onClick: () -> Unit
) {
    ToolbarButtonContainer(active = active, onClick = onClick) {
        Text(
            text = label,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            color = if (active) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun ToolbarIconButton(
    active: Boolean = false,
    onClick: () -> Unit,
    glyph: @Composable () -> Unit
) {
    ToolbarButtonContainer(active = active, onClick = onClick, content = glyph)
}

/** Three short bars mimicking a standard text-align icon — no icon-pack dependency needed. */
@Composable
private fun AlignGlyph(align: RichTextAlign) {
    val color = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = Modifier.size(20.dp, 14.dp)) {
        val barHeight = 2.dp.toPx()
        val gap = (size.height - barHeight * 3) / 2f
        val widths = listOf(size.width, size.width * 0.65f, size.width * 0.85f)
        widths.forEachIndexed { i, w ->
            val startX = when (align) {
                RichTextAlign.LEFT -> 0f
                RichTextAlign.CENTER -> (size.width - w) / 2f
                RichTextAlign.RIGHT -> size.width - w
            }
            val y = i * (barHeight + gap) + barHeight / 2f
            drawLine(
                color = color,
                start = Offset(startX, y),
                end = Offset(startX + w, y),
                strokeWidth = barHeight,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

/** A vertical bar + arrow, mimicking increase/decrease indent icons. */
@Composable
private fun IndentGlyph(increase: Boolean) {
    val color = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = Modifier.size(20.dp, 16.dp)) {
        val lineX = if (increase) 4.dp.toPx() else size.width - 4.dp.toPx()
        drawLine(
            color = color,
            start = Offset(lineX, 0f),
            end = Offset(lineX, size.height),
            strokeWidth = 2.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        // arrow shaft
        val arrowStartX = if (increase) lineX + 3.dp.toPx() else lineX - 3.dp.toPx()
        val arrowEndX = if (increase) size.width else 0f
        val midY = size.height / 2f
        drawLine(
            color = color,
            start = Offset(arrowStartX, midY),
            end = Offset(arrowEndX, midY),
            strokeWidth = 2.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

/** Simple 2x2 grid, mimicking a table-insert icon. */
@Composable
private fun TableGlyph() {
    val color = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = Modifier.size(18.dp)) {
        drawRect(color = color, style = Stroke(width = 1.5.dp.toPx()))
        drawLine(color, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), strokeWidth = 1.5.dp.toPx())
        drawLine(color, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), strokeWidth = 1.5.dp.toPx())
    }
}
