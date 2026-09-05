package com.mumtahin.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** One "ক) ..." short-answer sub-question. */
private data class ShortSubQuestion(
    val id: Long,
    val text: String
)

/**
 * MD3 EXPRESSIVE VERSION
 *
 * Bottom sheet opened from the "সংক্ষিপ্ত প্রশ্ন" question-type card. Same
 * ক)/খ)/গ)... growing sub-question list as শূন্যস্থান, but a plain answer
 * field — no built-in blank inserter here.
 *
 * Uses stable Material3 APIs only — no ExperimentalMaterial3ExpressiveApi
 * opt-in needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShortQuestionsBottomSheet(
    initialQuestion: SavedQuestion.ShortQuestions?,
    onDismiss: () -> Unit,
    onSave: (questionText: String, subQuestions: List<String>, marks: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var questionText by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var marks by remember { mutableStateOf(initialQuestion?.marks ?: "") }
    var subQuestions by remember {
        mutableStateOf(
            (initialQuestion?.subQuestions?.takeIf { it.isNotEmpty() } ?: listOf(""))
                .mapIndexed { index, text -> ShortSubQuestion(id = index.toLong(), text = text) }
        )
    }
    var nextId by remember { mutableStateOf((subQuestions.maxOfOrNull { it.id } ?: 0L) + 1) }

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
            ShortQuestionsHeroHeader(isEditing = initialQuestion != null)

            Spacer(modifier = Modifier.height(20.dp))

            AppTextField(
                label = "প্রশ্ন",
                value = questionText,
                onValueChange = { questionText = it },
                placeholder = "যেমন: নিচের প্রশ্নগুলোর উত্তর দাও"
            )

            Text(
                text = "সংক্ষিপ্ত প্রশ্নসমূহ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp, top = 4.dp)
            )

            subQuestions.forEachIndexed { index, item ->
                val focusRequester = focusRequesters.getOrPut(item.id) { FocusRequester() }
                ShortSubQuestionRow(
                    label = ordinalLabel(index),
                    value = item.text,
                    canRemove = subQuestions.size > 1,
                    focusRequester = focusRequester,
                    onValueChange = { newText ->
                        subQuestions = subQuestions.map {
                            if (it.id == item.id) it.copy(text = newText) else it
                        }
                    },
                    onRemove = {
                        subQuestions = subQuestions.filterNot { it.id == item.id }
                        focusRequesters.remove(item.id)
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    val newId = nextId
                    subQuestions = subQuestions + ShortSubQuestion(id = newId, text = "")
                    nextId += 1
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

            ShortQuestionsSaveButton(
                enabled = questionText.isNotBlank(),
                onClick = {
                    dismissSheet {
                        val nonEmpty = subQuestions.map { it.text }.filter { it.isNotBlank() }
                        onSave(questionText, nonEmpty, marks)
                    }
                }
            )
        }
    }
}

@Composable
private fun ShortQuestionsHeroHeader(isEditing: Boolean) {
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
                imageVector = Icons.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "সংক্ষিপ্ত প্রশ্ন",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isEditing) "প্রশ্ন সম্পাদনা করুন" else "প্রশ্ন ও sub-প্রশ্ন যোগ করুন",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ShortSubQuestionRow(
    label: String,
    value: String,
    canRemove: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
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
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = { Text("যেমন: তোমার রব কে?") },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
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
}

/** Save button with a bouncy spring-based shape morph on press. */
@Composable
private fun ShortQuestionsSaveButton(enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "shortQuestionsSaveButtonShapeMorph"
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
