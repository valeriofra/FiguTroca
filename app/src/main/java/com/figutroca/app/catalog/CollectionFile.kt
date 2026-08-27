package com.figutroca.app.catalog

import org.json.JSONArray
import org.json.JSONObject

/** One section (team/set) inside a downloadable collection file. */
data class SectionSpec(
    val code: String,
    val name: String,
    val nameEn: String?,
    val iso2: String?,
    val color: String?,
    val icon: String?,
    val from: Int?,
    val to: Int?,
    val labels: List<String>?
)

/**
 * A downloadable collection, as published in the catalog. See README §3 for the
 * format. `type` is "listing" (free physical-album organizer) or "collectible"
 * (economy). Loaded into the app via [com.figutroca.app.data.Repository.applyCollectionFile].
 */
data class CollectionFile(
    val schema: Int,
    val id: String,
    val name: String,
    val cover: String?,
    val type: String,
    val version: Int,
    val sections: List<SectionSpec>
) {
    val isCollectible: Boolean get() = type.equals("collectible", ignoreCase = true)

    companion object {
        fun parse(json: String): CollectionFile {
            val o = JSONObject(json)
            val secArr = o.optJSONArray("sections") ?: JSONArray()
            val sections = (0 until secArr.length()).map { i ->
                val s = secArr.getJSONObject(i)
                val labelsArr = s.optJSONArray("labels")
                SectionSpec(
                    code = s.getString("code").trim().uppercase(),
                    name = s.optString("name", s.getString("code")),
                    nameEn = s.optString("nameEn").ifBlank { null },
                    iso2 = s.optString("iso2").ifBlank { null },
                    color = s.optString("color").ifBlank { null },
                    icon = s.optString("icon").ifBlank { null },
                    from = if (s.has("from")) s.getInt("from") else null,
                    to = if (s.has("to")) s.getInt("to") else null,
                    labels = labelsArr?.let { la -> (0 until la.length()).map { la.getString(it) } }
                )
            }
            return CollectionFile(
                schema = o.optInt("schema", 1),
                id = o.optString("id", ""),
                name = o.optString("name", "Coleção"),
                cover = o.optString("cover").ifBlank { null },
                type = o.optString("type", "listing"),
                version = o.optInt("version", 1),
                sections = sections
            )
        }
    }
}
