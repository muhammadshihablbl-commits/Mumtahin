package com.mumtahin.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mumtahin.R

/** Which bottom sheet is currently open, and what (if anything) it's editing. */
private sealed class ActiveSheet {
    data class SingleQuestion(val typeTitle: String, val editing: SavedQuestion.SingleQuestion?) : ActiveSheet()
    data class WordList(val typeTitle: String, val editing: SavedQuestion.WordList?) : ActiveSheet()
    data class FillBlanks(val editing: SavedQuestion.FillBlanks?) : ActiveSheet()
    data class ShortQuestions(val editing: SavedQuestion.ShortQuestions?) : ActiveSheet()
    data class TrueFalse(val editing: SavedQuestion.TrueFalse?) : ActiveSheet()
    data class MathProblem(val editing: SavedQuestion.MathProblem?) : ActiveSheet()
}

/**
 * Opened when a subject is tapped on the Home screen.
 * Shows a toolbar (title + preview action), a collapsible "Exam Information"
 * card, the list of saved questions, and a "প্রশ্নের ধরন" (Question Types) grid.
 * More question-editing content will be added under this later.
 *
 * The rest of this screen lives in sibling files in this package:
 * SavedQuestion.kt (models), AppTextField.kt (shared input style),
 * ExamInfoSection.kt, SavedQuestionsSection.kt, QuestionTypesSection.kt,
 * and one file per bottom sheet (SingleQuestionBottomSheet.kt — কবিতা/প্রশ্ন,
 * WordListBottomSheet.kt — শব্দার্থ/বাক্য তৈরি/বিপরীত শব্দ,
 * FillBlanksBottomSheet.kt — শূন্যস্থান, ShortQuestionsBottomSheet.kt — সংক্ষিপ্ত প্রশ্ন).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreen(
    subjectName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var savedQuestions by remember { mutableStateOf(listOf<SavedQuestion>()) }
    var activeSheet by remember { mutableStateOf<ActiveSheet?>(null) }
    var isPreviewMode by remember { mutableStateOf(false) }
    var examInfo by remember { mutableStateOf(ExamInfo(subject = subjectName)) }

    BackHandler(enabled = isPreviewMode) {
        isPreviewMode = false
    }

    if (isPreviewMode) {
        QuestionPreviewScreen(
            subjectName = subjectName,
            examInfo = examInfo,
            savedQuestions = savedQuestions,
            onEditClick = { isPreviewMode = false },
            modifier = modifier
        )
    } else {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { Text(subjectName) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Eye / Preview icon.
                        // NOTE: Add a drawable named "ic_preview" to res/drawable.
                        IconButton(onClick = { isPreviewMode = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_preview),
                                contentDescription = "Preview"
                            )
                        }
                    },
                    // Status bar is set to colorScheme.primary in Theme.kt for the
                    // whole app; matching the toolbar to the same color here keeps
                    // them looking like one seamless band.
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                ExamInfoSection(
                    examInfo = examInfo,
                    onExamInfoChange = { examInfo = it }
                )

                if (savedQuestions.isNotEmpty()) {
                    SavedQuestionsSection(
                        questions = savedQuestions,
                        onEdit = { question ->
                            activeSheet = when (question) {
                                is SavedQuestion.SingleQuestion -> ActiveSheet.SingleQuestion(
                                    typeTitle = question.typeTitle,
                                    editing = question
                                )
                                is SavedQuestion.WordList -> ActiveSheet.WordList(
                                    typeTitle = question.typeTitle,
                                    editing = question
                                )
                                is SavedQuestion.FillBlanks -> ActiveSheet.FillBlanks(editing = question)
                                is SavedQuestion.ShortQuestions -> ActiveSheet.ShortQuestions(editing = question)
                                is SavedQuestion.TrueFalse -> ActiveSheet.TrueFalse(editing = question)
                                is SavedQuestion.MathProblem -> ActiveSheet.MathProblem(editing = question)
                            }
                        },
                        onDelete = { question ->
                            savedQuestions = savedQuestions.filterNot { it.id == question.id }
                        }
                    )
                }

                QuestionTypesSection(
                    subjectName = subjectName,
                    addedQuestionTypes = savedQuestions.mapNotNull {
                        when (it) {
                            is SavedQuestion.SingleQuestion -> it.typeTitle
                            is SavedQuestion.WordList -> it.typeTitle
                            is SavedQuestion.FillBlanks -> "শূন্যস্থান"
                            is SavedQuestion.ShortQuestions -> "সংক্ষিপ্ত প্রশ্ন"
                            is SavedQuestion.TrueFalse -> "ঠিক চিহ্ন"
                            is SavedQuestion.MathProblem -> "অংক"
                        }
                    }.toSet(),
                    onTypeClick = { typeTitle ->
                        activeSheet = when (typeTitle) {
                            "কবিতা", "প্রশ্ন" -> ActiveSheet.SingleQuestion(typeTitle = typeTitle, editing = null)
                            "শব্দার্থ", "বাক্য তৈরি", "বিপরীত শব্দ" -> ActiveSheet.WordList(
                                typeTitle = typeTitle,
                                editing = null
                            )
                            "শূন্যস্থান" -> ActiveSheet.FillBlanks(editing = null)
                            "সংক্ষিপ্ত প্রশ্ন" -> ActiveSheet.ShortQuestions(editing = null)
                            "ঠিক চিহ্ন" -> ActiveSheet.TrueFalse(editing = null)
                            "অংক" -> ActiveSheet.MathProblem(editing = null)
                            else -> null
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        when (val sheet = activeSheet) {
            is ActiveSheet.SingleQuestion -> {
                SingleQuestionBottomSheet(
                    typeTitle = sheet.typeTitle,
                    subjectName = subjectName,
                    initialQuestion = sheet.editing,
                    onDismiss = { activeSheet = null },
                    onSave = { questionText, marks ->
                        val editingId = sheet.editing?.id
                        savedQuestions = if (editingId != null) {
                            savedQuestions.map {
                                if (it.id == editingId && it is SavedQuestion.SingleQuestion) {
                                    it.copy(questionText = questionText, marks = marks)
                                } else {
                                    it
                                }
                            }
                        } else {
                            savedQuestions + SavedQuestion.SingleQuestion(
                                id = System.currentTimeMillis(),
                                typeTitle = sheet.typeTitle,
                                questionText = questionText,
                                marks = marks
                            )
                        }
                        activeSheet = null
                    }
                )
            }
            is ActiveSheet.WordList -> {
                WordListBottomSheet(
                    typeTitle = sheet.typeTitle,
                    initialQuestion = sheet.editing,
                    onDismiss = { activeSheet = null },
                    onSave = { questionText, words, marks ->
                        val editingId = sheet.editing?.id
                        savedQuestions = if (editingId != null) {
                            savedQuestions.map {
                                if (it.id == editingId && it is SavedQuestion.WordList) {
                                    it.copy(
                                        questionText = questionText,
                                        words = words,
                                        marks = marks
                                    )
                                } else {
                                    it
                                }
                            }
                        } else {
                            savedQuestions + SavedQuestion.WordList(
                                id = System.currentTimeMillis(),
                                typeTitle = sheet.typeTitle,
                                questionText = questionText,
                                words = words,
                                marks = marks
                            )
                        }
                        activeSheet = null
                    }
                )
            }
            is ActiveSheet.FillBlanks -> {
                FillBlanksBottomSheet(
                    subjectName = subjectName,
                    initialQuestion = sheet.editing,
                    onDismiss = { activeSheet = null },
                    onSave = { questionText, subQuestions, marks ->
                        val editingId = sheet.editing?.id
                        savedQuestions = if (editingId != null) {
                            savedQuestions.map {
                                if (it.id == editingId && it is SavedQuestion.FillBlanks) {
                                    it.copy(
                                        questionText = questionText,
                                        subQuestions = subQuestions,
                                        marks = marks
                                    )
                                } else {
                                    it
                                }
                            }
                        } else {
                            savedQuestions + SavedQuestion.FillBlanks(
                                id = System.currentTimeMillis(),
                                questionText = questionText,
                                subQuestions = subQuestions,
                                marks = marks
                            )
                        }
                        activeSheet = null
                    }
                )
            }
            is ActiveSheet.ShortQuestions -> {
                ShortQuestionsBottomSheet(
                    subjectName = subjectName,
                    initialQuestion = sheet.editing,
                    onDismiss = { activeSheet = null },
                    onSave = { questionText, subQuestions, marks ->
                        val editingId = sheet.editing?.id
                        savedQuestions = if (editingId != null) {
                            savedQuestions.map {
                                if (it.id == editingId && it is SavedQuestion.ShortQuestions) {
                                    it.copy(
                                        questionText = questionText,
                                        subQuestions = subQuestions,
                                        marks = marks
                                    )
                                } else {
                                    it
                                }
                            }
                        } else {
                            savedQuestions + SavedQuestion.ShortQuestions(
                                id = System.currentTimeMillis(),
                                questionText = questionText,
                                subQuestions = subQuestions,
                                marks = marks
                            )
                        }
                        activeSheet = null
                    }
                )
            }
            
            is ActiveSheet.TrueFalse -> {
                TrueFalseBottomSheet(
                    subjectName = subjectName,
                    initialQuestion = sheet.editing,
                    onDismiss = { activeSheet = null },
                    onSave = { questionText, statements, marks ->
                        val editingId = sheet.editing?.id
                        savedQuestions = if (editingId != null) {
                            savedQuestions.map {
                                if (it.id == editingId && it is SavedQuestion.TrueFalse) {
                                    it.copy(questionText = questionText, statements = statements, marks = marks)
                                } else {
                                    it
                                }
                            }
                        } else {
                            savedQuestions + SavedQuestion.TrueFalse(
                                id = System.currentTimeMillis(),
                                questionText = questionText,
                                statements = statements,
                                marks = marks
                            )
                        }
                        activeSheet = null
                    }
                )
            }
            is ActiveSheet.MathProblem -> {
                MathProblemBottomSheet(
                    initialQuestion = sheet.editing,
                    onDismiss = { activeSheet = null },
                    onSave = { questionText, layout, problems, marks ->
                        val editingId = sheet.editing?.id
                        savedQuestions = if (editingId != null) {
                            savedQuestions.map {
                                if (it.id == editingId && it is SavedQuestion.MathProblem) {
                                    it.copy(questionText = questionText, layout = layout, problems = problems, marks = marks)
                                } else it
                            }
                        } else {
                            savedQuestions + SavedQuestion.MathProblem(
                                id = System.currentTimeMillis(),
                                questionText = questionText,
                                layout = layout,
                                problems = problems,
                                marks = marks
                            )
                        }
                        activeSheet = null
                    }
                )
            }
            null -> Unit
        }
    }
}
