package com.figutroca.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.figutroca.app.ui.AppViewModel
import com.figutroca.app.ui.components.EmptyState
import com.figutroca.app.ui.components.TeamCard
import com.figutroca.app.ui.components.TeamSheet
import com.figutroca.app.ui.components.groupTeams
import com.figutroca.app.ui.theme.DuplicateAmber
import com.figutroca.app.ui.theme.MissingGray
import com.figutroca.app.ui.theme.OwnedGreen
import com.figutroca.app.util.ShareLists

private enum class Section { ALBUM, FALTAM, REPETIDAS }

@Composable
fun AlbumScreen(vm: AppViewModel, contentPadding: PaddingValues) {
    val album by vm.albumState.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val stats = album.stats
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var section by remember { mutableStateOf(Section.ALBUM) }
    var openTeamCode by remember { mutableStateOf<String?>(null) }

    if (album.activeCollection == null) {
        EmptyState(
            "Nenhuma coleção ativa",
            "Vá até a aba Coleções para criar sua coleção da Copa.",
            Modifier.padding(contentPadding)
        )
        return
    }
    if (album.stickers.isEmpty()) {
        EmptyState(
            "Álbum vazio",
            "Use o ícone de colar (topo) para importar sua lista, ou + para adicionar.",
            Modifier.padding(contentPadding)
        )
        return
    }

    val name = album.activeCollection?.name ?: "Coleção"
    val allTeams = remember(album.stickers) { groupTeams(album.stickers) }

    val sectionTeams = when (section) {
        Section.ALBUM -> allTeams
        Section.FALTAM -> allTeams.filter { it.missing > 0 }
        Section.REPETIDAS -> allTeams.filter { it.duplicates > 0 }
    }
    val teams = sectionTeams.filter {
        query.isBlank() || it.name.contains(query, true) || it.code.contains(query, true) ||
            com.figutroca.app.data.Teams.enName(it.code).contains(query, true)
    }
    val openGroup = openTeamCode?.let { code -> allTeams.find { it.code == code } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(name, style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionPill("${stats.owned}", "Tenho", OwnedGreen,
                        section == Section.ALBUM, Modifier.weight(1f)) { section = Section.ALBUM }
                    SectionPill("${stats.missing}", "Faltam", MissingGray,
                        section == Section.FALTAM, Modifier.weight(1f)) { section = Section.FALTAM }
                    SectionPill("${stats.duplicates}", "Repetidas", DuplicateAmber,
                        section == Section.REPETIDAS, Modifier.weight(1f)) { section = Section.REPETIDAS }
                }
                DiscreetSearch(query = query, onQueryChange = vm::setQuery)
                if (section != Section.ALBUM) {
                    val listText = if (section == Section.REPETIDAS)
                        ShareLists.duplicates(name, album.stickers)
                    else ShareLists.missing(name, album.stickers)
                    val label = if (section == Section.REPETIDAS) "Repetidas" else "Faltam"
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledTonalButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(listText))
                                Toast.makeText(context, "Lista copiada", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                            Text("  Copiar")
                        }
                        FilledTonalButton(
                            onClick = { ShareLists.share(context, listText, "$name — $label") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null)
                            Text("  Enviar")
                        }
                    }
                }
                Text(
                    "Toque numa seleção para abrir as figurinhas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(teams, key = { it.code }) { group ->
            val subtitle = when (section) {
                Section.ALBUM -> "${group.owned}/${group.total}"
                Section.FALTAM -> "faltam ${group.missing}"
                Section.REPETIDAS -> "${group.duplicates} repetidas"
            }
            TeamCard(
                group = group,
                subtitle = subtitle,
                badge = if (section == Section.REPETIDAS) group.duplicates else null,
                onClick = { openTeamCode = group.code }
            )
        }

        if (teams.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "Nenhuma seleção nesta seção.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }

    openGroup?.let { group ->
        val shownStickers = when (section) {
            Section.ALBUM -> group.stickers
            Section.FALTAM -> group.stickers.filter { it.missing }
            Section.REPETIDAS -> group.stickers.filter { it.duplicates > 0 }
        }
        val hint = when (section) {
            Section.ALBUM -> "Toque p/ marcar ou desmarcar · segure p/ ajustar"
            Section.FALTAM -> "Toque para marcar como colada · segure p/ ajustar"
            Section.REPETIDAS -> "Cada toque soma +1 · segure para ajustar"
        }
        val onTap: (com.figutroca.app.data.Sticker) -> Unit = when (section) {
            // Album: a tap toggles between "tenho" (1) and "vazio" (0).
            Section.ALBUM -> { s -> vm.setCount(s, if (s.owned) 0 else 1) }
            // Faltam / Repetidas: each tap adds one copy.
            else -> { s -> vm.increment(s) }
        }
        TeamSheet(
            group = group,
            hint = hint,
            stickers = shownStickers,
            badgeOf = { s -> if (section == Section.REPETIDAS) s.duplicates else null },
            onTap = onTap,
            onInc = { vm.increment(it) },
            onDec = { vm.decrement(it) },
            onDismiss = { openTeamCode = null }
        )
    }
}

@Composable
private fun SectionPill(
    value: String,
    label: String,
    color: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) color else color.copy(alpha = 0.14f)
    val valueColor = if (selected) Color.White else color
    val labelColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(label, color = labelColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun DiscreetSearch(query: String, onQueryChange: (String) -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = muted, modifier = Modifier.size(18.dp))
        Box(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            if (query.isEmpty()) {
                Text("Buscar seleção", color = muted, fontSize = 14.sp)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Limpar",
                tint = muted,
                modifier = Modifier.size(18.dp).clickable { onQueryChange("") }
            )
        }
    }
}
