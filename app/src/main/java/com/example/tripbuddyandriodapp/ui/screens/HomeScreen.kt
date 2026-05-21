package com.example.tripbuddyandriodapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tripbuddyandriodapp.R
import com.example.tripbuddyandriodapp.data.local.TripEntity
import com.example.tripbuddyandriodapp.ui.screens.tabs.*
import com.example.tripbuddyandriodapp.utils.DateUtils
import com.example.tripbuddyandriodapp.utils.TripStatusUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTripDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAttractionDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    detailViewModel: TripDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateTripSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val dashboardColor = Color(0xFF5C59D4)

    // Automatically sync the DetailViewModel with the active trip (ongoing or upcoming)
    LaunchedEffect(uiState.ongoingTrip, uiState.upcomingTrips) {
        val activeTrip = uiState.ongoingTrip ?: uiState.upcomingTrips.firstOrNull()
        activeTrip?.let { detailViewModel.setTripId(it.id) }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.height(72.dp)
            ) {
                val items = listOf(
                    HomeTabItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
                    HomeTabItem("Itinerary", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                    HomeTabItem("Packing", Icons.Filled.Backpack, Icons.Outlined.Backpack),
                    HomeTabItem("Budget", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                    HomeTabItem("Explore", Icons.Filled.Explore, Icons.Outlined.Explore)
                )
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { 
                            Icon(
                                imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon, 
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        label = { Text(item.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = dashboardColor,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showCreateTripSheet = true },
                    containerColor = dashboardColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Trip")
                }
            }
        }
    ) { padding ->
        val activeTrip = uiState.ongoingTrip ?: uiState.upcomingTrips.firstOrNull()
        
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFFBFBFF))) {
            when (selectedTab) {
                0 -> DashboardContent(uiState, onNavigateToTripDetail, onNavigateToSettings, { showCreateTripSheet = true })
                1 -> TabContentWrapper(activeTrip) { 
                    ItineraryTab(days = detailViewModel.days.collectAsState().value, viewModel = detailViewModel) 
                }
                2 -> TabContentWrapper(activeTrip) { 
                    PackingTab(viewModel = detailViewModel) 
                }
                3 -> TabContentWrapper(activeTrip) { 
                    BudgetTab(viewModel = detailViewModel) 
                }
                4 -> TabContentWrapper(activeTrip) { 
                    ExploreTab(
                        destination = activeTrip?.destination ?: "", 
                        viewModel = hiltViewModel(),
                        onNavigateToAttractionDetail = onNavigateToAttractionDetail
                    ) 
                }
            }
        }
    }

    if (showCreateTripSheet) {
        CreateTripBottomSheet(
            onDismiss = { showCreateTripSheet = false },
            onSave = { dest, s, e, trav, temp ->
                viewModel.addTrip(dest, s, e, trav, temp)
                showCreateTripSheet = false
            }
        )
    }
}

