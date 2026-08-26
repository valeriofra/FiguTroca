package com.figutroca.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.figutroca.app.ui.AppViewModel
import com.figutroca.app.ui.components.EmptyState
import com.figutroca.app.ui.components.StatPill
import com.figutroca.app.ui.components.TeamCard
import com.figutroca.app.ui.components.TeamSheet
import com.figutroca.app.ui.components.groupTeams
import com.figutroca.app.ui.theme.DuplicateAmber
import com.figutroca.app.ui.theme.MissingGray
import com.figutroca.app.ui.theme.OwnedGreen

@Composable
fun AlbumScreen(vm: AppViewModel, contentPadding: PaddingValues) {
    val album by vm.albumState.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val stats = album.stats
    var openTeamCode by remember { mutableStateOf<String?>(null) }

    if (album.activeCollection == null) {
        EmptyState(
            title = "Nenhuma coleção ativa",
            subtitle = "Vá até a aba Coleções para criar sua coleção da Copa.",
            modifier = Modifier.padding(contentPadding)
        )
        return
    }
    if (album.stickers.isEmpty()) {
        EmptyState(
            title = "Álbum vazio",
            subtitle = "Use o ícone de colar (topo) para importar sua lista, ou + para adicionar.",
            modifier = Modifier.padding(contentPadding)
        )
        return
    }

    val allTeams = remember(album.stickers) { groupTeams(album.stickers) }
    val teams = allTeams.filter {
        query.isBlank() || it.name.contains(query, true) || it.code.contains(query, true)
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
                ProgressCard(
                    name = album.activeCollection?.name ?: "",
                    owned = stats.owned,
                    total = stats.total,
                    completion = stats.completion
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill("${stats.owned}", "Tenho", OwnedGreen, Modifier.weight(1f))
                    StatPill("${stats.missing}", "Faltam", MissingGray, Modifier.weight(1f))
                    StatPill("${stats.duplicates}", "Repetidas", DuplicateAmber, Modifier.weight(1f))
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = vm::setQuery,
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    placeholder = { Text("Buscar seleção") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Toque numa seleção para abrir as figurinhas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(teams, key = { it.code }) { group ->
            TeamCard(group = group, onClick = { openTeamCode = group.code })
        }

        if (teams.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "Nenhuma seleção encontrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }

    openGroup?.let { group ->
        TeamSheet(
            group = group,
            onInc = { vm.increment(it) },
            onDec = { vm.decrement(it) },
            onDismiss = { openTeamCode = null }
        )
    }
}

@Composable
private fun ProgressCard(name: String, owned: Int, total: Int, completion: Float) {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(name, style = MaterialTheme.typography.headlineMedium)
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "$owned de $total figurinhas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${(completion * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { completion },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}
