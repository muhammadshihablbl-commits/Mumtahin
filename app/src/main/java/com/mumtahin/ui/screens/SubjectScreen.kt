package com.mumtahin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mumtahin.R
import kotlinx.coroutines.launch

private data class WordItem(
    val id: Long,
    val word: String
)

private sealed class SavedQuestion {
    abstract val id: Long
    abstract val marks: String

    data class Poem(
        override val id: Long,
        val questionText: String,
        override val marks: String
    ) : SavedQuestion()

    data class WordList(
        override val id: Long,
        val typeTitle: String,
        val questionText: String,
        val words: List<WordItem>,
        override val marks: String
    ) : SavedQuestion()
}

private sealed class ActiveSheet {
    data class Poem(val editing: SavedQuestion.Poem?) : ActiveSheet()
    data class WordList(val typeTitle: String, val editing: SavedQuestion.WordList?) : ActiveSheet()
}

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
                    IconButton(onClick = onPreviewClick) {
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
                    }
                }.toSet(),
                onTypeClick = { typeTitle ->
                    activeSheet = when (typeTitle) {
                        "কবিতা" -> ActiveSheet.Poem(editing = null)
                        "শব্দার্থ", "বাক্য তৈরি", "বিপরীত শব্দ" -> ActiveSheet.WordList(
                            typeTitle = typeTitle,
                            editing = null
                        )
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
        null -> Unit
    }
}

@Composable
private fun ExamInfoSection(subjectName: String) {
    var expanded by remember { mutableStateOf(true) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "examInfoArrowRotation"
    )

    var examName by remember { mutableStateOf("") }
    var madrasaName by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(subjectName) }
    var className by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var fullMarks by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Exam Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "পরীক্ষা সম্পর্কিত তথ্য দেখতে ট্যাপ করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(arrowRotation)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    label = "পরীক্ষার নাম",
                    value = examName,
                    onValueChange = { examName = it },
                    placeholder = "যেমন: অর্ধবার্ষিক পরীক্ষা"
                )
                AppTextField(
                    label = "মাদ্রাসার নাম",
                    value = madrasaName,
                    onValueChange = { madrasaName = it },
                    placeholder = "মাদ্রাসার নাম লিখুন"
                )
                AppTextField(
                    label = "বিষয়",
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = "যেমন: বাংলা"
                )
                AppTextField(
                    label = "শ্রেণী",
                    value = className,
                    onValueChange = { className = it },
                    placeholder = "যেমন: ৬ষ্ঠ"
                )
                AppTextField(
                    label = "সময়",
                    value = duration,
                    onValueChange = { duration = it },
                    placeholder = "যেমন: ২ ঘণ্টা"
                )
                AppTextField(
                    label = "পূর্ণমান",
                    value = fullMarks,
                    onValueChange = { fullMarks = it },
                    placeholder = "যেমন: ১০০"
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save")
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        minLines = minLines,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun SavedQuestionsSection(
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
    }
    val metaText = when (question) {
        is SavedQuestion.Poem -> "মার্ক: ${question.marks}"
        is SavedQuestion.WordList ->
            "ধরন: ${question.typeTitle}  ·  মোট শব্দ: ${question.words.size}  ·  মার্ক: ${question.marks}"
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

private val questionTypes = listOf(
    "কবিতা",
    "শব্দার্থ",
    "বাক্য তৈরি",
    "বিপরীত শব্দ",
    "শূন্যস্থান",
    "সংক্ষিপ্ত প্রশ্ন",
    "প্রশ্ন",
    "ঠিক চিহ্ন",
    "Table"
)

@Composable
private fun QuestionTypesSection(
    addedQuestionTypes: Set<String>,
    onTypeClick: (String) -> Unit
) {
    var selectedTypes by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "প্রশ্নের ধরন",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )

        questionTypes.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { type ->
                    val isHandledType = type in listOf("কবিতা", "শব্দার্থ", "বাক্য তৈরি", "বিপরীত শব্দ")
                    val isSelected = if (isHandledType) {
                        type in addedQuestionTypes
                    } else {
                        type in selectedTypes
                    }

                    val onClickAction = {
                        if (isHandledType) {
                            onTypeClick(type)
                        } else {
                            selectedTypes = if (type in selectedTypes) {
                                selectedTypes - type
                            } else {
                                selectedTypes + type
                            }
                        }
                    }

                    QuestionTypeCard(
                        label = type,
                        selected = isSelected,
                        onClick = onClickAction,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuestionTypeCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "questionTypeCardColor"
    )
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.medium,
        border = if (selected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoemQuestionBottomSheet(
    initialQuestion: SavedQuestion.Poem?,
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
                text = "কবিতা",
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
                placeholder = "যেমন: প্রার্থনা কবিতার প্রথম ১২ লাইন সুন্দর করে লিখ",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordListBottomSheet(
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

    // Auto focus handling
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
                    "বাক্য তৈরি" -> "যেমন: নিচের শব্দগুলো দিয়ে বাক্য তৈরি কর:"
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

            // Chunked into 2 items per row
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
