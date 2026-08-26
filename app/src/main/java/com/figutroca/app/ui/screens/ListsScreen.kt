package com.figutroca.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.figutroca.app.data.Sticker
import com.figutroca.app.ui.AppViewModel
import com.figutroca.app.ui.components.EmptyState
import com.figutroca.app.util.ShareLists
import android.widget.Toast

enum class ListKind { DUPLICATES, MISSING }

@Composable
fun ListsScreen(vm: AppViewModel, kind: ListKind, contentPadding: PaddingValues) {
    val album by vm.albumState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val name = album.activeCollection?.name ?: "Coleção"

    val items = when (kind) {
        ListKind.DUPLICATES -> album.stickers.filter { it.duplicates > 0 }
        ListKind.MISSING -> album.stickers.filter { it.missing }
    }
    val title = if (kind == ListKind.DUPLICATES) "Repetidas para troca" else "Faltam"
    val listText = when (kind) {
        ListKind.DUPLICATES -> ShareLists.duplicates(name, album.stickers)
        ListKind.MISSING -> ShareLists.missing(name, album.stickers)
    }
    val totalCount = when (kind) {
        ListKind.DUPLICATES -> items.sumOf { it.duplicates }
        ListKind.MISSING -> items.size
    }

    if (album.activeCollection == null) {
        EmptyState(
            "Nenhuma coleção ativa",
            "Crie uma coleção na aba Coleções para começar.",
            Modifier.padding(contentPadding)
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    "$totalCount ${if (kind == ListKind.DUPLICATES) "figurinhas repetidas" else "figurinhas faltando"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                ShareCard(
                    text = listText,
                    onCopy = {
                        clipboard.setText(AnnotatedString(listText))
                        Toast.makeText(context, "Lista copiada", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        ShareLists.share(context, listText, "$name — $title")
                    }
                )
            }
        }

        if (items.isEmpty()) {
            item {
                Text(
                    if (kind == ListKind.DUPLICATES) "Você ainda não tem repetidas. 🙂"
                    else "Parabéns, não falta nenhuma! 🏆",
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(items, key = { it.id }) { sticker ->
            StickerRow(sticker, kind, vm)
        }
    }
}

@Composable
private fun ShareCard(text: String, onCopy: () -> Unit, onShare: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = onCopy, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                    Text("  Copiar")
                }
                FilledTonalButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Share, contentDescription = null)
                    Text("  Compartilhar")
                }
            }
        }
    }
}

@Composable
private fun StickerRow(sticker: Sticker, kind: ListKind, vm: AppViewModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    sticker.code,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (sticker.group.isNotBlank()) {
                    Text(
                        sticker.group,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (kind == ListKind.DUPLICATES) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.decrement(sticker) }) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Menos uma")
                    }
                    Text(
                        "${sticker.duplicates}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilledTonalIconButton(onClick = { vm.increment(sticker) }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Mais uma")
                    }
                }
            } else {
                FilledTonalButton(onClick = { vm.increment(sticker) }) {
                    Text("Já colei")
                }
            }
        }
    }
}
