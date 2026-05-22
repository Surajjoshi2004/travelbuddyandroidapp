package com.example.tripbuddyandriodapp.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tripbuddyandriodapp.R
import com.example.tripbuddyandriodapp.ui.components.AttractionCard
import com.example.tripbuddyandriodapp.ui.components.WeatherCard
import com.example.tripbuddyandriodapp.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreTab(
    destination: String,
    viewModel: ExploreViewModel,
    onNavigateToAttractionDetail: (String) -> Unit
) {
    val uiState by viewModel.filteredUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LaunchedEffect(destination) {
        if (destination.isNotBlank() && uiState is ExploreUiState.Idle) {
            val now = System.currentTimeMillis()
            viewModel.loadExploreData(
                destination = destination,
                startDate = DateUtils.formatIso(now),
                endDate = DateUtils.formatIso(now + 86400000 * 7)
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search attractions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is ExploreUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ExploreUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
                    ) {
                        state.weather?.let {
                            WeatherCard(weather = it)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Text(text = "Top Attractions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.attractions) { attraction ->
                                AttractionCard(attraction = attraction, onClick = { onNavigateToAttractionDetail(attraction.xid) })
                            }
                        }

                        if (state.aiActivities.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "AI Picked Activities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            state.aiActivities.forEach { (title, desc) ->
                                AiActivityCard(title = title, description = desc)
                            }
                        }

                        if (state.aiFoods.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFFE91E63))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Local Food Delights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            state.aiFoods.forEach { food ->
                                AiInfoCard(text = food)
                            }
                        }

                        if (state.aiTips.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFFC107))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Local Travel Tips", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            state.aiTips.forEach { tip ->
                                AiInfoCard(text = tip)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                is ExploreUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                else -> {}
            }
        }
    }
}

@Composable
fun AiActivityCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AiInfoCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
    }
}
