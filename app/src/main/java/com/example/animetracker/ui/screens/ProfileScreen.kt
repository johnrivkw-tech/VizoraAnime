package com.example.animetracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.animetracker.viewmodel.AnimeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: AnimeViewModel) {
    val allAnime by viewModel.allLocalAnime.collectAsState()

    val totalEpisodesWatched = allAnime.sumOf { it.episodesWatched }
    // Uses each anime's real episode length (pulled from the online database
    // when added via search) instead of a flat estimate.
    val totalMinutes = allAnime.sumOf { it.episodesWatched * it.durationMinutes }
    val totalHours = totalMinutes / 60.0
    val totalDays = totalMinutes / (60.0 * 24.0)

    Scaffold(topBar = { TopAppBar(title = { Text("Profile") }) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your Watching Stats",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            StatCard(
                label = "Episodes Watched",
                value = totalEpisodesWatched.toString()
            )

            StatCard(
                label = "Time Spent Watching",
                value = "%.1f hours".format(totalHours)
            )

            StatCard(
                label = "That's Equivalent To",
                value = "%.1f days".format(totalDays)
            )

            Text(
                text = "Calculated using each show's actual episode length where known, " +
                    "or 24 minutes for manually-added entries.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
