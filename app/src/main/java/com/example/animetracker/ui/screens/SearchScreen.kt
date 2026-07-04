package com.example.animetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.animetracker.ui.components.AnimeGridPosterCard
import com.example.animetracker.ui.components.InfoMessage
import com.example.animetracker.ui.model.toHomeCardItem
import com.example.animetracker.viewmodel.AnimeViewModel

/**
 * Full-catalog search tab. Distinct from [SearchAnimeDialog] (the modal used
 * from My List to quickly add a title) — this is a persistent, browsable
 * screen for finding and jumping into anything on MyAnimeList via Jikan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: AnimeViewModel, onAnimeClick: (Int) -> Unit) {
    val query by viewModel.catalogQuery.collectAsState()
    val results by viewModel.catalogResults.collectAsState()
    val isLoading by viewModel.isCatalogSearching.collectAsState()
    val error by viewModel.catalogSearchError.collectAsState()
    val localByMalId by viewModel.localByMalId.collectAsState()

    val items = remember(results, localByMalId) {
        results.map { it.toHomeCardItem(localByMalId[it.mal_id]) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Search") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onCatalogQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search the whole catalog") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onCatalogQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    query.isBlank() -> InfoMessage(
                        icon = Icons.Default.Search,
                        title = "Find any anime",
                        subtitle = "Search MyAnimeList's full catalog by title"
                    )
                    isLoading && items.isEmpty() -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                    error != null && items.isEmpty() -> InfoMessage(
                        icon = Icons.Default.WifiOff,
                        title = "Couldn't search",
                        subtitle = error.orEmpty(),
                        actionLabel = "Retry",
                        onAction = { viewModel.retryCatalogSearch() }
                    )
                    items.isEmpty() -> InfoMessage(
                        icon = Icons.Default.SearchOff,
                        title = "No matches",
                        subtitle = "Try a different title or check the spelling"
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items, key = { it.key }) { item ->
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
}

