package com.mumtahin.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * MD3 EXPRESSIVE VERSION
 *
 * Bottom sheet shared by "কবিতা" and "প্রশ্ন" — a single question whose
 * answer is expected to be long, with no sub-questions. `typeTitle` picks
 * the sheet's heading, icon, and question-field hint.
 *
 * Expressive touches added:
 *  - Larger corner radius on the sheet + a bold "hero" icon badge up top
 *  - Bouncy press animation on the Save button (spring, not linear ease)
 *  - Quick-pick mark chips so most users never have to type a number
 *  - Bolder, bigger typography for the heading
 *
 * All of this uses stable Material3 APIs only — no ExperimentalMaterial3ExpressiveApi
 * opt-in needed, so no material3 version bump is required beyond what the
 * project already has.
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
        sheetState = sheetState,
        // Expressive: bigger top corners than the M3 default (28.dp)
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
            HeroHeader(typeTitle = typeTitle, isEditing = initialQuestion != null)

            Spacer(modifier = Modifier.height(20.dp))

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

            MarksPicker(
                marks = marks,
                onMarksChange = { marks = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExpressiveSaveButton(
                enabled = question.isNotBlank(),
                onClick = { dismissSheet { onSave(question, marks) } }
            )
        }
    }
}

/**
 * Icon badge + heading. The badge shape and icon change with `typeTitle`
 * so কবিতা and প্রশ্ন feel like distinct, recognizable destinations
 * instead of two generic forms.
 */
@Composable
private fun HeroHeader(typeTitle: String, isEditing: Boolean) {
    val icon: ImageVector = if (typeTitle == "কবিতা") Icons.Filled.Create else Icons.Filled.Info

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                // Expressive: soft "cookie"-like large-radius square instead of a plain circle
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = typeTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isEditing) "প্রশ্ন সম্পাদনা করুন" else "প্রশ্ন ও মার্ক লিখুন",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Quick-pick mark chips (common values) + falls back to free text via
 * AppTextField, so entry is fast for the 90% case but still flexible.
 */
@Composable
private fun MarksPicker(
    marks: String,
    onMarksChange: (String) -> Unit
) {
    val commonMarks = listOf("৫", "১০", "১৫", "২০")

    Text(
        text = "মার্ক",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        commonMarks.forEach { value ->
            FilterChip(
                selected = marks == value,
                onClick = { onMarksChange(value) },
                label = { Text(value) },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }

    AppTextField(
        label = "অথবা নিজে লিখুন",
        value = marks,
        onValueChange = onMarksChange,
        placeholder = "যেমন: ১০"
    )
}

/**
 * Save button with a bouncy (spring-based, overshooting) scale animation
 * on press — the signature "expressive" motion feel instead of a flat
 * linear press state.
 */
@Composable
private fun ExpressiveSaveButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "saveButtonShapeMorph"
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
