package com.example.animetracker.data.network

/**
 * Data models for the Jikan API (jikan.moe), which wraps MyAnimeList.
 * The same "anime object" shape is returned by search, top-anime, seasonal,
 * and full-details endpoints, so one model covers all of them — fields a
 * given endpoint doesn't provide simply come back null.
 */
data class JikanSearchResponse(
    val data: List<JikanAnimeResult> = emptyList()
)

data class JikanAnimeFullResponse(
    val data: JikanAnimeResult
)

data class JikanAnimeResult(
    val mal_id: Int,
    val title: String,
    val episodes: Int?,
    val score: Double?,
    val status: String?,
    val season: String?,
    val year: Int?,
    val synopsis: String?,
    val images: JikanImages,
    val genres: List<JikanNamedEntity> = emptyList(),
    val studios: List<JikanNamedEntity> = emptyList(),
    val trailer: JikanTrailer? = null,
    /** Raw duration string from Jikan, e.g. "24 min per ep" or "1 hr 44 min". */
    val duration: String? = null
)

data class JikanNamedEntity(
    val mal_id: Int,
    val name: String
)

data class JikanImages(
    val jpg: JikanImageUrls
)

data class JikanImageUrls(
    val image_url: String?,
    val large_image_url: String?
)

data class JikanTrailer(
    val youtube_id: String?,
    val url: String?,
    val embed_url: String?
)

data class JikanGenreResponse(
    val data: List<JikanGenre> = emptyList()
)

data class JikanGenre(
    val mal_id: Int,
    val name: String,
    val count: Int? = null
)

data class JikanCharactersResponse(
    val data: List<JikanCharacterEntry> = emptyList()
)

data class JikanCharacterEntry(
    val character: JikanCharacterInfo,
    val role: String
)

data class JikanCharacterInfo(
    val mal_id: Int,
    val name: String,
    val images: JikanImages
)

/**
 * Parses Jikan's free-text duration ("24 min per ep", "1 hr 44 min", "2 hr 3 min per ep")
 * into total minutes. Falls back to [fallback] (default: 24, a typical TV episode) if the
 * string is missing or doesn't contain a recognizable number.
 */
fun parseDurationMinutes(raw: String?, fallback: Int = 24): Int {
    if (raw.isNullOrBlank()) return fallback
    val hours = Regex("""(\d+)\s*hr""").find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    val minutes = Regex("""(\d+)\s*min""").find(raw)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    val total = hours * 60 + minutes
    return if (total > 0) total else fallback
}
