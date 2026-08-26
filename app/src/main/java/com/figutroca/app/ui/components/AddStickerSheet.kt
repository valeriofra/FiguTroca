package com.figutroca.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Bottom sheet to add sticker slots to the current album, either as a numbered
 * range ("1 a 670") or as free-form codes ("ARG1, ARG2, FWC").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStickerSheet(
    onDismiss: () -> Unit,
    onAddRange: (from: Int, to: Int, group: String) -> Unit,
    onAddCodes: (raw: String, group: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rangeMode by remember { mutableStateOf(true) }
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var codes by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Adicionar figurinhas", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = rangeMode,
                    onClick = { rangeMode = true },
                    label = { Text("Intervalo") }
                )
                FilterChip(
                    selected = !rangeMode,
                    onClick = { rangeMode = false },
                    label = { Text("Avulsas") }
                )
            }

            if (rangeMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = from,
                        onValueChange = { from = it.filter(Char::isDigit) },
                        label = { Text("De") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = to,
                        onValueChange = { to = it.filter(Char::isDigit) },
                        label = { Text("Até") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                OutlinedTextField(
                    value = codes,
                    onValueChange = { codes = it },
                    label = { Text("Códigos (separados por vírgula)") },
                    placeholder = { Text("ex: ARG1, ARG2, FWC, 101") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = group,
                onValueChange = { group = it },
                label = { Text("Grupo / seleção (opcional)") },
                placeholder = { Text("ex: Brasil") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (rangeMode) {
                        val f = from.toIntOrNull()
                        val t = to.toIntOrNull()
                        if (f != null && t != null) {
                            onAddRange(f, t, group)
                            onDismiss()
                        }
                    } else if (codes.isNotBlank()) {
                        onAddCodes(codes, group)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar ao álbum")
            }
        }
    }
}
