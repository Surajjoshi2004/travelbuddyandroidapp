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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tripbuddyandriodapp.data.local.TripEntity
import com.example.tripbuddyandriodapp.ui.screens.tabs.*
import com.example.tripbuddyandriodapp.utils.DateUtils

// ── Design Tokens ─────────────────────────────────────────────
private val BrandPurple    = Color(0xFF5C59D4)
private val BrandPurpleLight = Color(0xFFEBEBFF)
private val BrandGreen     = Color(0xFF4CAF50)
private val SurfaceWhite   = Color(0xFFFFFFFF)
private val BackgroundPage = Color(0xFFF6F6FB)
private val TextPrimary    = Color(0xFF1A1A2E)
private val TextSecondary  = Color(0xFF8A8A9A)
private val CardBorder     = Color(0xFFEEEEF5)

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

    LaunchedEffect(uiState.ongoingTrip, uiState.upcomingTrips) {
        val activeTrip = uiState.ongoingTrip ?: uiState.upcomingTrips.firstOrNull()
        activeTrip?.let { detailViewModel.setTripId(it.id) }
    }

    Scaffold(
        containerColor = BackgroundPage,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWhite,
                tonalElevation = 0.dp,
                windowInsets = NavigationBarDefaults.windowInsets,
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                val items = listOf(
                    HomeTabItemData("Home",      Icons.Filled.Home,                 Icons.Outlined.Home),
                    HomeTabItemData("Itinerary", Icons.Filled.CalendarMonth,        Icons.Outlined.CalendarMonth),
                    HomeTabItemData("Packing",   Icons.Filled.Backpack,             Icons.Outlined.Backpack),
                    HomeTabItemData("Budget",    Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
                    HomeTabItemData("Explore",   Icons.Filled.Explore,              Icons.Outlined.Explore)
                )
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selectedTab == index) BrandPurpleLight
                                        else Color.Transparent
                                    )
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                item.label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = BrandPurple,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor   = BrandPurple,
                            unselectedTextColor = TextSecondary,
                            indicatorColor      = Color.Transparent
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showCreateTripSheet = true },
                    containerColor = BrandPurple,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Trip", modifier = Modifier.size(26.dp))
                }
            }
        }
    ) { padding ->
        val activeTrip = uiState.ongoingTrip ?: uiState.upcomingTrips.firstOrNull()

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BackgroundPage)
        ) {
            when (selectedTab) {
                0 -> DashboardContent(uiState, onNavigateToTripDetail, onNavigateToSettings) { showCreateTripSheet = true }
                1 -> TabContentWrapper(activeTrip) {
                    ItineraryTab(days = detailViewModel.days.collectAsState().value, viewModel = detailViewModel)
                }
                2 -> TabContentWrapper(activeTrip) { PackingTab(viewModel = detailViewModel) }
                3 -> TabContentWrapper(activeTrip) { BudgetTab(viewModel = detailViewModel) }
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

data class HomeTabItemData(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// ── Empty Tab Wrapper ─────────────────────────────────────────
@Composable
fun TabContentWrapper(activeTrip: TripEntity?, content: @Composable () -> Unit) {
    if (activeTrip == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BrandPurpleLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Hiking,
                        null,
                        tint = BrandPurple,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "No trip selected",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Create or select a trip from the Home tab to start planning.",
                    textAlign = TextAlign.Center,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    } else {
        content()
    }
}

// ── Dashboard ─────────────────────────────────────────────────
@Composable
fun DashboardContent(
    uiState: HomeUiState,
    onNavigateToTripDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onCreateClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(20.dp))
            HomeHeader(onProfileClick = onNavigateToSettings)
        }

        if (uiState.ongoingTrip != null) {
            item {
                SectionLabel("ONGOING TRIP")
                Spacer(Modifier.height(8.dp))
                OngoingTripCard(
                    trip = uiState.ongoingTrip,
                    onClick = { onNavigateToTripDetail(uiState.ongoingTrip.id) }
                )
            }
        }

        item {
            SectionLabel("UPCOMING TRIPS")
        }

        if (uiState.upcomingTrips.isEmpty() && uiState.ongoingTrip == null) {
            item { EmptyStatePlaceholder(onCreateClick = onCreateClick) }
        } else {
            items(uiState.upcomingTrips) { trip ->
                UpcomingTripCard(trip = trip, onClick = { onNavigateToTripDetail(trip.id) })
            }
        }

        item {
            SectionLabel("YOUR STATS")
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = uiState.totalTrips.toString(),
                    label = "Total trips",
                    icon = Icons.Default.FlightTakeoff,
                    iconColor = BrandPurple,
                    iconBg = BrandPurpleLight
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = uiState.citiesExplored.toString(),
                    label = "Cities explored",
                    icon = Icons.Default.Map,
                    iconColor = BrandGreen,
                    iconBg = Color(0xFFE8F5E9)
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

// ── Header ────────────────────────────────────────────────────
@Composable
fun HomeHeader(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good morning,",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Text(
                text = "Buddy 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(BrandPurple, Color(0xFF9C99F0)))
                )
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "BD",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

// ── Section Label ─────────────────────────────────────────────
@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp
    )
}

// ── Ongoing Trip Card ─────────────────────────────────────────
@Composable
fun OngoingTripCard(trip: TripEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF5C59D4), Color(0xFF9C99F0))
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "● LIVE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = trip.destination,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Group,
                        null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "${trip.travelers} Travelers",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
            Icon(
                Icons.Default.ArrowForwardIos,
                null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(16.dp)
            )
        }
    }
}

