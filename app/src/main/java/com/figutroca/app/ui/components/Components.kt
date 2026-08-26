package com.figutroca.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.figutroca.app.ui.theme.DuplicateAmber
import com.figutroca.app.ui.theme.OwnedGreen

/**
 * A single sticker cell in the album grid.
 * Tap = add one copy, long-press = remove one copy.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickerCell(
    sticker: Sticker,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val owned = sticker.owned
    val hasDupes = sticker.duplicates > 0

    val target = when {
        hasDupes -> DuplicateAmber
        owned -> OwnedGreen
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val bg by animateColorAsState(target, label = "stickerBg")
    val content = if (owned || hasDupes) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .then(
                if (!owned) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    RoundedCornerShape(14.dp)
                ) else Modifier
            )
            .combinedClickable(onClick = onTap, onLongClick = onLongPress),
        contentAlignment = Alignment.Center
    ) {
        // Codes like "BRA 13" render as a small team prefix over a big number.
        val parts = sticker.code.split(' ', limit = 2)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (parts.size == 2) {
                Text(
                    text = parts[0],
                    color = content.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
            Text(
                text = if (parts.size == 2) parts[1] else sticker.code,
                color = content,
                fontWeight = FontWeight.Bold,
                fontSize = if (parts.size == 2) 17.sp else 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        if (hasDupes) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+${sticker.duplicates}",
                    color = DuplicateAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun StatPill(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp).fillMaxWidth()
            )
        }
    }
}
