package com.figutroca.app.util

import android.content.Context
import android.content.Intent
import com.figutroca.app.data.Sticker
import com.figutroca.app.data.Teams

/**
 * Builds the collector text lists used to trade, grouped one selection per
 * line and ordered like the official album.
 */
object ShareLists {

    private fun number(s: Sticker): String =
        if (s.code.contains(' ')) s.code.substringAfter(' ') else s.code

    private fun teamsInOrder(stickers: List<Sticker>): List<Pair<String, List<Sticker>>> =
        stickers.groupBy { it.code.substringBefore(' ') }
            .toList()
            .sortedWith(compareBy({ Teams.orderIndex(it.first) }, { it.first }))
            .map { (code, list) -> code to list.sortedBy { it.sortKey } }

    /** e.g. "Faltam (290):\nFWC: 4, 7\nBRA: 8, 9, 12" */
    fun missing(collectionName: String, stickers: List<Sticker>): String {
        val total = stickers.count { it.missing }
        val lines = teamsInOrder(stickers).mapNotNull { (code, list) ->
            val nums = list.filter { it.missing }
            if (nums.isEmpty()) null
            else "$code: " + nums.joinToString(", ") { number(it) }
        }
        return buildString {
            append(collectionName).append('\n')
            append("Faltam (").append(total).append("):")
            if (lines.isEmpty()) append(" nenhuma 🏆") else lines.forEach { append('\n').append(it) }
        }
    }

    /** e.g. "Repetidas (288):\nPOR: 8(2), 12(2)" — number(extras) when > 1 extra. */
    fun duplicates(collectionName: String, stickers: List<Sticker>): String {
        val total = stickers.sumOf { it.duplicates }
        val lines = teamsInOrder(stickers).mapNotNull { (code, list) ->
            val dupes = list.filter { it.duplicates > 0 }
            if (dupes.isEmpty()) null
            else "$code: " + dupes.joinToString(", ") { s ->
                val extra = s.duplicates
                if (extra > 1) "${number(s)}($extra)" else number(s)
            }
        }
        return buildString {
            append(collectionName).append('\n')
            append("Repetidas (").append(total).append("):")
            if (lines.isEmpty()) append(" nenhuma") else lines.forEach { append('\n').append(it) }
        }
    }

    fun share(context: Context, text: String, subject: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, subject))
    }
}
