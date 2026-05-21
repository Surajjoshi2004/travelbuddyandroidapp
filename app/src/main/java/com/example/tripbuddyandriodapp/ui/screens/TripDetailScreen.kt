package com.example.tripbuddyandriodapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tripbuddyandriodapp.R
import com.example.tripbuddyandriodapp.ui.screens.tabs.BudgetTab
import com.example.tripbuddyandriodapp.ui.screens.tabs.ExploreTab
import com.example.tripbuddyandriodapp.ui.screens.tabs.ItineraryTab
import com.example.tripbuddyandriodapp.ui.screens.tabs.PackingTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: Long,
    onBack: () -> Unit,
    onNavigateToAttractionDetail: (String) -> Unit,
    viewModel: TripDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(tripId) {
        viewModel.setTripId(tripId)
    }

    val trip by viewModel.trip.collectAsState()
    val days by viewModel.days.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.itinerary),
        stringResource(R.string.packing),
        stringResource(R.string.budget),
        stringResource(R.string.explore)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.destination ?: "", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share trip logic */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Menu logic */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ItineraryTab(days = days, viewModel = viewModel)
                1 -> PackingTab(viewModel = viewModel)
                2 -> BudgetTab(viewModel = viewModel)
                3 -> ExploreTab(
                    destination = trip?.destination ?: "", 
                    viewModel = hiltViewModel(),
                    onNavigateToAttractionDetail = onNavigateToAttractionDetail
                )
            }
        }
    }
}
