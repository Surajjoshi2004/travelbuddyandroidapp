package com.example.tripbuddyandriodapp.ui.screens.tabs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripbuddyandriodapp.data.local.ActivityEntity
import com.example.tripbuddyandriodapp.data.local.DayEntity
import com.example.tripbuddyandriodapp.ui.screens.TripDetailViewModel
import com.example.tripbuddyandriodapp.utils.DateUtils
import kotlinx.coroutines.launch

// ── Refined Design Tokens (Synced with HomeScreen) ────────────
private val BrandPurple      = Color(0xFF5C59D4)
private val BrandPurpleLight = Color(0xFFF0F0FF)
private val BrandOrange      = Color(0xFFFF9F43) 
private val BrandBlue        = Color(0xFF4834D4) 
private val BrandTeal        = Color(0xFF22A6B3)
private val BrandSurface     = Color(0xFFFFFFFF)
private val BackgroundPage   = Color(0xFFF8F9FE)
private val TextPrimary      = Color(0xFF1A1A2E)
private val TextSecondary    = Color(0xFF95AFC0)
private val DividerColor     = Color(0xFFEFEFF7)

@Composable
fun ItineraryTab(
    days: List<DayEntity>,
    viewModel: TripDetailViewModel
) {
    var showAddActivitySheetForDayId by remember { mutableStateOf<Long?>(null) }
    val trip by viewModel.trip.collectAsState()
    val destination = trip?.destination ?: ""
    val aiSuggestions by viewModel.aiActivitySuggestions.collectAsState()
    val isGenerating by viewModel.isGeneratingItinerary.collectAsState()
    val isFetchingSuggestions by viewModel.isFetchingSuggestions.collectAsState()
    val aiError by viewModel.aiError.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedDayIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundPage)) {
        if (days.isEmpty()) {
            EmptyItineraryState()
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Enhanced Day Selector (Pills) ──
                DaySelectorPills(
                    days = days,
                    selectedIndex = selectedDayIndex,
                    onDaySelected = { index ->
                        selectedDayIndex = index
                        coroutineScope.launch {
                            // Scroll to header (0) + index + 1
                            listState.animateScrollToItem(index + 1)
                        }
                    }
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp) // Adjusted for Nav Bar + FAB
                ) {
                    item {
                        ItineraryHeader(destination)
                    }

                    itemsIndexed(days, key = { _, day -> day.id }) { index, day ->
                        DayExpandableCard(
                            day = day,
                            viewModel = viewModel,
                            isGenerating = isGenerating,
                            isInitiallyExpanded = index == 0,
                            onAddActivity = { showAddActivitySheetForDayId = day.id }
                        )
                    }
                }
            }
        }

        if (isGenerating) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = BrandPurple,
                trackColor = BrandPurpleLight
            )
        }

        // Action Button
        FloatingActionButton(
            onClick = { if (days.isNotEmpty()) showAddActivitySheetForDayId = days.first().id },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            containerColor = BrandPurple,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
        }
    }

    if (showAddActivitySheetForDayId != null) {
        AddActivityBottomSheet(
            destination = destination,
            suggestions = aiSuggestions,
            isFetching = isFetchingSuggestions,
            error = aiError,
            onRefresh = { viewModel.refreshAiSuggestions() },
            onSurpriseMe = { viewModel.getAiSurpriseActivity() },
            onDismiss = { showAddActivitySheetForDayId = null },
            onSave = { time, title, desc ->
                viewModel.addActivity(showAddActivitySheetForDayId!!, time, title, desc)
                showAddActivitySheetForDayId = null
            }
        )
    }
}

