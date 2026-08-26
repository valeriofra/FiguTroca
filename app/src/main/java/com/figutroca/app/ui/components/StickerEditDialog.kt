package com.figutroca.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.figutroca.app.data.Sticker

/**
 * Edit one sticker: set the exact number of copies owned, mark it as missing,
 * or delete it. Changes are applied immediately through [onSetCount].
 */
@Composable
fun StickerEditDialog(
    sticker: Sticker,
    onSetCount: (Int) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var count by remember { mutableIntStateOf(sticker.count) }

    fun apply(newCount: Int) {
        count = newCount.coerceAtLeast(0)
        onSetCount(count)
    }

    val status = when {
        count == 0 -> "Falta"
        count == 1 -> "Tenho"
        else -> "Tenho + ${count - 1} repetida${if (count - 1 > 1) "s" else ""}"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(sticker.code, fontWeight = FontWeight.Bold)
                if (sticker.group.isNotBlank()) {
                    Text(
                        sticker.group,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(onClick = { apply(count - 1) }) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Menos uma")
                    }
                    Text(
                        "$count",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    FilledIconButton(onClick = { apply(count + 1) }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Mais uma")
                    }
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(status) },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { apply(0) }) { Text("Marcar falta") }
                    TextButton(onClick = { apply(1) }) { Text("Tenho 1") }
                    TextButton(
                        onClick = { onDelete(); onDismiss() },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("  Excluir")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Concluir") }
        }
    )
}
