package com.mumtahin.ui.screens

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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Bottom sheet shared by "শব্দার্থ", "বাক্য তৈরি" and "বিপরীত শব্দ" — any
 * question that's a title + a growing list of single-word entries + marks.
 * `typeTitle` picks the sheet's heading and question-field hint.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WordListBottomSheet(
    typeTitle: String,
    initialQuestion: SavedQuestion.WordList?,
    onDismiss: () -> Unit,
    onSave: (questionText: String, words: List<WordItem>, marks: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var questionText by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var marks by remember { mutableStateOf(initialQuestion?.marks ?: "") }
    var words by remember {
        mutableStateOf(
            initialQuestion?.words ?: listOf(WordItem(id = 0L, word = ""))
        )
    }
    var nextWordId by remember { mutableStateOf((words.maxOfOrNull { it.id } ?: 0L) + 1) }

    // Auto-focus a freshly added word field.
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
                text = typeTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = if (initialQuestion != null) {
                    "প্রশ্ন সম্পাদনা করুন"
                } else {
                    "প্রশ্ন ও শব্দ যোগ করুন"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AppTextField(
                label = "প্রশ্ন",
                value = questionText,
                onValueChange = { questionText = it },
                placeholder = when (typeTitle) {
                    "শব্দার্থ" -> "যেমন: শব্দার্থ লেখ। যেকোনো ১২টি:"
                    "বাক্য তৈরি" -> "যেমন: নিচের শব্দগুলো দিয়ে বাক্য তৈরি কর:"
                    "বিপরীত শব্দ" -> "যেমন: বিপরীত শব্দ লেখো:"
                    else -> "প্রশ্ন লিখুন"
                }
            )

            Text(
                text = "শব্দসমূহ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            words.chunked(2).forEach { rowWords ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowWords.forEach { wordItem ->
                        val focusRequester = focusRequesters.getOrPut(wordItem.id) { FocusRequester() }

                        WordItemField(
                            wordItem = wordItem,
                            canRemove = words.size > 1,
                            focusRequester = focusRequester,
                            onWordChange = { newWord ->
                                words = words.map {
                                    if (it.id == wordItem.id) it.copy(word = newWord) else it
                                }
                            },
                            onRemove = {
                                words = words.filterNot { it.id == wordItem.id }
                                focusRequesters.remove(wordItem.id)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowWords.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    val newId = nextWordId
                    words = words + WordItem(id = newId, word = "")
                    nextWordId += 1
                    newlyAddedId = newId
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("+ আরও শব্দ যোগ করুন")
            }

            AppTextField(
                label = "মার্ক",
                value = marks,
                onValueChange = { marks = it },
                placeholder = "যেমন: ১২"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    dismissSheet {
                        val nonEmptyWords = words.filter { it.word.isNotBlank() }
                        onSave(questionText, nonEmptyWords, marks)
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
private fun WordItemField(
    wordItem: WordItem,
    canRemove: Boolean,
    focusRequester: FocusRequester,
    onWordChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = wordItem.word,
        onValueChange = onWordChange,
        modifier = modifier.focusRequester(focusRequester),
        label = { Text("শব্দ") },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        trailingIcon = {
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
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
