package com.example.tripbuddyandriodapp.ui.screens.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tripbuddyandriodapp.R
import com.example.tripbuddyandriodapp.data.local.DayEntity
import com.example.tripbuddyandriodapp.ui.components.ActivityRow
import com.example.tripbuddyandriodapp.ui.screens.TripDetailViewModel
import com.example.tripbuddyandriodapp.utils.DateUtils

@Composable
fun ItineraryTab(
    days: List<DayEntity>,
    viewModel: TripDetailViewModel
) {
    var showAddActivitySheetForDayId by remember { mutableStateOf<Long?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (days.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Initializing Itinerary...")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp)
            ) {
                items(days, key = { it.id }) { day ->
                    DayExpandableCard(
                        day = day,
                        viewModel = viewModel,
                        onAddActivity = { showAddActivitySheetForDayId = day.id }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { if (days.isNotEmpty()) showAddActivitySheetForDayId = days.first().id },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

    if (showAddActivitySheetForDayId != null) {
        AddActivityBottomSheet(
            onDismiss = { showAddActivitySheetForDayId = null },
            onSave = { time, title, desc ->
                viewModel.addActivity(showAddActivitySheetForDayId!!, time, title, desc)
                showAddActivitySheetForDayId = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayExpandableCard(
    day: DayEntity,
    viewModel: TripDetailViewModel,
    onAddActivity: () -> Unit
) {
    var expanded by remember { mutableStateOf(day.dayNumber == 1) }
    val activities by viewModel.getActivitiesForDay(day.id).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Day ${day.dayNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(DateUtils.formatDate(day.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    activities.forEach { activity ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteActivity(activity)
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
                                Box(Modifier.fillMaxSize().background(color).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                }
                            },
                            enableDismissFromStartToEnd = false
                        ) {
                            ActivityRow(activity = activity, onDelete = { viewModel.deleteActivity(activity) })
                        }
                    }
                    
                    TextButton(onClick = onAddActivity, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Activity")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityBottomSheet(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var time by remember { mutableStateOf("09:00 AM") }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp).fillMaxWidth().navigationBarsPadding().imePadding()) {
            Text("Add Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { onSave(time, title, desc) }, modifier = Modifier.fillMaxWidth(), enabled = title.isNotBlank()) {
                Text("Save Activity")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
