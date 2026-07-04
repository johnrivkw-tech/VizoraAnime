package com.example.animetracker.data

import android.util.Xml
import java.io.InputStream
import java.io.StringWriter
import org.xmlpull.v1.XmlPullParser

/**
 * A single <anime> entry parsed from a MyAnimeList XML export.
 * Mirrors the subset of fields the app cares about; [malId] is null
 * if the export didn't include a usable series id.
 */
data class ImportedMalAnime(
    val malId: Int?,
    val title: String,
    val episodesWatched: Int,
    val totalEpisodes: Int,
    val status: AnimeStatus,
    val rating: Int
)

/** Maps a MAL "my_status" string to the app's [AnimeStatus]. */
private fun malStatusToAnimeStatus(malStatus: String): AnimeStatus = when (malStatus.trim()) {
    "Watching" -> AnimeStatus.WATCHING
    "Completed" -> AnimeStatus.COMPLETED
    "Plan to Watch" -> AnimeStatus.PLAN_TO_WATCH
    "On-Hold" -> AnimeStatus.WATCHING
    "Dropped" -> AnimeStatus.PLAN_TO_WATCH
    else -> AnimeStatus.PLAN_TO_WATCH
}

/** Maps the app's [AnimeStatus] back to a MAL "my_status" string. */
private fun animeStatusToMalStatus(status: AnimeStatus): String = when (status) {
    AnimeStatus.WATCHING -> "Watching"
    AnimeStatus.COMPLETED -> "Completed"
    AnimeStatus.PLAN_TO_WATCH -> "Plan to Watch"
}

/**
 * Parses a MyAnimeList XML export (the standard `<myanimelist><anime>...</anime></myanimelist>`
 * format) into a list of [ImportedMalAnime].
 */
fun parseMalXml(input: InputStream): List<ImportedMalAnime> {
    val result = mutableListOf<ImportedMalAnime>()

    val parser: XmlPullParser = Xml.newPullParser()
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
    parser.setInput(input, null)

    var inAnime = false
    var currentTag: String? = null

    var malId: Int? = null
    var title = ""
    var episodesWatched = 0
    var totalEpisodes = 0
    var malStatus = ""
    var rating = 0

    fun resetEntry() {
        malId = null
        title = ""
        episodesWatched = 0
        totalEpisodes = 0
        malStatus = ""
        rating = 0
    }

    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                val tag = parser.name
                if (tag == "anime") {
                    inAnime = true
                    resetEntry()
                }
                currentTag = tag
            }
            XmlPullParser.TEXT -> {
                if (inAnime && currentTag != null) {
                    val text = parser.text?.trim().orEmpty()
                    if (text.isNotEmpty()) {
                        when (currentTag) {
                            "series_animedb_id" -> malId = text.toIntOrNull()
                            "series_title" -> title = text
                            "series_episodes" -> totalEpisodes = text.toIntOrNull() ?: 0
                            "my_watched_episodes" -> episodesWatched = text.toIntOrNull() ?: 0
                            "my_score" -> rating = text.toIntOrNull() ?: 0
                            "my_status" -> malStatus = text
                        }
                    }
                }
            }
            XmlPullParser.END_TAG -> {
                if (parser.name == "anime" && inAnime) {
                    if (title.isNotBlank()) {
                        result.add(
                            ImportedMalAnime(
                                malId = malId,
                                title = title,
                                episodesWatched = episodesWatched,
                                totalEpisodes = totalEpisodes,
                                status = malStatusToAnimeStatus(malStatus),
                                rating = rating.coerceIn(0, 10)
                            )
                        )
                    }
                    inAnime = false
                }
                currentTag = null
            }
        }
        eventType = parser.next()
    }

    return result
}

/** Escapes text for safe inclusion inside a CDATA section. */
private fun String.safeForCdata(): String = this.replace("]]>", "]]]]><![CDATA[>")

/**
 * Builds a MyAnimeList-compatible XML export string from the given list of [Anime].
 */
fun buildMalXml(animeList: List<Anime>): String {
    val writer = StringWriter()
    writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n")
    writer.append("<myanimelist>\n")

    animeList.forEach { anime ->
        writer.append("  <anime>\n")
        writer.append("    <series_animedb_id>${anime.malId ?: 0}</series_animedb_id>\n")
        writer.append("    <series_title><![CDATA[${anime.name.safeForCdata()}]]></series_title>\n")
        writer.append("    <series_episodes>${anime.totalEpisodes}</series_episodes>\n")
        writer.append("    <my_watched_episodes>${anime.episodesWatched}</my_watched_episodes>\n")
        writer.append("    <my_score>${anime.rating}</my_score>\n")
        writer.append("    <my_status>${animeStatusToMalStatus(anime.status)}</my_status>\n")
        writer.append("  </anime>\n")
    }

    writer.append("</myanimelist>\n")
    return writer.toString()
}
