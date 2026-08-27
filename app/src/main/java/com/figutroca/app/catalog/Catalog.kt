package com.figutroca.app.catalog

import org.json.JSONArray
import org.json.JSONObject

/**
 * One entry in the published catalog index — enough to render a "download"
 * card without fetching the full collection file. See README §3.
 *
 * `url` points at the [CollectionFile] JSON. `free` marks whether the
 * collection is available on the free plan; `collectible` distinguishes the
 * economy collections (which must be signed) from plain listings.
 */
data class CatalogEntry(
    val id: String,
    val name: String,
    val cover: String?,
    val url: String,
    val free: Boolean,
    val collectible: Boolean,
    val description: String?
)

/**
 * The catalog index: the list of collections the author has published. The app
 * downloads this from a public place (e.g. GitHub Pages) and shows it under
 * "Nova coleção". Users can only download from here — they can't author
 * collections themselves.
 */
data class Catalog(
    val schema: Int,
    val collections: List<CatalogEntry>
) {
    companion object {
        fun parse(json: String): Catalog {
            val o = JSONObject(json)
            val arr = o.optJSONArray("collections") ?: JSONArray()
            val entries = (0 until arr.length()).map { i ->
                val e = arr.getJSONObject(i)
                CatalogEntry(
                    id = e.optString("id", ""),
                    name = e.optString("name", "Coleção"),
                    cover = e.optString("cover").ifBlank { null },
                    url = e.optString("url", ""),
                    free = e.optBoolean("free", true),
                    collectible = e.optString("type", "listing")
                        .equals("collectible", ignoreCase = true),
                    description = e.optString("description").ifBlank { null }
                )
            }
            return Catalog(schema = o.optInt("schema", 1), collections = entries)
        }
    }
}
