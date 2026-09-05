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
import androidx.compose.foundation.shape.RoundedCornerShape
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

/** Original full set — বাংলা ও ইংরেজি দুটোতেই একদম অপরিবর্তিত, কবিতাসহ। */
private val defaultQuestionTypes = listOf(
    "কবিতা", "শব্দার্থ", "বাক্য তৈরি", "বিপরীত শব্দ",
    "শূন্যস্থান", "সংক্ষিপ্ত প্রশ্ন", "প্রশ্ন", "ঠিক চিহ্ন"
)

/**
 * Which cards show up on the "প্রশ্নের ধরন" গ্রিডে — শুধু অংকের জন্য আলাদা
 * সেট (টেক্সট-বেজড টাইপ বাদ, বদলে "অংক" টাইপ)। বাকি সব সাবজেক্ট (বাংলা,
 * ইংরেজি, আরবী, ভবিষ্যতের যেকোনো নতুন সাবজেক্ট) একই ডিফল্ট সেট পাবে।
 */
private fun questionTypesFor(subjectName: String): List<String> = when (subjectName) {
    "অংক" -> listOf("অংক", "শূন্যস্থান", "সংক্ষিপ্ত প্রশ্ন", "প্রশ্ন", "ঠিক চিহ্ন")
    else -> defaultQuestionTypes
}

/** Every type that actually has a working bottom sheet wired up. */
private val handledQuestionTypes = listOf(
    "কবিতা", "শব্দার্থ", "বাক্য তৈরি", "বিপরীত শব্দ", "শূন্যস্থান",
    "সংক্ষিপ্ত প্রশ্ন", "প্রশ্ন", "ঠিক চিহ্ন", "অংক"
)

@Composable
internal fun QuestionTypesSection(
    subjectName: String,
    addedQuestionTypes: Set<String>,
    onTypeClick: (String) -> Unit
) {
    var selectedTypes by remember { mutableStateOf(setOf<String>()) }
    val questionTypes = remember(subjectName) { questionTypesFor(subjectName) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "প্রশ্নের ধরন",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
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
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "questionTypeCardColor"
    )
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(64.dp), // এক্সপ্রেসভ লেআউটে বড় টাচ টার্গেট
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp), // Expressive Rounded Corner
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
