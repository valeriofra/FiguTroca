package com.figutroca.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    hint: String,
    stickers: List<Sticker>,
    badgeOf: (Sticker) -> Int?,
    locked: Boolean,
    onToggleLock: () -> Unit,
    onTap: (Sticker) -> Unit,
    onAddRange: (from: Int, to: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddRange by remember { mutableStateOf(false) }

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
                FilledTonalIconButton(onClick = { showAddRange = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Adicionar por intervalo")
                }
                FilledTonalIconButton(
                    onClick = onToggleLock,
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
                if (locked) "🔒 Travado — toque no cadeado para editar" else hint,
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
                    columns = GridCells.Adaptive(minSize = 116.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)
                ) {
                    items(stickers, key = { it.id }) { s ->
                        StickerRect(
                            sticker = s,
                            badge = badgeOf(s),
                            locked = locked,
                            onTap = { onTap(s) }
                        )
                    }
                }
            }
        }
    }

    if (showAddRange) {
        AddRangeDialog(
            teamCode = group.code,
            onDismiss = { showAddRange = false },
            onConfirm = { from, to -> onAddRange(from, to); showAddRange = false }
        )
    }
}

@Composable
private fun AddRangeDialog(teamCode: String, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    var from by remember { mutableStateOf("1") }
    var to by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar em $teamCode") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Cria as figurinhas $teamCode de um número ao outro (como faltando).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val f = from.toIntOrNull()
                    val t = to.toIntOrNull()
                    if (f != null && t != null) onConfirm(f, t)
                },
                enabled = from.toIntOrNull() != null && to.toIntOrNull() != null
            ) { Text("Adicionar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * A sticker styled like the ROMi collectible card. Everything that changes per
 * selection comes from the code prefix: the base colour is the background, the
 * flag sits in the yellow circle, the code shows vertically and on the banner,
 * and the country name is on the lower banner; the big number's colour adapts
 * to the background. Missing = an empty gray slot with just the number.
 * [badge] (when > 0) shows a red spare count. A tap runs the section action.
 */
@Composable
fun StickerRect(
    sticker: Sticker,
    badge: Int?,
    locked: Boolean,
    onTap: () -> Unit
) {
    val have = sticker.owned
    val prefix = sticker.code.substringBefore(' ')
    val number = if (sticker.code.contains(' ')) sticker.code.substringAfter(' ') else sticker.code
    val paddedNumber = number.toIntOrNull()?.let { "%02d".format(it) } ?: number
    val base = Color(Teams.color(prefix))

    Box(
        modifier = Modifier
            // Portrait collectible-card ratio (matches the ROMi sticker art: 12541 x 17277).
            .aspectRatio(0.726f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (have) base else MissingGray.copy(alpha = 0.18f))
            .then(
                if (!have) Modifier.border(
                    1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .clickable(enabled = !locked, onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        if (have) {
            OwnedCard(prefix = prefix, number = number, paddedNumber = paddedNumber)
        } else {
            Text(
                number,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun BoxScope.OwnedCard(
    prefix: String,
    number: String,
    paddedNumber: String
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        val u = w.value // card width in dp, used to scale text with the tile size
        val numberShadow = Shadow(color = Color(0x99000000), offset = Offset(0f, 2f), blurRadius = 4f)

        // The ROMi card artwork (player, accent shape, ball, banners, flag balloon,
        // vertical-code frame and ROMi logo). The team colour shows through behind it.
        Image(
            painter = painterResource(R.drawable.sticker_card),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        // Big sticker number (top-left). White with a soft shadow reads on the accent.
        Text(
            number,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (u * 0.15f).sp,
            lineHeight = (u * 0.15f).sp,
            style = TextStyle(shadow = numberShadow),
            modifier = Modifier.offset(x = w * 0.05f, y = h * 0.02f)
        )

        // Flag inside the yellow balloon (centred at 89.6% x, 62.5% y).
        Text(
            Teams.flag(prefix),
            fontSize = (u * 0.072f).sp,
            modifier = Modifier
                .offset(x = w * 0.896f, y = h * 0.625f)
                .offset(x = -(u * 0.045f).dp, y = -(u * 0.055f).dp)
        )

        // Code + number on the red banner.
        Text(
            "$prefix $paddedNumber",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (u * 0.085f).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .offset(x = w * 0.14f, y = h * 0.852f)
                .offset(y = -(u * 0.06f).dp)
        )

        // Country name on the lower banner.
        Text(
            Teams.name(prefix).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = (u * 0.048f).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .offset(x = w * 0.15f, y = h * 0.956f)
                .offset(y = -(u * 0.035f).dp)
        )

        // Country code stacked on the right edge (matches the outlined B/R/A).
        Column(
            modifier = Modifier.offset(x = w * 0.785f, y = h * 0.70f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            prefix.forEach { ch ->
                Text(
                    ch.toString(),
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = (u * 0.075f).sp,
                    lineHeight = (u * 0.092f).sp
                )
            }
        }
    }
}

