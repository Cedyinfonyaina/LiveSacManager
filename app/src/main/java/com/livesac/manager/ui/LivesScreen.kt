@file:OptIn(ExperimentalMaterial3Api::class)

package com.livesac.manager.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livesac.manager.data.LiveSummary
import com.livesac.manager.domain.defaultLiveTitle
import com.livesac.manager.domain.formatAr
import com.livesac.manager.domain.formatDateLong
import com.livesac.manager.domain.formatDateShort
import com.livesac.manager.domain.plural
import com.livesac.manager.domain.todayUtcMillis

/** Écran 1 : liste des Lives. */
@Composable
fun LivesScreen(onOpenLive: (Long) -> Unit) {
    val repo = rememberRepository()
    val vm: LivesViewModel = viewModel(factory = VmFactory { LivesViewModel(repo) })
    val livesState by vm.lives.collectAsStateWithLifecycle()
    var showNew by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar("Live Sac Manager") },
        floatingActionButton = { AppFab("+ Nouveau Live") { showNew = true } }
    ) { padding ->
        val lives = livesState
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                lives == null -> Unit
                lives.isEmpty() -> EmptyMessage("Aucun Live pour le moment.\nAppuyez sur « + Nouveau Live » pour commencer.")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lives, key = { it.id }) { live ->
                        LiveCard(live) { onOpenLive(live.id) }
                    }
                }
            }
        }
    }

    if (showNew) {
        NewLiveDialog(
            onDismiss = { showNew = false },
            onCreate = { title, dateMillis ->
                vm.createLive(title, dateMillis) { newId ->
                    showNew = false
                    onOpenLive(newId)
                }
            }
        )
    }
}

@Composable
private fun LiveCard(live: LiveSummary, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = live.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatDateShort(live.dateMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = plural(live.stats.clientCount, "client"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = plural(live.stats.bagCount, "sac"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatAr(live.stats.total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Création d'un Live : uniquement un titre et une date. */
@Composable
private fun NewLiveDialog(onDismiss: () -> Unit, onCreate: (String, Long) -> Unit) {
    var dateMillis by remember { mutableStateOf(todayUtcMillis()) }
    var title by remember { mutableStateOf(defaultLiveTitle(dateMillis)) }
    var titleEdited by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Nouveau Live") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleEdited = true
                    },
                    label = { Text("Titre") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Date : " + formatDateLong(dateMillis))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title.trim(), dateMillis) },
                enabled = title.isNotBlank()
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { picked ->
                        dateMillis = picked
                        // Si le titre n'a pas été modifié à la main, il suit la date choisie.
                        if (!titleEdited) title = defaultLiveTitle(picked)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Annuler") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
