package com.mumtahin.ui.screens

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
    data class Poem(val editing: SavedQuestion.Poem?) : ActiveSheet()
    data class WordList(val typeTitle: String, val editing: SavedQuestion.WordList?) : ActiveSheet()
    data class FillBlanks(val editing: SavedQuestion.FillBlanks?) : ActiveSheet()
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
 * and one file per bottom sheet (PoemQuestionBottomSheet.kt,
 * WordListBottomSheet.kt, FillBlanksBottomSheet.kt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreen(
    subjectName: String,
    onBackClick: () -> Unit,
    onPreviewClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var savedQuestions by remember { mutableStateOf(listOf<SavedQuestion>()) }
    var activeSheet by remember { mutableStateOf<ActiveSheet?>(null) }

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
                    IconButton(onClick = onPreviewClick) {
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
            ExamInfoSection(subjectName = subjectName)

            if (savedQuestions.isNotEmpty()) {
                SavedQuestionsSection(
                    questions = savedQuestions,
                    onEdit = { question ->
                        activeSheet = when (question) {
                            is SavedQuestion.Poem -> ActiveSheet.Poem(editing = question)
                            is SavedQuestion.WordList -> ActiveSheet.WordList(
                                typeTitle = question.typeTitle,
                                editing = question
                            )
                            is SavedQuestion.FillBlanks -> ActiveSheet.FillBlanks(editing = question)
                        }
                    },
                    onDelete = { question ->
                        savedQuestions = savedQuestions.filterNot { it.id == question.id }
                    }
                )
            }

            QuestionTypesSection(
                addedQuestionTypes = savedQuestions.mapNotNull {
                    when (it) {
                        is SavedQuestion.Poem -> "কবিতা"
                        is SavedQuestion.WordList -> it.typeTitle
                        is SavedQuestion.FillBlanks -> "শূন্যস্থান"
                    }
                }.toSet(),
                onTypeClick = { typeTitle ->
                    activeSheet = when (typeTitle) {
                        "কবিতা" -> ActiveSheet.Poem(editing = null)
                        "শব্দার্থ", "বাক্য তৈরি", "বিপরীত শব্দ" -> ActiveSheet.WordList(
                            typeTitle = typeTitle,
                            editing = null
                        )
                        "শূন্যস্থান" -> ActiveSheet.FillBlanks(editing = null)
                        else -> null
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    when (val sheet = activeSheet) {
        is ActiveSheet.Poem -> {
            PoemQuestionBottomSheet(
                initialQuestion = sheet.editing,
                onDismiss = { activeSheet = null },
                onSave = { questionText, marks ->
                    val editingId = sheet.editing?.id
                    savedQuestions = if (editingId != null) {
                        savedQuestions.map {
                            if (it.id == editingId && it is SavedQuestion.Poem) {
                                it.copy(questionText = questionText, marks = marks)
                            } else {
                                it
                            }
                        }
                    } else {
                        savedQuestions + SavedQuestion.Poem(
                            id = System.currentTimeMillis(),
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
        null -> Unit
    }
}
