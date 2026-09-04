package com.mumtahin.ui.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Bottom sheet shared by "কবিতা" and "প্রশ্ন" — a single question whose
 * answer is expected to be long, with no sub-questions. `typeTitle` picks
 * the sheet's heading and question-field hint.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SingleQuestionBottomSheet(
    typeTitle: String,
    initialQuestion: SavedQuestion.SingleQuestion?,
    onDismiss: () -> Unit,
    onSave: (questionText: String, marks: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var marks by remember { mutableStateOf(initialQuestion?.marks ?: "") }

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
                    "প্রশ্ন ও মার্ক লিখুন"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AppTextField(
                label = "প্রশ্ন",
                value = question,
                onValueChange = { question = it },
                placeholder = when (typeTitle) {
                    "কবিতা" -> "যেমন: প্রার্থনা কবিতার প্রথম ১২ লাইন সুন্দর করে লিখ"
                    "প্রশ্ন" -> "যেমন: রাসূলুল্লাহ (সা.)-এর জীবনী বর্ণনা কর"
                    else -> "প্রশ্ন লিখুন"
                },
                singleLine = false,
                minLines = 3
            )
            AppTextField(
                label = "মার্ক",
                value = marks,
                onValueChange = { marks = it },
                placeholder = "যেমন: ১০"
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { dismissSheet { onSave(question, marks) } },
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
