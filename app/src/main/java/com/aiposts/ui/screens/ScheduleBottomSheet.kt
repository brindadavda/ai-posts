package com.aiposts.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiposts.ui.components.PrimaryButton
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleBottomSheet(
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState(is24Hour = false)

    var showDateDialog by remember { mutableStateOf(false) }

    if (showDateDialog) {
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                PrimaryButton(text = "Confirm Date", onClick = { showDateDialog = false })
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Schedule Reminder", style = MaterialTheme.typography.titleMedium)
        PrimaryButton(text = "Pick Date", onClick = { showDateDialog = true })
        TimeInput(state = timeState)
        PrimaryButton(
            text = "Set Reminder",
            onClick = {
                val millis = dateState.selectedDateMillis ?: System.currentTimeMillis()
                val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                onConfirm(
                    LocalDateTime.of(
                        selectedDate,
                        LocalTime.of(timeState.hour, timeState.minute)
                    )
                )
                onDismiss()
            }
        )
    }
}
