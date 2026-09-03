package com.mumtahin.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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

private val handledQuestionTypes = listOf(
    "কবিতা", "শব্দার্থ", "বাক্য তৈরি", "বিপরীত শব্দ", "শূন্যস্থান"
)

@Composable
internal fun QuestionTypesSection(
    addedQuestionTypes: Set<String>,
    onTypeClick: (String) -> Unit
) {
    // Local selection state for the other (not-yet-wired) question types —
    // kept here so real selected/unselected behavior can be added later
    // without restructuring the UI.
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
                    val isHandledType = type in handledQuestionTypes
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
                // Odd item count: keep the last, lone card at half width
                // so the grid still looks balanced instead of stretching.
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
