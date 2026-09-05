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
import androidx.compose.material.icons.filled.Check
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

/** One "ক) ..." true/false statement (no answer key). */
private data class TrueFalseStatement(
    val id: Long,
    val text: String
)

/**
 * MD3 EXPRESSIVE
 *
 * Bottom sheet opened from the "ঠিক চিহ্ন" question-type card. Same
 * ক)/খ)/গ)... growing statement list as সংক্ষিপ্ত প্রশ্ন — just a plain
 * list of statements the student marks ✓/✗ on paper. No answer key is
 * stored here.
 *
 * Uses stable Material3 APIs only — no ExperimentalMaterial3ExpressiveApi
 * opt-in needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrueFalseBottomSheet(
    initialQuestion: SavedQuestion.TrueFalse?,
    onDismiss: () -> Unit,
    onSave: (questionText: String, statements: List<String>, marks: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var questionText by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var marks by remember { mutableStateOf(initialQuestion?.marks ?: "") }
    var statements by remember {
        mutableStateOf(
            (initialQuestion?.statements?.takeIf { it.isNotEmpty() } ?: listOf(""))
                .mapIndexed { index, text -> TrueFalseStatement(id = index.toLong(), text = text) }
        )
    }
    var nextId by remember { mutableStateOf((statements.maxOfOrNull { it.id } ?: 0L) + 1) }

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
            TrueFalseHeroHeader(isEditing = initialQuestion != null)

            Spacer(modifier = Modifier.height(20.dp))

            AppTextField(
                label = "প্রশ্ন",
                value = questionText,
                onValueChange = { questionText = it },
                placeholder = "যেমন: নিচের বাক্যগুলোতে ঠিক (✓) বা ভুল (✗) চিহ্ন দাও"
            )

            Text(
                text = "বাক্যসমূহ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp, top = 4.dp)
            )

            statements.forEachIndexed { index, item ->
                val focusRequester = focusRequesters.getOrPut(item.id) { FocusRequester() }
                TrueFalseStatementRow(
                    label = ordinalLabel(index),
                    value = item.text,
                    canRemove = statements.size > 1,
                    focusRequester = focusRequester,
                    onValueChange = { newText ->
                        statements = statements.map {
                            if (it.id == item.id) it.copy(text = newText) else it
                        }
                    },
                    onRemove = {
                        statements = statements.filterNot { it.id == item.id }
                        focusRequesters.remove(item.id)
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    val newId = nextId
                    statements = statements + TrueFalseStatement(id = newId, text = "")
                    nextId += 1
                    newlyAddedId = newId
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = CircleShape
            ) {
                Text("+ আরও বাক্য যোগ করুন", fontWeight = FontWeight.SemiBold)
            }

            AppTextField(
                label = "মার্ক",
                value = marks,
                onValueChange = { marks = it },
                placeholder = "যেমন: ১০"
            )

            Spacer(modifier = Modifier.height(8.dp))

            TrueFalseSaveButton(
                enabled = questionText.isNotBlank(),
                onClick = {
                    dismissSheet {
                        val nonEmpty = statements.map { it.text }.filter { it.isNotBlank() }
                        onSave(questionText, nonEmpty, marks)
                    }
                }
            )
        }
    }
}

@Composable
private fun TrueFalseHeroHeader(isEditing: Boolean) {
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
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "ঠিক চিহ্ন",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isEditing) "প্রশ্ন সম্পাদনা করুন" else "প্রশ্ন ও বাক্য যোগ করুন",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TrueFalseStatementRow(
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
        // Circular ক)/খ)/গ)... badge, matching FillBlanks/ShortQuestions.
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
            placeholder = { Text("যেমন: আল্লাহ এক ও অদ্বিতীয়") },
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
private fun TrueFalseSaveButton(enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "trueFalseSaveButtonShapeMorph"
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