// ── Upcoming Trip Card ────────────────────────────────────────
@Composable
fun UpcomingTripCard(trip: TripEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandPurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Text("🏖️", fontSize = 22.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.destination,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${DateUtils.formatDayMonth(trip.startDate)} – ${DateUtils.formatDayMonth(trip.endDate)}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ── Stat Card ─────────────────────────────────────────────────
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(text = label, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

// ── Empty State ───────────────────────────────────────────────
@Composable
fun EmptyStatePlaceholder(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BrandPurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Hiking,
                null,
                tint = BrandPurple,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "No upcoming trips",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = TextPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Start planning your next adventure!",
            color = TextSecondary,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onCreateClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Plan an adventure", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Create Trip Bottom Sheet ──────────────────────────────────
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

    val selectedDateText = if (
        dateRangePickerState.selectedStartDateMillis != null &&
        dateRangePickerState.selectedEndDateMillis != null
    ) {
        val start = DateUtils.formatDate(dateRangePickerState.selectedStartDateMillis!!)
        val end   = DateUtils.formatDate(dateRangePickerState.selectedEndDateMillis!!)
        "$start – $end"
    } else "Select dates"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Handle
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(CardBorder)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "New Adventure",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = TextPrimary
            )
            Text(
                "Fill in the details below to get started.",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination") },
                leadingIcon = {
                    Icon(Icons.Outlined.LocationOn, null, tint = BrandPurple)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(14.dp))

            OutlinedCard(
                onClick = { showDateRangePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = BrandPurple, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = selectedDateText,
                        color = if (selectedDateText == "Select dates") TextSecondary else TextPrimary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Traveler counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundPage)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(Icons.Outlined.Group, null, tint = BrandPurple, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text("Travelers", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, color = TextPrimary)
                IconButton(
                    onClick = { if (travelers > 1) travelers-- },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                }
                Text(
                    travelers.toString(),
                    modifier = Modifier.padding(horizontal = 14.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                IconButton(
                    onClick = { if (travelers < 20) travelers++ },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Trip type", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Beach", "Business", "Trekking").forEach { item ->
                    FilterChip(
                        selected = template == item,
                        onClick = { template = item },
                        label = { Text(item, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPurple,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (
                        dateRangePickerState.selectedStartDateMillis != null &&
                        dateRangePickerState.selectedEndDateMillis != null
                    ) {
                        onSave(
                            destination,
                            dateRangePickerState.selectedStartDateMillis!!,
                            dateRangePickerState.selectedEndDateMillis!!,
                            travelers,
                            template
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                enabled = destination.length >= 2 && dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text("Save Trip", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDateRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = { showDateRangePicker = false }) { Text("OK") }
            }
        ) {
            DateRangePicker(state = dateRangePickerState, modifier = Modifier.weight(1f))
        }
    }
}