data class HomeTabItem(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

@Composable
fun TabContentWrapper(activeTrip: TripEntity?, content: @Composable () -> Unit) {
    if (activeTrip == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Hiking, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("No trip selected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Create or select a trip from the Home tab to start planning your itinerary.", 
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        content()
    }
}

@Composable
fun DashboardContent(
    uiState: HomeUiState,
    onNavigateToTripDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onCreateClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            HomeHeader(onProfileClick = onNavigateToSettings)
        }

        if (uiState.ongoingTrip != null) {
            item {
                SectionLabel(text = "ONGOING TRIP")
                OngoingTripCard(
                    trip = uiState.ongoingTrip, 
                    cardColor = Color(0xFF5C59D4),
                    onClick = { onNavigateToTripDetail(uiState.ongoingTrip.id) }
                )
            }
        }

        item {
            SectionLabel(text = "UPCOMING")
        }

        if (uiState.upcomingTrips.isEmpty() && uiState.ongoingTrip == null) {
            item { EmptyStatePlaceholder(onCreateClick = onCreateClick) }
        } else {
            items(uiState.upcomingTrips) { trip ->
                UpcomingTripCard(trip = trip, onClick = { onNavigateToTripDetail(trip.id) })
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(Modifier.weight(1f), uiState.totalTrips.toString(), "Total trips", Icons.Default.FlightTakeoff, Color(0xFF5C59D4))
                StatCard(Modifier.weight(1f), uiState.citiesExplored.toString(), "Cities explored", Icons.Default.Map, Color(0xFF4CAF50))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HomeHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Good morning,", color = Color.Gray, fontSize = 14.sp)
            Text(text = "Buddy 👋", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFEBEBFF))
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "BD", color = Color(0xFF5C59D4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun OngoingTripCard(trip: TripEntity, cardColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BeachAccess,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${trip.templateUsed.uppercase()} • ${trip.travelers} TRAVELERS",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = trip.destination,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OngoingInfoColumn("Day", "3 of 7")
                OngoingInfoColumn("Budget used", "₹12,400 / ₹20,000")
                OngoingInfoColumn("Weather", "31°C ☀️")
            }
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { 0.62f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Packing 62% done", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                Text(text = "Day 3", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun OngoingInfoColumn(label: String, value: String) {
    Column {
        Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun UpcomingTripCard(trip: TripEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if(trip.templateUsed == "Trekking") "🏔️" else "🏖️", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = trip.destination, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(
                    text = "${DateUtils.formatDayMonth(trip.startDate)} - ${DateUtils.formatDayMonth(trip.endDate)} • ${trip.travelers} travelers",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val daysRemaining = DateUtils.getDaysRemaining(trip.startDate)
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$daysRemaining days",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { 0.3f },
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFFEEEEEE)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "30%", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = iconColor, 
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text(text = label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EmptyStatePlaceholder(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Hiking, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No upcoming trips", fontWeight = FontWeight.Bold, color = Color.Gray)
        TextButton(onClick = onCreateClick) {
            Text("Plan your first adventure")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripBottomSheet(
    onDismiss: () -> Unit,
    onSave: (String, Long, Long, Int, String) -> Unit
) {
    var destination by remember { mutableStateOf("") }
    var travelers by remember { mutableStateOf(1) }
    var template by remember { mutableStateOf("Beach") }
    val dateRangePickerState = rememberDateRangePickerState()
    var showDateRangePicker by remember { mutableStateOf(false) }

    val selectedDateText = if (dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null) {
        val start = DateUtils.formatDate(dateRangePickerState.selectedStartDateMillis!!)
        val end = DateUtils.formatDate(dateRangePickerState.selectedEndDateMillis!!)
        "$start - $end"
    } else "Select Dates"

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(24.dp).fillMaxWidth().navigationBarsPadding().imePadding()) {
            Text("New Adventure", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = destination, onValueChange = { destination = it }, label = { Text("Destination") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedCard(onClick = { showDateRangePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = selectedDateText)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Travelers", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                IconButton(onClick = { if (travelers > 1) travelers-- }) { Icon(Icons.Default.Remove, null) }
                Text(travelers.toString(), modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
                IconButton(onClick = { if (travelers < 20) travelers++ }) { Icon(Icons.Default.Add, null) }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Template", fontWeight = FontWeight.Medium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Beach", "Business", "Trekking").forEach { item ->
                    FilterChip(selected = template == item, onClick = { template = item }, label = { Text(item) })
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { 
                    if (dateRangePickerState.selectedStartDateMillis != null && dateRangePickerState.selectedEndDateMillis != null) {
                        onSave(destination, dateRangePickerState.selectedStartDateMillis!!, dateRangePickerState.selectedEndDateMillis!!, travelers, template) 
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = destination.length >= 2 && dateRangePickerState.selectedEndDateMillis != null
            ) { Text("Save Trip", fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    if (showDateRangePicker) {
        DatePickerDialog(onDismissRequest = { showDateRangePicker = false }, confirmButton = { TextButton(onClick = { showDateRangePicker = false }) { Text("OK") } }) {
            DateRangePicker(state = dateRangePickerState, modifier = Modifier.weight(1f))
        }
    }
}
