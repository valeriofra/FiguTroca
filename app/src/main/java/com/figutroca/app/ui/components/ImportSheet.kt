package com.figutroca.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.figutroca.app.data.ImportMode
import com.figutroca.app.util.ListParser

private data class ImportKind(val label: String, val mode: ImportMode, val hint: String)

private val kinds = listOf(
    ImportKind("Repetidas", ImportMode.DUPLICATES, "Cada quantidade é o nº de repetidas para troca (POR 8(2) = 2 extras)."),
    ImportKind("Tenho", ImportMode.OWNED, "Cada quantidade é o total de cópias que você tem."),
    ImportKind("Faltam", ImportMode.MISSING, "Marca essas figurinhas como faltando (cria os espaços).")
)

/**
 * Paste a collector list ("FWC 1(2), 12  BRA 2, 3, 4(2) …") and turn it into
 * stickers. Every team referenced is auto-completed to its full 1–20 set.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSheet(
    onDismiss: () -> Unit,
    onImport: (raw: String, mode: ImportMode) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf("") }
    var kindIndex by remember { mutableStateOf(0) }
    val kind = kinds[kindIndex]
    val preview = remember(text) { ListParser.parse(text) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Importar lista", style = MaterialTheme.typography.titleLarge)

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                kinds.forEachIndexed { index, k ->
                    SegmentedButton(
                        selected = kindIndex == index,
                        onClick = { kindIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index, kinds.size),
                        label = { Text(k.label) }
                    )
                }
            }

            Text(
                kind.hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Cole a lista aqui") },
                placeholder = { Text("FWC 1(2), 12, 11\nBRA 2, 3, 4(2), 14\nPOR 8(2)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 240.dp)
            )

            if (preview.entries.isNotEmpty()) {
                Text(
                    "Detectado: ${preview.teams.size} seleções · " +
                        "${preview.stickerCount} figurinhas · ${preview.copies} cópias",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = { onImport(text, kind.mode); onDismiss() },
                enabled = preview.entries.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Importar ${preview.stickerCount.takeIf { it > 0 }?.let { "($it)" } ?: ""}".trim())
            }
        }
    }
}
