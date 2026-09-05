package com.mumtahin.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val operators = listOf("+", "−", "×", "÷")

/**
 * MD3 EXPRESSIVE
 *
 * Bottom sheet opened from the "গাণিতিক সমস্যা" question-type card. Each
 * problem is operand1 [operator] operand2, where the operator is a
 * cycling badge (tap to move to the next symbol) instead of a dropdown —
 * faster to use on a phone keyboard-open layout. A layout toggle picks
 * whether the whole set renders as column-form (উপর-নিচে) or inline
 * (পাশাপাশি) on the exam paper.
 *
 * Uses stable Material3 APIs only — no ExperimentalMaterial3ExpressiveApi
 * opt-in needed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MathProblemBottomSheet(
    initialQuestion: SavedQuestion.MathProblem?,
    onDismiss: () -> Unit,
    onSave: (questionText: String, layout: MathLayout, problems: List<MathProblemEntry>, marks: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var questionText by remember { mutableStateOf(initialQuestion?.questionText ?: "নিচের অংকগুলো কর") }
    var marks by remember { mutableStateOf(initialQuestion?.marks ?: "") }
    var layout by remember { mutableStateOf(initialQuestion?.layout ?: MathLayout.VERTICAL) }
    var problems by remember {
        mutableStateOf(
            initialQuestion?.problems?.takeIf { it.isNotEmpty() }
                ?.mapIndexed { index, entry -> IndexedMathEntry(id = index.toLong(), entry = entry) }
                ?: listOf(IndexedMathEntry(id = 0L, entry = MathProblemEntry("", "+", "")))
        )
    }
    var nextId by remember { mutableStateOf((problems.maxOfOrNull { it.id } ?: 0L) + 1) }

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
            MathProblemHeroHeader(isEditing = initialQuestion != null)

            Spacer(modifier = Modifier.height(20.dp))

            AppTextField(
                label = "প্রশ্ন",
                value = questionText,
                onValueChange = { questionText = it },
                placeholder = "যেমন: নিচের অংকগুলো কর"
            )

            LayoutPicker(layout = layout, onLayoutChange = { layout = it })

            Text(
                text = "অংকসমূহ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp, top = 4.dp)
            )

            problems.forEachIndexed { index, item ->
                MathProblemRow(
                    label = ordinalLabel(index),
                    entry = item.entry,
                    canRemove = problems.size > 1,
                    onEntryChange = { newEntry ->
                        problems = problems.map { if (it.id == item.id) it.copy(entry = newEntry) else it }
                    },
                    onRemove = {
                        problems = problems.filterNot { it.id == item.id }
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    val newId = nextId
                    problems = problems + IndexedMathEntry(id = newId, entry = MathProblemEntry("", "+", ""))
                    nextId += 1
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = CircleShape
            ) {
                Text("+ আরও অংক যোগ করুন", fontWeight = FontWeight.SemiBold)
            }

            AppTextField(
                label = "মার্ক",
                value = marks,
                onValueChange = { marks = it },
                placeholder = "যেমন: ১০"
            )

            Spacer(modifier = Modifier.height(8.dp))

            MathProblemSaveButton(
                enabled = questionText.isNotBlank(),
                onClick = {
                    dismissSheet {
                        val nonEmpty = problems
                            .map { it.entry }
                            .filter { it.operand1.isNotBlank() || it.operand2.isNotBlank() }
                        onSave(questionText, layout, nonEmpty, marks)
                    }
                }
            )
        }
    }
}

/** Wraps a MathProblemEntry with a stable id for list diffing / focus tracking. */
private data class IndexedMathEntry(val id: Long, val entry: MathProblemEntry)

@Composable
private fun MathProblemHeroHeader(isEditing: Boolean) {
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
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "অংক",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isEditing) "প্রশ্ন সম্পাদনা করুন" else "অংক ও লেআউট বাছাই করুন",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

/** উপর-নিচে vs পাশাপাশি — single-select pill chips, like the marks quick-picker. */
@Composable
private fun LayoutPicker(layout: MathLayout, onLayoutChange: (MathLayout) -> Unit) {
    Text(
        text = "লেআউট",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
    )
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        FilterChip(
            selected = layout == MathLayout.VERTICAL,
            onClick = { onLayoutChange(MathLayout.VERTICAL) },
            label = { Text("উপর-নিচে") },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier.padding(end = 8.dp)
        )
        FilterChip(
            selected = layout == MathLayout.HORIZONTAL,
            onClick = { onLayoutChange(MathLayout.HORIZONTAL) },
            label = { Text("পাশাপাশি") },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )
    }
}

@Composable
private fun MathProblemRow(
    label: String,
    entry: MathProblemEntry,
    canRemove: Boolean,
    onEntryChange: (MathProblemEntry) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ক)/খ)/গ)... badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        OutlinedTextField(
            value = entry.operand1,
            onValueChange = { onEntryChange(entry.copy(operand1 = it)) },
            modifier = Modifier.weight(1f),
            placeholder = { Text("৫২") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        // Expressive: tap-to-cycle operator badge instead of a dropdown.
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    val currentIndex = operators.indexOf(entry.operator).takeIf { it >= 0 } ?: 0
                    val nextOperator = operators[(currentIndex + 1) % operators.size]
                    onEntryChange(entry.copy(operator = nextOperator))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.operator,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        OutlinedTextField(
            value = entry.operand2,
            onValueChange = { onEntryChange(entry.copy(operand2 = it)) },
            modifier = Modifier.weight(1f),
            placeholder = { Text("৩৮") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        if (canRemove) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** Save button with a bouncy spring-based shape morph on press. */
@Composable
private fun MathProblemSaveButton(enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "mathProblemSaveButtonShapeMorph"
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
