package com.example.animetracker.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.animetracker.ui.components.AnimeGridPosterCard
import com.example.animetracker.ui.components.InfoMessage
import com.example.animetracker.ui.model.toHomeCardItem
import com.example.animetracker.viewmodel.AnimeViewModel

private enum class DiscoverTab { GENRES, SEASON }

/**
 * Browse tab: genre chips backed by Jikan's genre list (each tap re-queries
 * anime for that genre), plus a "This Season" grid. Both share the poster
 * grid + status-badge pattern used on the Search tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(viewModel: AnimeViewModel, onAnimeClick: (Int) -> Unit) {
    var tab by remember { mutableStateOf(DiscoverTab.GENRES) }

    LaunchedEffect(Unit) {
        viewModel.loadGenresIfNeeded()
        viewModel.loadSeasonBrowseIfNeeded()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Discover") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = tab == DiscoverTab.GENRES,
                    onClick = { tab = DiscoverTab.GENRES },
                    label = { Text("Genres") }
                )
                FilterChip(
                    selected = tab == DiscoverTab.SEASON,
                    onClick = { tab = DiscoverTab.SEASON },
                    label = { Text("This Season") }
                )
            }

            when (tab) {
                DiscoverTab.GENRES -> GenreBrowse(viewModel, onAnimeClick)
                DiscoverTab.SEASON -> SeasonBrowse(viewModel, onAnimeClick)
            }
        }
    }
}

@Composable
private fun GenreBrowse(viewModel: AnimeViewModel, onAnimeClick: (Int) -> Unit) {
    val genres by viewModel.genres.collectAsState()
    val isGenresLoading by viewModel.isGenresLoading.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val genreAnime by viewModel.genreAnime.collectAsState()
    val isGenreAnimeLoading by viewModel.isGenreAnimeLoading.collectAsState()
    val genreAnimeError by viewModel.genreAnimeError.collectAsState()
    val localByMalId by viewModel.localByMalId.collectAsState()

    val posterItems = remember(genreAnime, localByMalId) {
        genreAnime.map { it.toHomeCardItem(localByMalId[it.mal_id]) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isGenresLoading && genres.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genres.forEach { genre ->
                    FilterChip(
                        selected = selectedGenre?.mal_id == genre.mal_id,
                        onClick = { viewModel.selectGenre(genre) },
                        label = { Text(genre.name) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isGenreAnimeLoading && posterItems.isEmpty() -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                genreAnimeError != null && posterItems.isEmpty() -> InfoMessage(
                    icon = Icons.Default.WifiOff,
                    title = "Couldn't load",
                    subtitle = genreAnimeError.orEmpty(),
                    actionLabel = "Retry",
                    onAction = { viewModel.retryGenreAnime() }
                )
                selectedGenre == null -> InfoMessage(
                    icon = Icons.Default.Category,
                    title = "Pick a genre",
                    subtitle = "Choose a genre above to start browsing"
                )
                posterItems.isEmpty() -> InfoMessage(
                    icon = Icons.Default.SearchOff,
                    title = "No results",
                    subtitle = "Nothing found for ${selectedGenre?.name}"
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(posterItems, key = { it.key }) { item ->
                        AnimeGridPosterCard(
                            item = item,
                            onClick = { item.malId?.let(onAnimeClick) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonBrowse(viewModel: AnimeViewModel, onAnimeClick: (Int) -> Unit) {
    val seasonAnime by viewModel.seasonAnime.collectAsState()
    val isLoading by viewModel.isSeasonLoading.collectAsState()
    val error by viewModel.seasonError.collectAsState()
    val localByMalId by viewModel.localByMalId.collectAsState()

    val posterItems = remember(seasonAnime, localByMalId) {
        seasonAnime.map { it.toHomeCardItem(localByMalId[it.mal_id]) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading && posterItems.isEmpty() -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
            error != null && posterItems.isEmpty() -> InfoMessage(
                icon = Icons.Default.WifiOff,
                title = "Couldn't load this season",
                subtitle = error.orEmpty(),
                actionLabel = "Retry",
                onAction = { viewModel.loadSeasonBrowse() }
            )
            posterItems.isEmpty() -> InfoMessage(
                icon = Icons.Default.SearchOff,
                title = "Nothing found",
                subtitle = "No seasonal anime available right now"
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posterItems, key = { it.key }) { item ->
                    AnimeGridPosterCard(
                        item = item,
                        onClick = { item.malId?.let(onAnimeClick) }
                    )
                }
            }
        }
    }
}
