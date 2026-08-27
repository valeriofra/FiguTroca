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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.figutroca.app.R
import com.figutroca.app.data.Sticker
import com.figutroca.app.data.Teams
import com.figutroca.app.ui.theme.MissingGray

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

/** Groups a flat sticker list by team code (prefix), in official album order. */
fun groupTeams(stickers: List<Sticker>): List<TeamGroup> =
    stickers
        .groupBy { it.code.substringBefore(' ') }
        .map { (code, list) ->
            TeamGroup(code, Teams.name(code), list.sortedBy { it.sortKey })
        }
        .sortedWith(compareBy({ Teams.orderIndex(it.code) }, { it.name }))

/**
 * A team card: flag on top, then the code (large) and the English + Portuguese
 * names. Painted in the selection's base colour. [badge] shows a red count
 * (used on the Repetidas section); [subtitle] is the section-specific line.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCard(
    group: TeamGroup,
    subtitle: String,
    badge: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val base = Color(Teams.color(group.code))
    val onBase = if (base.luminance() > 0.6f) Color(0xFF1A1A1A) else Color.White
    val soft = onBase.copy(alpha = 0.75f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(base)
            .clickable(onClick = onClick)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Flag dominates the card.
            Text(Teams.flag(group.code), fontSize = 60.sp, lineHeight = 64.sp)
            Text(
                group.code,
                color = onBase,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 26.sp
            )
            Text(
                Teams.enName(group.code),
                color = onBase,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                group.name,
                color = soft,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { if (group.total == 0) 0f else group.owned.toFloat() / group.total },
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                color = onBase,
                trackColor = onBase.copy(alpha = 0.25f),
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
            Text(subtitle, color = soft, fontSize = 10.sp, lineHeight = 12.sp, modifier = Modifier.padding(top = 3.dp))
        }
        if (badge != null && badge > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp).size(26.dp).clip(CircleShape).background(RedBadge),
                contentAlignment = Alignment.Center
            ) {
                Text("$badge", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Popup with a team's stickers as rectangles. [stickers] is already filtered
 * for the section; [badgeOf] decides the red count shown on each rectangle
 * (null = none). Long-press / tap a rectangle to reveal -/+.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSheet(
    group: TeamGroup,
    subtitle: String,
    stickers: List<Sticker>,
    badgeOf: (Sticker) -> Int?,
    onInc: (Sticker) -> Unit,
    onDec: (Sticker) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var locked by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(Teams.flag(group.code), fontSize = 26.sp)
                Text(
                    "${group.code} · ${group.name}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalIconButton(
                    onClick = { locked = !locked; if (locked) selectedId = null },
                    colors = if (locked) IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = RedBadge, contentColor = Color.White
                    ) else IconButtonDefaults.filledTonalIconButtonColors()
                ) {
                    Icon(
                        if (locked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                        contentDescription = if (locked) "Destravar edição" else "Travar edição"
                    )
                }
            }
            Text(
                if (locked) "🔒 Travado — toque no cadeado para editar"
                else "Toque num vazio para adicionar · segure para ajustar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (stickers.isEmpty()) {
                Text(
                    "Nada aqui nesta seção.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 72.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)
                ) {
                    items(stickers, key = { it.id }) { s ->
                        StickerRect(
                            sticker = s,
                            badge = badgeOf(s),
                            selected = selectedId == s.id,
                            locked = locked,
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
}

/**
 * A sticker rectangle styled like an album slot. Owned = the selection's base
 * colour with a faint player silhouette, the flag, the code and the number;
 * missing = an empty gray slot. [badge] (when > 0) shows a red count on top.
 * Long-press / tap reveals -/+ controls.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickerRect(
    sticker: Sticker,
    badge: Int?,
    selected: Boolean,
    locked: Boolean,
    onSelect: () -> Unit,
    onDeselect: () -> Unit,
    onInc: () -> Unit,
    onDec: () -> Unit
) {
    val have = sticker.owned
    val prefix = sticker.code.substringBefore(' ')
    val number = if (sticker.code.contains(' ')) sticker.code.substringAfter(' ') else sticker.code
    val base = Color(Teams.color(prefix))
    val onBase = if (base.luminance() > 0.6f) Color(0xFF1A1A1A) else Color.White
    val bg = if (have) base else MissingGray.copy(alpha = 0.18f)
    val fg = if (have) onBase else MaterialTheme.colorScheme.onSurfaceVariant

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
                onClick = {
                    if (locked) return@combinedClickable
                    when {
                        selected -> onDeselect()
                        !have -> onInc() // tap an empty slot to add it
                        else -> onSelect() // owned -> open the -/+ stepper
                    }
                },
                onLongClick = { if (!locked) onSelect() }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (have && !selected) {
            Icon(
                painter = painterResource(R.drawable.ic_player),
                contentDescription = null,
                tint = onBase.copy(alpha = 0.16f),
                modifier = Modifier.fillMaxSize().padding(top = 14.dp)
            )
        }

        if (selected) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepZone("−", fg, onDec)
                Text("${sticker.count}", color = fg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                StepZone("+", fg, onInc)
            }
        } else {
            if (have) {
                Text(
                    Teams.flag(prefix),
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                )
                Text(
                    prefix,
                    color = fg.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.5.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                )
            }
            Text(
                number,
                color = fg,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (badge != null && badge > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(3.dp).size(19.dp).clip(CircleShape).background(RedBadge),
                contentAlignment = Alignment.Center
            ) {
                Text("$badge", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StepZone(label: String, fg: Color, onClick: () -> Unit) {
    Box(
        Modifier.weight(1f).fillMaxHeight().clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.28f)),
            contentAlignment = Alignment.Center
        ) {
            Text(label, color = fg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
