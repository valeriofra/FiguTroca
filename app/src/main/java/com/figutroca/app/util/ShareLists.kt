package com.figutroca.app.util

import android.content.Context
import android.content.Intent
import com.figutroca.app.data.Sticker

/** Builds the classic collector text lists used to trade in chat groups. */
object ShareLists {

    /** e.g. "Faltam (3): 5, 18, 220" */
    fun missing(collectionName: String, stickers: List<Sticker>): String {
        val missing = stickers.filter { it.missing }
        val codes = missing.joinToString(", ") { it.code }
        return buildString {
            append(collectionName).append('\n')
            append("Faltam (").append(missing.size).append("): ")
            append(if (codes.isEmpty()) "nenhuma 🎉" else codes)
        }
    }

    /** e.g. "Repetidas (4): 7, 7, 12, 30"  (a code repeats once per spare copy) */
    fun duplicates(collectionName: String, stickers: List<Sticker>): String {
        val dupes = stickers.filter { it.duplicates > 0 }
        val total = dupes.sumOf { it.duplicates }
        val codes = dupes.flatMap { s -> List(s.duplicates) { s.code } }.joinToString(", ")
        return buildString {
            append(collectionName).append('\n')
            append("Repetidas (").append(total).append("): ")
            append(if (codes.isEmpty()) "nenhuma" else codes)
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
