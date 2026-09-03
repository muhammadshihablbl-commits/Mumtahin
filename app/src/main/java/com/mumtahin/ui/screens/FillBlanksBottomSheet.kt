package com.mumtahin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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

private val banglaOrdinalLabels = listOf(
    "ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ",
    "ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন",
    "প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ",
    "স", "হ"
)

private fun ordinalLabel(index: Int): String =
    banglaOrdinalLabels.getOrNull(index) ?: (index + 1).toString()

/**
 * Bottom sheet opened from the "শূন্যস্থান" question-type card. Each
 * sub-question is prefixed ক), খ), গ)... and has a built-in "___" button
 * that inserts a blank at the current cursor position, plus a remove "✕".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FillBlanksBottomSheet(
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
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "শূন্যস্থান",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = if (initialQuestion != null) {
                    "প্রশ্ন সম্পাদনা করুন"
                } else {
                    "প্রশ্ন ও শূন্যস্থান যোগ করুন"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AppTextField(
                label = "প্রশ্ন",
                value = questionText,
                onValueChange = { questionText = it },
                placeholder = "যেমন: শূন্যস্থান পূরণ করো"
            )

            Text(
                text = "শূন্যস্থান বাক্য",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            blanks.forEachIndexed { index, item ->
                val focusRequester = focusRequesters.getOrPut(item.id) { FocusRequester() }
                BlankSubQuestionRow(
                    label = ordinalLabel(index),
                    item = item,
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
                shape = MaterialTheme.shapes.medium
            ) {
                Text("+ আরও প্রশ্ন যোগ করুন")
            }

            AppTextField(
                label = "মার্ক",
                value = marks,
                onValueChange = { marks = it },
                placeholder = "যেমন: ১০"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    dismissSheet {
                        val nonEmpty = blanks.map { it.value.text }.filter { it.isNotBlank() }
                        onSave(questionText, nonEmpty, marks)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun BlankSubQuestionRow(
    label: String,
    item: BlankItem,
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
        Text(
            text = "$label)",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )

        OutlinedTextField(
            value = item.value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = { Text("যেমন: আল্লাহ আমার____") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
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
