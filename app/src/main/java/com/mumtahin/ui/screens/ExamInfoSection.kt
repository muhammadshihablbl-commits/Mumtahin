package com.mumtahin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ExamInfoSection(
    examName: String,
    onExamNameChange: (String) -> Unit,
    madrasahName: String,
    onMadrasahNameChange: (String) -> Unit,
    subjectName: String,
    onSubjectNameChange: (String) -> Unit,
    className: String,
    onClassNameChange: (String) -> Unit,
    time: String,
    onTimeChange: (String) -> Unit,
    fullMarks: String,
    onFullMarksChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
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
                        text = if (isExpanded) "তথ্য পরিবর্তন করতে নিচে লিখুন" else "পরীক্ষা সম্পর্কিত তথ্য দেখতে ট্যাপ করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    ExpressiveInputField(
                        value = examName,
                        onValueChange = onExamNameChange,
                        label = "পরীক্ষার নাম",
                        icon = Icons.Default.Edit
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExpressiveInputField(
                        value = madrasahName,
                        onValueChange = onMadrasahNameChange,
                        label = "মাদরাসার নাম",
                        icon = Icons.Default.School
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExpressiveInputField(
                        value = subjectName,
                        onValueChange = onSubjectNameChange,
                        label = "বিষয়",
                        icon = Icons.Default.Book
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExpressiveInputField(
                        value = className,
                        onValueChange = onClassNameChange,
                        label = "শ্রেণী",
                        icon = Icons.Default.Class
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExpressiveInputField(
                        value = time,
                        onValueChange = onTimeChange,
                        label = "সময়",
                        icon = Icons.Default.Timer
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ExpressiveInputField(
                        value = fullMarks,
                        onValueChange = onFullMarksChange,
                        label = "পূর্ণমান",
                        icon = Icons.Default.Grade
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Save Information", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        singleLine = true
    )
}
