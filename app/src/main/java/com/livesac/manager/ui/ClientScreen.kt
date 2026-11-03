@file:OptIn(ExperimentalMaterial3Api::class)

package com.livesac.manager.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livesac.manager.data.ClientDetail
import com.livesac.manager.domain.Bag
import com.livesac.manager.domain.formatAr
import com.livesac.manager.domain.plural

/** Écran 4 : détail d'un client, avec ajout / modification / suppression de sacs. */
@Composable
fun ClientScreen(clientId: Long, onBack: () -> Unit) {
    val repo = rememberRepository()
    val vm: ClientViewModel = viewModel(factory = VmFactory { ClientViewModel(repo, clientId) })
    val clientState by vm.client.collectAsStateWithLifecycle()

    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Bag?>(null) }
    var deleting by remember { mutableStateOf<Bag?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar("Fiche client", onBack) },
        floatingActionButton = { AppFab("+ Ajouter un sac") { adding = true } }
    ) { padding ->
        val client = clientState
        if (client != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { ClientHeader(client) }
                item {
                    Text(
                        text = "Sacs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                items(client.bags, key = { it.id }) { bag ->
                    BagRow(
                        bag = bag,
                        onEdit = { editing = bag },
                        onDelete = { deleting = bag }
                    )
                }
            }
        }
    }

    if (adding) {
        BagDialog(
            title = "Ajouter un sac",
            initialCode = "",
            initialPrice = "",
            onDismiss = { adding = false },
            onConfirm = { code, price ->
                vm.addBag(code, price)
                adding = false
            }
        )
    }

    editing?.let { bag ->
        BagDialog(
            title = "Modifier le sac",
            initialCode = bag.jpCode,
            initialPrice = bag.price.toString(),
            onDismiss = { editing = null },
            onConfirm = { code, price ->
                vm.updateBag(bag, code, price)
                editing = null
            }
        )
    }

    deleting?.let { bag ->
        val isLastBag = (clientState?.bagCount ?: 0) <= 1
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = Color.White,
            title = { Text("Supprimer ce sac ?") },
            text = {
                Text(
                    buildString {
                        append(bag.jpCode).append(" — ").append(formatAr(bag.price))
                        if (isLastBag) {
                            append("\n\nC'est le dernier sac : la fiche de ce client sera aussi supprimée.")
                        }
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    vm.deleteBag(bag.id) { onBack() }
                    deleting = null
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Annuler") } }
        )
    }
}

@Composable
private fun ClientHeader(client: ClientDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = client.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "JP : " + client.jpCodes.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = plural(client.bagCount, "sac"),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.75f)
            )
            Text(
                text = "Total à payer",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.75f)
            )
            Text(
                text = formatAr(client.total),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BagRow(bag: Bag, onEdit: () -> Unit, onDelete: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 6.dp, end = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = bag.jpCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatAr(bag.price),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Modifier ce sac")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer ce sac")
            }
        }
    }
}

@Composable
private fun BagDialog(
    title: String,
    initialCode: String,
    initialPrice: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var code by remember { mutableStateOf(initialCode) }
    var price by remember { mutableStateOf(initialPrice) }
    val amount = price.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code JP") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { typed -> price = typed.filter { c -> c in '0'..'9' }.take(9) },
                    label = { Text("Prix du sac") },
                    suffix = { Text("Ar") },
                    supportingText = { if (amount > 0) Text(formatAr(amount)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(code.trim(), amount) },
                enabled = code.isNotBlank() && amount > 0
            ) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
