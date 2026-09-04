package com.mumtahin.ui.screens

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun ExamInfoSection(subjectName: String) {
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(28.dp) // MD3 Expressive Corner Radius
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
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

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { expanded = false },
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
