package com.mumtahin.ui.subject

import com.mumtahin.data.ExamInfo
import com.mumtahin.data.SavedQuestion
import com.mumtahin.ui.components.StatusBarColor
import com.mumtahin.ui.preview.QuestionPreviewScreen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mumtahin.R
import com.mumtahin.data.SubjectRepository
import com.mumtahin.data.SubjectViewModel

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
 *
 * MVVM: all persisted data (exam info, saved questions, whether the exam-info
 * card is collapsed) lives in [SubjectViewModel] / [com.mumtahin.data.SubjectData],
 * backed by DataStore via [SubjectRepository] — it survives process death and
 * app restarts. This Composable only reads `uiState` and calls the
 * ViewModel's functions; it never mutates the question list or ExamInfo
 * directly. `activeSheet` and `isPreviewMode` are transient UI-only
 * navigation state (which sheet is open, preview vs. edit) — those don't
 * need persisting, so they stay as local `remember` state here.
 *
 * The rest of this screen lives in sibling files in this package
 * (com.mumtahin.ui.subject): ExamInfoSection.kt, SavedQuestionsSection.kt,
 * QuestionTypesSection.kt, and one file per bottom sheet
 * (SingleQuestionBottomSheet.kt — কবিতা/প্রশ্ন,
 * WordListBottomSheet.kt — শব্দার্থ/বাক্য তৈরি/বিপরীত শব্দ,
 * FillBlanksBottomSheet.kt — শূন্যস্থান, ShortQuestionsBottomSheet.kt — সংক্ষিপ্ত প্রশ্ন,
 * TrueFalseBottomSheet.kt — ঠিক চিহ্ন, MathProblemBottomSheet.kt — অংক).
 * Models (SavedQuestion, ExamInfo) live in com.mumtahin.data;
 * AppTextField (shared input style) lives in com.mumtahin.ui.components.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreen(
    subjectName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appContext = LocalContext.current.applicationContext
    val repository = remember(appContext) { SubjectRepository(appContext) }
    val viewModel: SubjectViewModel = viewModel(
        key = "subject_$subjectName", // distinct ViewModel instance per subject
        factory = SubjectViewModel.Factory(subjectName, repository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val savedQuestions = uiState.savedQuestions
    val examInfo = uiState.examInfo
    val examInfoExpanded = uiState.examInfoExpanded

    // Same color as this screen's TopAppBar (both the edit-mode one below
    // and QuestionPreviewScreen's) — covers both since this composable
    // stays mounted across the isPreviewMode toggle.
    StatusBarColor(MaterialTheme.colorScheme.primary)

    var activeSheet by remember { mutableStateOf<ActiveSheet?>(null) }
    var isPreviewMode by remember { mutableStateOf(false) }

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
                        IconButton(onClick = { isPreviewMode = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_preview),
                                contentDescription = "Preview"
                            )
                        }
                    },
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
                    onExamInfoChange = { viewModel.updateExamInfo(it) },
                    expanded = examInfoExpanded,
                    onExpandedChange = { viewModel.setExamInfoExpanded(it) }
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
                        onDelete = { question -> viewModel.deleteQuestion(question.id) }
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

        // Every onSave below just builds the new/updated SavedQuestion and
        // hands it to viewModel.upsertQuestion — the "is this an add or an
        // edit" branching now lives once, in the ViewModel, instead of
        // being repeated in each bottom sheet's callback like before.
        when (val sheet = activeSheet) {
            is ActiveSheet.SingleQuestion -> {
                SingleQuestionBottomSheet(
                    typeTitle = sheet.typeTitle,
                    subjectName = subjectName,
                    initialQuestion = sheet.editing,
                    onDismiss = { activeSheet = null },
                    onSave = { questionText, marks ->
                        viewModel.upsertQuestion(
                            SavedQuestion.SingleQuestion(
                                id = sheet.editing?.id ?: System.currentTimeMillis(),
                                typeTitle = sheet.typeTitle,
                                questionText = questionText,
                                marks = marks
                            )
                        )
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
                        viewModel.upsertQuestion(
                            SavedQuestion.WordList(
                                id = sheet.editing?.id ?: System.currentTimeMillis(),
                                typeTitle = sheet.typeTitle,
                                questionText = questionText,
                                words = words,
                                marks = marks
                            )
                        )
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
                        viewModel.upsertQuestion(
                            SavedQuestion.FillBlanks(
                                id = sheet.editing?.id ?: System.currentTimeMillis(),
                                questionText = questionText,
                                subQuestions = subQuestions,
                                marks = marks
                            )
                        )
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
                        viewModel.upsertQuestion(
                            SavedQuestion.ShortQuestions(
                                id = sheet.editing?.id ?: System.currentTimeMillis(),
                                questionText = questionText,
                                subQuestions = subQuestions,
                                marks = marks
                            )
                        )
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
                        viewModel.upsertQuestion(
                            SavedQuestion.TrueFalse(
                                id = sheet.editing?.id ?: System.currentTimeMillis(),
                                questionText = questionText,
                                statements = statements,
                                marks = marks
                            )
                        )
                        activeSheet = null
                    }
                )
            }
            is ActiveSheet.MathProblem -> {
                MathProblemBottomSheet(
                    initialQuestion = sheet.editing,
                    onDismiss = { activeSheet = null },
                    onSave = { questionText, layout, problems, marks ->
                        viewModel.upsertQuestion(
                            SavedQuestion.MathProblem(
                                id = sheet.editing?.id ?: System.currentTimeMillis(),
                                questionText = questionText,
                                layout = layout,
                                problems = problems,
                                marks = marks
                            )
                        )
                        activeSheet = null
                    }
                )
            }
            null -> Unit
        }
    }
}
