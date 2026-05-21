package com.example.tripbuddyandriodapp.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
        // Search Bar at the top
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search attractions or categories...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5C59D4),
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            ),
            singleLine = true
        )

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is ExploreUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ExploreUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        state.weather?.let {
                            WeatherCard(weather = it)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Text(
                            text = stringResource(R.string.must_visit),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (state.attractions.isEmpty()) {
                            Text(
                                text = "No attractions found matching your search.",
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            // Using a horizontal row structure that doesn't trigger measurement penalties
                            LazyRow(
                                contentPadding = PaddingValues(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.attractions, key = { it.xid }) { attraction ->
                                    AttractionCard(
                                        attraction = attraction,
                                        onClick = { onNavigateToAttractionDetail(attraction.xid) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.local_food),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LocalFoodSection(destination)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                is ExploreUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val now = System.currentTimeMillis()
                            viewModel.loadExploreData(destination, DateUtils.formatIso(now), DateUtils.formatIso(now + 86400000 * 7))
                        }) {
                            Text("Retry")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun LocalFoodSection(destination: String) {
    val foods = remember(destination) {
        when {
            destination.contains("Paris", ignoreCase = true) -> listOf("🥐 Croissant", "🍮 Crème Brûlée", "🥘 Ratatouille")
            destination.contains("Tokyo", ignoreCase = true) -> listOf("🍣 Sushi", "🍜 Ramen", "🐙 Takoyaki")
            destination.contains("Mumbai", ignoreCase = true) -> listOf("🍔 Vada Pav", "🥘 Pav Bhaji", "🐟 Bombil Fry")
            destination.contains("Goa", ignoreCase = true) -> listOf("🍛 Fish Curry", "🥥 Bebinca", "🍤 Prawn Balchão")
            else -> listOf("🍔 Burger", "🍕 Pizza", "🍝 Pasta")
        }
    }

    Column {
        foods.forEach { food ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = food,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}