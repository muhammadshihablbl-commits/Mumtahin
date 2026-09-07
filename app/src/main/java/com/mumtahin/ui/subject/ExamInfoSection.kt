package com.mumtahin.ui.subject

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mumtahin.data.ExamInfo
import com.mumtahin.data.HeaderOrder
import com.mumtahin.ui.components.AppTextField

@Composable
internal fun ExamInfoSection(
    examInfo: ExamInfo,
    onExamInfoChange: (ExamInfo) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "examInfoArrowRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(28.dp) // MD3 Expressive Corner Radius
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!expanded) }
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Exam Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "পরীক্ষা সম্পর্কিত তথ্য দেখতে ট্যাপ করুন",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
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
                    .padding(horizontal = 20.dp)
            ) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    label = "পরীক্ষার নাম",
                    value = examInfo.examName,
                    onValueChange = { onExamInfoChange(examInfo.copy(examName = it)) },
                    placeholder = "যেমন: অর্ধবার্ষিক পরীক্ষা"
                )
                AppTextField(
                    label = "মাদ্রাসার নাম",
                    value = examInfo.madrasaName,
                    onValueChange = { onExamInfoChange(examInfo.copy(madrasaName = it)) },
                    placeholder = "মাদ্রাসার নাম লিখুন"
                )

                Text(
                    text = "প্রিভিউতে আগে কোনটা দেখাবে?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
                )
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    androidx.compose.material3.FilterChip(
                        selected = examInfo.headerOrder == HeaderOrder.EXAM_FIRST,
                        onClick = { onExamInfoChange(examInfo.copy(headerOrder = HeaderOrder.EXAM_FIRST)) },
                        label = { Text("পরীক্ষার নাম আগে") },
                        shape = RoundedCornerShape(50),
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    androidx.compose.material3.FilterChip(
                        selected = examInfo.headerOrder == HeaderOrder.MADRASA_FIRST,
                        onClick = { onExamInfoChange(examInfo.copy(headerOrder = HeaderOrder.MADRASA_FIRST)) },
                        label = { Text("মাদ্রাসার নাম আগে") },
                        shape = RoundedCornerShape(50),
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }

                AppTextField(
                    label = "বিষয়",
                    value = examInfo.subject,
                    onValueChange = { onExamInfoChange(examInfo.copy(subject = it)) },
                    placeholder = "যেমন: বাংলা"
                )
                AppTextField(
                    label = "শ্রেণী",
                    value = examInfo.className,
                    onValueChange = { onExamInfoChange(examInfo.copy(className = it)) },
                    placeholder = "যেমন: ৬ষ্ঠ"
                )
                AppTextField(
                    label = "সময়",
                    value = examInfo.duration,
                    onValueChange = { onExamInfoChange(examInfo.copy(duration = it)) },
                    placeholder = "যেমন: ২ ঘণ্টা"
                )
                AppTextField(
                    label = "পূর্ণমান",
                    value = examInfo.fullMarks,
                    onValueChange = { onExamInfoChange(examInfo.copy(fullMarks = it)) },
                    placeholder = "যেমন: ১০০"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onExpandedChange(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = CircleShape // MD3 Expressive Pill Button
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
