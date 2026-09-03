package com.mumtahin.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mumtahin.R

/**
 * Compact list of already-added questions, shown between Exam Information
 * and the Question Types grid. Works for any SavedQuestion subtype so more
 * question types can be added here later.
 */
@Composable
internal fun SavedQuestionsSection(
    questions: List<SavedQuestion>,
    onEdit: (SavedQuestion) -> Unit,
    onDelete: (SavedQuestion) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "যোগ করা প্রশ্ন",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )

        questions.forEach { question ->
            SavedQuestionCard(
                question = question,
                onEdit = { onEdit(question) },
                onDelete = { onDelete(question) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SavedQuestionCard(
    question: SavedQuestion,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val questionText = when (question) {
        is SavedQuestion.Poem -> question.questionText
        is SavedQuestion.WordList -> question.questionText
        is SavedQuestion.FillBlanks -> question.questionText
    }
    val metaText = when (question) {
        is SavedQuestion.Poem -> "মার্ক: ${question.marks}"
        is SavedQuestion.WordList ->
            "ধরন: ${question.typeTitle}  ·  মোট শব্দ: ${question.words.size}  ·  মার্ক: ${question.marks}"
        is SavedQuestion.FillBlanks ->
            "মোট শূন্যস্থান: ${question.subQuestions.size}  ·  মার্ক: ${question.marks}"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = questionText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = metaText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
