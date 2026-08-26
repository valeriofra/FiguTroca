package com.figutroca.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.figutroca.app.data.Sticker
import com.figutroca.app.ui.theme.MissingGray
import com.figutroca.app.ui.theme.OwnedGreen

private val RedBadge = Color(0xFFE4002B)

/** A team/section and its stickers, with progress numbers. */
data class TeamGroup(
    val code: String,
    val name: String,
    val stickers: List<Sticker>
) {
    val total: Int get() = stickers.size
    val owned: Int get() = stickers.count { it.owned }
    val missing: Int get() = total - owned
    val duplicates: Int get() = stickers.sumOf { it.duplicates }
}

/** Groups a flat sticker list by team code (prefix before the space). */
fun groupTeams(stickers: List<Sticker>): List<TeamGroup> {
    val specials = setOf("FWC", "CC")
    return stickers
        .groupBy { it.code.substringBefore(' ') }
        .map { (code, list) ->
            TeamGroup(code, list.firstOrNull()?.group?.ifBlank { code } ?: code, list.sortedBy { it.sortKey })
        }
        .sortedWith(compareBy({ if (it.code in specials) 0 else 1 }, { it.name }))
}

/** One team card on the home grid. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCard(group: TeamGroup, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    group.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (group.duplicates > 0) {
                    Box(
                        Modifier.size(22.dp).clip(CircleShape).background(RedBadge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${group.duplicates}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                "${group.owned}/${group.total}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { if (group.total == 0) 0f else group.owned.toFloat() / group.total },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

/** Popup with the team's stickers as rectangles; long-press one to edit with +/-. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSheet(
    group: TeamGroup,
    onInc: (Sticker) -> Unit,
    onDec: (Sticker) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedId by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(group.name, style = MaterialTheme.typography.titleLarge)
            Text(
                "${group.owned}/${group.total} · segure uma figurinha para ajustar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 72.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)
            ) {
                items(group.stickers, key = { it.id }) { s ->
                    StickerRect(
                        sticker = s,
                        selected = selectedId == s.id,
                        onSelect = { selectedId = s.id },
                        onDeselect = { selectedId = null },
                        onInc = { onInc(s) },
                        onDec = { onDec(s) }
                    )
                }
            }
        }
    }
}

/**
 * A sticker rectangle. Green = owned, gray = missing. A red badge on top shows
 * how many copies you have when it's more than one. Long-press (or tap) to
 * reveal -/+ controls; tap the middle to close them.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickerRect(
    sticker: Sticker,
    selected: Boolean,
    onSelect: () -> Unit,
    onDeselect: () -> Unit,
    onInc: () -> Unit,
    onDec: () -> Unit
) {
    val have = sticker.owned
    val bg = if (have) OwnedGreen else MissingGray.copy(alpha = 0.22f)
    val fg = if (have) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .then(
                if (!have) Modifier.border(
                    1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .combinedClickable(
                onClick = { if (selected) onDeselect() else onSelect() },
                onLongClick = onSelect
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepZone("−", Color.White.copy(alpha = 0.28f), fg, onDec)
                Text("${sticker.count}", color = fg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                StepZone("+", Color.White.copy(alpha = 0.28f), fg, onInc)
            }
        } else {
            val parts = sticker.code.split(' ', limit = 2)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (parts.size == 2) {
                    Text(parts[0], color = fg.copy(alpha = 0.85f), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    if (parts.size == 2) parts[1] else sticker.code,
                    color = fg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        if (sticker.count > 1) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(3.dp).size(19.dp).clip(CircleShape).background(RedBadge),
                contentAlignment = Alignment.Center
            ) {
                Text("${sticker.count}", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StepZone(
    label: String,
    bg: Color,
    fg: Color,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = fg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
