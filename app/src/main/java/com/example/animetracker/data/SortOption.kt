package com.example.animetracker.data

/** Client-side sort applied to the My List screen. No schema/DB changes needed —
 *  "Recently Added" piggybacks on the auto-generated [Anime.id], which already
 *  increases with insertion order. */
enum class SortOption(val label: String) {
    TITLE("Title (A–Z)"),
    RECENTLY_ADDED("Recently Added"),
    RATING("Rating (High to Low)"),
    PROGRESS("Episodes Watched")
}
