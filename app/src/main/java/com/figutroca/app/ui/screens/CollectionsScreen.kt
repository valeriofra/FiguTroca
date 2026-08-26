package com.figutroca.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.figutroca.app.data.Collection
import com.figutroca.app.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@Composable
fun CollectionsScreen(vm: AppViewModel, contentPadding: PaddingValues) {
    val album by vm.albumState.collectAsStateWithLifecycle()
    val collections by vm.collections.collectAsStateWithLifecycle()
    val active = album.activeCollection

    var showNew by remember { mutableStateOf(false) }
    var showArchive by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Coleções", style = MaterialTheme.typography.headlineMedium) }

        if (active != null) {
            item {
                ActiveCard(
                    name = active.name,
                    owned = album.stats.owned,
                    total = album.stats.total,
                    percent = (album.stats.completion * 100).toInt()
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { showNew = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Text("  Nova coleção")
                }
                if (active != null) {
                    OutlinedButton(onClick = { showArchive = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Archive, contentDescription = null)
                        Text("  Arquivar")
                    }
                }
            }
        }

        val archived = collections.filter { !it.isActive }
        if (archived.isNotEmpty()) {
            item {
                Text(
                    "Arquivadas",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(archived, key = { it.id }) { c ->
                ArchivedCard(
                    collection = c,
                    onReopen = { vm.activateCollection(c) },
                    onDelete = { vm.deleteCollection(c) }
                )
            }
        }
    }

    if (showNew) {
        NewCollectionDialog(
            title = "Nova coleção",
            defaultName = suggestName(),
            confirmLabel = "Criar",
            onDismiss = { showNew = false },
            onConfirm = { name, total ->
                vm.createCollection(name, total)
                showNew = false
            }
        )
    }

    if (showArchive) {
        NewCollectionDialog(
            title = "Arquivar e iniciar nova",
            defaultName = suggestName(),
            confirmLabel = "Arquivar e criar",
            message = "A coleção atual \"${active?.name}\" será guardada como arquivada e uma nova coleção será iniciada.",
            onDismiss = { showArchive = false },
            onConfirm = { name, total ->
                vm.archiveAndStartNew(name, total)
                showArchive = false
            }
        )
    }
}

@Composable
private fun ActiveCard(name: String, owned: Int, total: Int, percent: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            AssistChip(
                onClick = {},
                enabled = false,
                leadingIcon = {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                },
                label = { Text("Ativa") }
            )
            Text(
                name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                "$owned de $total · $percent% completa",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ArchivedCard(collection: Collection, onReopen: () -> Unit, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    Card {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(Modifier.padding(start = 12.dp)) {
                    Text(collection.name, fontWeight = FontWeight.SemiBold)
                    val when_ = collection.archivedAt ?: collection.createdAt
                    Text(
                        "Arquivada em ${dateFmt.format(Date(when_))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                TextButton(onClick = onReopen) { Text("Reabrir") }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Excluir")
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Excluir coleção?") },
            text = { Text("\"${collection.name}\" e todas as suas figurinhas serão removidas. Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); confirmDelete = false }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun NewCollectionDialog(
    title: String,
    defaultName: String,
    confirmLabel: String,
    message: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, numberedTotal: Int) -> Unit
) {
    var name by remember { mutableStateOf(defaultName) }
    var total by remember { mutableStateOf("670") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (message != null) {
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da coleção") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = total,
                    onValueChange = { total = it.filter(Char::isDigit) },
                    label = { Text("Total de figurinhas numeradas") },
                    supportingText = { Text("Deixe 0 para adicionar manualmente depois") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, total.toIntOrNull() ?: 0) }) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun suggestName(): String {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    return "Copa do Mundo $year"
}
