package com.mumtahin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mumtahin.R

/**
 * Opened by tapping the preview (eye) icon on SubjectScreen's toolbar.
 * Renders the exam info + saved questions inside a canvas locked to the
 * real A4 aspect ratio (210mm : 297mm), floating on a neutral backdrop —
 * like an actual sheet of paper. The pencil icon in this screen's toolbar
 * returns to SubjectScreen (Exam Information + প্রশ্নের ধরন); there's no
 * separate back arrow, since the pencil is the only way back by design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuestionPreviewScreen(
    subjectName: String,
    examInfo: ExamInfo,
    savedQuestions: List<SavedQuestion>,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("$subjectName — প্রিভিউ") },
                navigationIcon = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        // Neutral backdrop (like a document editor canvas) so the white
        // A4 page reads clearly as a distinct sheet of paper.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 20.dp, horizontal = 16.dp)
        ) {
            A4PageCanvas(examInfo = examInfo, savedQuestions = savedQuestions)
        }
    }
}

/**
 * A page locked to the real A4 ratio (width:height = 210:297 = 1:√2) via
 * `aspectRatio`, so it stays proportionally accurate on any screen width.
 * Its own content scrolls internally if it overflows one page's height.
 */
@Composable
private fun A4PageCanvas(examInfo: ExamInfo, savedQuestions: List<SavedQuestion>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(210f / 297f), // exact A4 portrait ratio
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(2.dp), // real paper has near-sharp corners
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp) // page margin
        ) {
            PageHeader(examInfo = examInfo)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(16.dp))

            if (savedQuestions.isEmpty()) {
                Text(
                    text = "এখনো কোনো প্রশ্ন যোগ করা হয়নি",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            } else {
                savedQuestions.forEachIndexed { index, question ->
                    if (question is SavedQuestion.MathProblem) {
                        MathProblemPreviewRow(number = index + 1, question = question)
                    } else {
                        PreviewQuestionRow(number = index + 1, question = question)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun PageHeader(examInfo: ExamInfo) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (examInfo.madrasaName.isNotBlank()) {
            Text(
                text = examInfo.madrasaName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (examInfo.examName.isNotBlank()) {
            Text(
                text = examInfo.examName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // বিষয় / শ্রেণী / সময় / পূর্ণমান — one row, like a real question paper.
        Column(modifier = Modifier.fillMaxWidth()) {
            if (examInfo.subject.isNotBlank()) {
                MetaLine(label = "বিষয়", value = examInfo.subject)
            }
            if (examInfo.className.isNotBlank()) {
                MetaLine(label = "শ্রেণী", value = examInfo.className)
            }
            if (examInfo.duration.isNotBlank() || examInfo.fullMarks.isNotBlank()) {
                Text(
                    text = listOfNotNull(
                        examInfo.duration.takeIf { it.isNotBlank() }?.let { "সময়: $it" },
                        examInfo.fullMarks.takeIf { it.isNotBlank() }?.let { "পূর্ণমান: $it" }
                    ).joinToString("      "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MetaLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun PreviewQuestionRow(number: Int, question: SavedQuestion) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$number. ${previewQuestionText(question)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        previewSubItems(question)?.let { items ->
            Column(modifier = Modifier.padding(top = 6.dp, start = 16.dp)) {
                items.forEachIndexed { i, text ->
                    Text(
                        text = "${ordinalLabel(i)}) $text",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }

        Text(
            text = "[মার্ক: ${question.marks}]",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun previewQuestionText(question: SavedQuestion): String = when (question) {
    is SavedQuestion.SingleQuestion -> question.questionText
    is SavedQuestion.WordList -> question.questionText
    is SavedQuestion.FillBlanks -> question.questionText
    is SavedQuestion.ShortQuestions -> question.questionText
    is SavedQuestion.TrueFalse -> question.questionText
    is SavedQuestion.MathProblem -> question.questionText
}

/** Sub-list to render under the question text, if the type has one. */
private fun previewSubItems(question: SavedQuestion): List<String>? = when (question) {
    is SavedQuestion.WordList -> question.words.map { it.word }.filter { it.isNotBlank() }
    is SavedQuestion.FillBlanks -> question.subQuestions
    is SavedQuestion.ShortQuestions -> question.subQuestions
    is SavedQuestion.TrueFalse -> question.statements
    is SavedQuestion.MathProblem -> null // rendered separately by MathProblemPreviewRow
    is SavedQuestion.SingleQuestion -> null
}

/**
 * গাণিতিক সমস্যা renders as a 2-per-row grid of either column-form
 * (উপর-নিচে) or inline (পাশাপাশি) problems, based on `question.layout`.
 */
@Composable
private fun MathProblemPreviewRow(number: Int, question: SavedQuestion.MathProblem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$number. ${question.questionText}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(10.dp))

        question.problems.chunked(2).forEach { rowEntries ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                rowEntries.forEach { entry ->
                    if (question.layout == MathLayout.VERTICAL) {
                        VerticalMathEntry(entry)
                    } else {
                        HorizontalMathEntry(entry)
                    }
                }
            }
        }

        Text(
            text = "[মার্ক: ${question.marks}]",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

/** Traditional column form: operand1 on top, operator+operand2 below, line, then blank space for the answer. */
@Composable
private fun VerticalMathEntry(entry: MathProblemEntry) {
    Column(horizontalAlignment = Alignment.End) {
        Text(entry.operand1, style = MaterialTheme.typography.titleMedium, color = Color.Black)
        Row {
            Text(
                "${entry.operator} ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(entry.operand2, style = MaterialTheme.typography.titleMedium, color = Color.Black)
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp).width(60.dp),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(28.dp)) // answer space
    }
}

/** Inline form: "operand1 operator operand2 = ____". */
@Composable
private fun HorizontalMathEntry(entry: MathProblemEntry) {
    Text(
        text = "${entry.operand1} ${entry.operator} ${entry.operand2} = _______",
        style = MaterialTheme.typography.titleMedium,
        color = Color.Black
    )
}
