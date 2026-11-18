package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.StudyPlanRowEntity
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.FridayOffBgDark
import com.example.ui.theme.FridayOffBgLight
import com.example.ui.theme.FridayOffTextDark
import com.example.ui.theme.FridayOffTextLight

@Composable
fun StudyPlannerScreen(
    rows: List<StudyPlanRowEntity>,
    isDarkTheme: Boolean,
    onUpdateRow: (StudyPlanRowEntity) -> Unit,
    onAddDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var editingRow by remember { mutableStateOf<StudyPlanRowEntity?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.studyPlannerTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.studyPlannerSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Table Header (100% responsive weights — zero horizontal scroll)
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.colDate,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.1f)
                        )
                        Text(
                            text = strings.colSubject,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.25f)
                        )
                        Text(
                            text = strings.colChapter,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.35f)
                        )
                        Text(
                            text = strings.colHours,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }

            items(rows, key = { it.id }) { row ->
                val isFridayOff = row.offDay || row.dayOfWeekIso == 5
                val offBg = if (isDarkTheme) FridayOffBgDark else FridayOffBgLight
                val offText = if (isDarkTheme) FridayOffTextDark else FridayOffTextLight

                Surface(
                    color = if (isFridayOff) offBg else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = if (isFridayOff) 0.dp else 1.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { editingRow = row }
                        .testTag("study_plan_row_${row.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.1f).padding(end = 4.dp)) {
                            Text(
                                text = row.dateLabel,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFridayOff) offText else MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isFridayOff) {
                            Row(
                                modifier = Modifier.weight(2.6f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Weekend,
                                    contentDescription = null,
                                    tint = offText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.fridayOffDay,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = offText
                                )
                            }
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = offText,
                                modifier = Modifier.weight(0.7f)
                            )
                        } else {
                            Text(
                                text = row.subject,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.25f).padding(end = 4.dp)
                            )
                            Text(
                                text = row.chapter,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.35f).padding(end = 4.dp)
                            )
                            Row(
                                modifier = Modifier.weight(0.7f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${row.hours}h",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = strings.editStudyRowTitle,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onAddDay,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_study_day_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.addStudyDay.removePrefix("+ ").trim())
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.savedLocallyNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    editingRow?.let { row ->
        EditStudyRowDialog(
            row = row,
            onDismiss = { editingRow = null },
            onSave = { updated ->
                onUpdateRow(updated)
                editingRow = null
            }
        )
    }
}

@Composable
private fun EditStudyRowDialog(
    row: StudyPlanRowEntity,
    onDismiss: () -> Unit,
    onSave: (StudyPlanRowEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    var dateLabel by remember { mutableStateOf(row.dateLabel) }
    var subject by remember { mutableStateOf(row.subject) }
    var chapter by remember { mutableStateOf(row.chapter) }
    var hours by remember { mutableStateOf(row.hours) }
    var isOffDay by remember { mutableStateOf(row.offDay) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.editStudyRowTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { dateLabel = it },
                    label = { Text(strings.colDate) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_study_date_input")
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(strings.colSubject) },
                    enabled = !isOffDay,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_study_subject_input")
                )
                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text(strings.colChapter) },
                    enabled = !isOffDay,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_study_chapter_input")
                )
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("${strings.colHours} (${strings.hoursUnit})") },
                    enabled = !isOffDay,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_study_hours_input")
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isOffDay = !isOffDay }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isOffDay,
                        onCheckedChange = { isOffDay = it }
                    )
                    Text(strings.markAsOffDay, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        row.copy(
                            dateLabel = dateLabel.ifBlank { row.dateLabel },
                            subject = subject.trim(),
                            chapter = chapter.trim(),
                            hours = hours.trim().ifBlank { "0" },
                            offDay = isOffDay
                        )
                    )
                },
                modifier = Modifier.testTag("save_study_row_button")
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