@Composable
fun DaySelectorPills(days: List<DayEntity>, selectedIndex: Int, onDaySelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BrandSurface,
        shadowElevation = 2.dp
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(days) { index, day ->
                val isSelected = selectedIndex == index
                val bgColor by animateColorAsState(if (isSelected) BrandPurple else BrandPurpleLight, label = "bgColor")
                val contentColor by animateColorAsState(if (isSelected) Color.White else BrandPurple, label = "contentColor")
                val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")

                Surface(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clickable { onDaySelected(index) },
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor,
                    border = if (!isSelected) BorderStroke(1.dp, BrandPurple.copy(alpha = 0.1f)) else null
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                        Text(
                            text = "${day.dayNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItineraryHeader(destination: String) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(
            text = "Trip Timeline",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = TextPrimary
        )
        if (destination.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Explore, null, tint = BrandPurple, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = destination,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayExpandableCard(
    day: DayEntity,
    viewModel: TripDetailViewModel,
    isGenerating: Boolean,
    isInitiallyExpanded: Boolean,
    onAddActivity: () -> Unit
) {
    var expanded by remember { mutableStateOf(isInitiallyExpanded) }
    val activities by viewModel.getActivitiesForDay(day.id).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Enhanced Date Badge
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(BrandPurple, Color(0xFF7B78E8)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = DateUtils.formatDate(day.date).take(3).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = DateUtils.formatDate(day.date).split(" ").getOrNull(1) ?: "${day.dayNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Day ${day.dayNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = DateUtils.formatDate(day.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(400)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(400)) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    if (activities.isNotEmpty()) {
                        activities.sortedBy { it.time }.forEachIndexed { index, activity ->
                            TimelineActivityItem(
                                activity = activity,
                                isLast = index == activities.size - 1,
                                onDelete = { viewModel.deleteActivity(activity) }
                            )
                        }
                    } else {
                        EmptyDayPrompt(
                            isGenerating = isGenerating,
                            onPlanDay = { viewModel.generateAiItineraryForDay(day.id, day.dayNumber) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = onAddActivity,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurpleLight, contentColor = BrandPurple),
                        elevation = null
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Add Activity", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineActivityItem(
    activity: ActivityEntity,
    isLast: Boolean,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color(0xFFFFEBEE) else Color.Transparent
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.DeleteForever, null, tint = Color.Red, modifier = Modifier.size(28.dp))
            }
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Timeline line and customized dot with color based on time
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(36.dp)
            ) {
                val accentColor = getTimeColor(activity.time)
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(3.dp, BrandSurface, CircleShape)
                )
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(120.dp) // Adjusted height
                            .background(Brush.verticalGradient(listOf(accentColor, DividerColor)))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Polished Activity Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundPage.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, DividerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                    // Category Icon
                    val timeColor = getTimeColor(activity.time)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(timeColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getActivityIcon(activity.title),
                            contentDescription = null,
                            tint = timeColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Timer, null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = activity.time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = TextSecondary.copy(alpha = 0.4f))
                            }
                        }
                        
                        Text(
                            text = activity.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontSize = 17.sp,
                            lineHeight = 22.sp
                        )
                        
                        if (activity.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = activity.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDayPrompt(isGenerating: Boolean, onPlanDay: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BrandPurpleLight.copy(alpha = 0.6f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, null, tint = BrandPurple, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Need a head start?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                "Let AI create a custom schedule for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onPlanDay,
                enabled = !isGenerating,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                modifier = Modifier.height(52.dp).padding(horizontal = 12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("AI Plan My Day", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyItineraryState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(140.dp).clip(CircleShape).background(BrandPurpleLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Map, null, tint = BrandPurple, modifier = Modifier.size(64.dp))
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text("No Plans Yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Your adventure begins here. Choose a day and start planning.", color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 48.dp), fontWeight = FontWeight.Bold)
    }
}

private fun getTimeColor(time: String): Color {
    val t = time.lowercase()
    return when {
        t.contains("am") -> BrandOrange
        t.contains("12 pm") || t.contains("1 pm") || t.contains("2 pm") || t.contains("3 pm") || t.contains("4 pm") -> BrandTeal
        else -> BrandBlue
    }
}

private fun getActivityIcon(title: String): ImageVector {
    val t = title.lowercase()
    return when {
        t.contains("eat") || t.contains("food") || t.contains("dinner") || t.contains("lunch") || t.contains("breakfast") || t.contains("restaurant") -> Icons.Default.Restaurant
        t.contains("flight") || t.contains("airport") || t.contains("plane") -> Icons.Default.Flight
        t.contains("hotel") || t.contains("stay") || t.contains("check-in") || t.contains("resort") -> Icons.Default.Hotel
        t.contains("walk") || t.contains("tour") || t.contains("hiking") -> Icons.AutoMirrored.Filled.DirectionsRun
        t.contains("beach") || t.contains("surf") || t.contains("pool") -> Icons.Default.BeachAccess
        t.contains("museum") || t.contains("art") || t.contains("gallery") -> Icons.Default.Museum
        t.contains("drink") || t.contains("bar") || t.contains("night") || t.contains("club") -> Icons.Default.LocalBar
        t.contains("train") || t.contains("metro") || t.contains("bus") -> Icons.Default.DirectionsTransit
        t.contains("car") || t.contains("drive") || t.contains("taxi") -> Icons.Default.DirectionsCar
        t.contains("shop") || t.contains("mall") || t.contains("market") -> Icons.Default.ShoppingBag
        else -> Icons.Default.Hiking
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityBottomSheet(
    destination: String,
    suggestions: List<Pair<String, String>>,
    isFetching: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onSurpriseMe: suspend () -> Pair<String, String>?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var time by remember { mutableStateOf("09:00 AM") }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isSurprising by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = DividerColor) },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 64.dp).fillMaxWidth().navigationBarsPadding().imePadding()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New Activity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = TextPrimary)
                if (destination.isNotBlank()) {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isFetching,
                        modifier = Modifier.background(BrandPurpleLight, CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp), tint = BrandPurple)
                    }
                }
            }

            if (destination.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = BrandPurple, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI SUGGESTIONS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = BrandPurple, letterSpacing = 1.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isFetching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape), color = BrandPurple, trackColor = BrandPurpleLight)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Surface(
                            onClick = {
                                scope.launch {
                                    isSurprising = true
                                    val surprise = onSurpriseMe()
                                    if (surprise != null) {
                                        title = surprise.first
                                        desc = surprise.second
                                    }
                                    isSurprising = false
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = TextPrimary,
                            contentColor = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isSurprising) "..." else "Surprise Me", fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    itemsIndexed(suggestions) { _, suggestion ->
                        AssistChip(
                            onClick = {
                                title = suggestion.first
                                desc = suggestion.second
                            },
                            label = { Text(suggestion.first, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(14.dp),
                            colors = AssistChipDefaults.assistChipColors(containerColor = BrandPurpleLight, labelColor = BrandPurple),
                            border = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Start Time") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Outlined.Timer, null, modifier = Modifier.size(22.dp)) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple, unfocusedBorderColor = DividerColor)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What are you doing?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple, unfocusedBorderColor = DividerColor)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Details (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPurple, unfocusedBorderColor = DividerColor)
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            Button(
                onClick = { onSave(time, title, desc) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Confirm Activity", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
    }
}
