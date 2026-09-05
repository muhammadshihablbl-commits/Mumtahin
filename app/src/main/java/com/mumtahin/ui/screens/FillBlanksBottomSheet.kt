package com.mumtahin.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** One "ক) ..." fill-in-the-blank sub-question. */
private data class BlankItem(
    val id: Long,
    val value: TextFieldValue = TextFieldValue("")
)

/**
 * MD3 EXPRESSIVE VERSION
 *
 * Bottom sheet opened from the "শূন্যস্থান" question-type card. Each
 * sub-question is prefixed with a circular ক)/খ)/গ)... badge and has a
 * built-in "___" chip that inserts a blank at the current cursor position,
 * plus a remove "✕".
 *
 * Uses stable Material3 APIs only — no ExperimentalMaterial3ExpressiveApi
 * opt-in needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FillBlanksBottomSheet(
    subjectName: String,
    initialQuestion: SavedQuestion.FillBlanks?,
    onDismiss: () -> Unit,
    onSave: (questionText: String, subQuestions: List<String>, marks: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var questionText by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var marks by remember { mutableStateOf(initialQuestion?.marks ?: "") }
    var blanks by remember {
        mutableStateOf(
            (initialQuestion?.subQuestions?.takeIf { it.isNotEmpty() } ?: listOf(""))
                .mapIndexed { index, text -> BlankItem(id = index.toLong(), value = TextFieldValue(text)) }
        )
    }
    var nextBlankId by remember { mutableStateOf((blanks.maxOfOrNull { it.id } ?: 0L) + 1) }

    val focusRequesters = remember { mutableStateMapOf<Long, FocusRequester>() }
    var newlyAddedId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(newlyAddedId) {
        newlyAddedId?.let { id ->
            focusRequesters[id]?.requestFocus()
            newlyAddedId = null
        }
    }

    fun dismissSheet(afterHide: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) afterHide()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 24.dp)
        ) {
            FillBlanksHeroHeader(isEditing = initialQuestion != null)

            Spacer(modifier = Modifier.height(20.dp))

            AppTextField(
                label = "প্রশ্ন",
                value = questionText,
                onValueChange = { questionText = it },
                placeholder = "যেমন: শূন্যস্থান পূরণ করো"
            )

            Text(
                text = "শূন্যস্থান বাক্য",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp, top = 4.dp)
            )

            val blankPlaceholder = if (subjectName == "অংক") "যেমন: ৫ × ___ = ৩৫" else "যেমন: আল্লাহ আমার____"

            blanks.forEachIndexed { index, item ->
                val focusRequester = focusRequesters.getOrPut(item.id) { FocusRequester() }
                BlankSubQuestionRow(
                    label = ordinalLabel(index),
                    item = item,
                    placeholder = blankPlaceholder,
                    canRemove = blanks.size > 1,
                    focusRequester = focusRequester,
                    onValueChange = { newValue ->
                        blanks = blanks.map { if (it.id == item.id) it.copy(value = newValue) else it }
                    },
                    onInsertBlank = {
                        val current = item.value
                        val start = current.selection.start
                        val end = current.selection.end
                        val newText = current.text.replaceRange(start, end, "_____")
                        val newCursor = start + 5
                        blanks = blanks.map {
                            if (it.id == item.id) {
                                it.copy(value = TextFieldValue(newText, TextRange(newCursor)))
                            } else {
                                it
                            }
                        }
                    },
                    onRemove = {
                        blanks = blanks.filterNot { it.id == item.id }
                        focusRequesters.remove(item.id)
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    val newId = nextBlankId
                    blanks = blanks + BlankItem(id = newId, value = TextFieldValue(""))
                    nextBlankId += 1
                    newlyAddedId = newId
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = CircleShape
            ) {
                Text("+ আরও প্রশ্ন যোগ করুন", fontWeight = FontWeight.SemiBold)
            }

            AppTextField(
                label = "মার্ক",
                value = marks,
                onValueChange = { marks = it },
                placeholder = "যেমন: ১০"
            )

            Spacer(modifier = Modifier.height(8.dp))

            FillBlanksSaveButton(
                enabled = questionText.isNotBlank(),
                onClick = {
                    dismissSheet {
                        val nonEmpty = blanks.map { it.value.text }.filter { it.isNotBlank() }
                        onSave(questionText, nonEmpty, marks)
                    }
                }
            )
        }
    }
}

@Composable
private fun FillBlanksHeroHeader(isEditing: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "শূন্যস্থান",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isEditing) "প্রশ্ন সম্পাদনা করুন" else "প্রশ্ন ও শূন্যস্থান যোগ করুন",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun BlankSubQuestionRow(
    label: String,
    item: BlankItem,
    placeholder: String,
    canRemove: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (TextFieldValue) -> Unit,
    onInsertBlank: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Expressive: circular badge instead of plain "ক)" text.
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        OutlinedTextField(
            value = item.value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = { Text(placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Built-in blank inserter — taps in "_____" at the
                    // cursor so the user doesn't have to type underscores.
                    Text(
                        text = "___",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable { onInsertBlank() }
                    )
                    if (canRemove) {
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { onRemove() }
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

/** Save button with a bouncy spring-based shape morph on press. */
@Composable
private fun FillBlanksSaveButton(enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fillBlanksSaveButtonShapeMorph"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = "সংরক্ষণ করুন",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
