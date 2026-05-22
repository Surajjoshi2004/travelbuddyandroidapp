package com.example.tripbuddyandriodapp.ui.screens.tabs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripbuddyandriodapp.data.local.PackingItemEntity
import com.example.tripbuddyandriodapp.ui.screens.TripDetailViewModel

@Composable
fun PackingTab(viewModel: TripDetailViewModel) {
    val items by viewModel.packingItems.collectAsState()
    val trip by viewModel.trip.collectAsState()
    val destination = trip?.destination ?: ""
    val aiSuggestions by viewModel.aiPackingSuggestions.collectAsState()
    val isFetching by viewModel.isFetchingSuggestions.collectAsState()
    val aiError by viewModel.aiError.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = items.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    val packedCount = items.count { it.isChecked }
    val totalCount = items.size
    val progress = if (totalCount > 0) packedCount.toFloat() / totalCount else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "packingProgress"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Progress header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF5C59D4)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Packing Progress", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "$packedCount / $totalCount items",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${(animatedProgress * 100).toInt()}% packed ✅",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }

        if (destination.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            if (aiError != null) {
                Text(
                    text = "AI Error: $aiError", 
                    color = MaterialTheme.colorScheme.error, 
                    style = MaterialTheme.typography.labelSmall
                )
            } else {
                Text(
                    text = "AI Suggestions for $destination", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = Color(0xFF5C59D4)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isFetching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = Color(0xFF5C59D4))
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (aiSuggestions.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(aiSuggestions) { suggestion ->
                        val isAlreadyAdded = items.any { it.name.equals(suggestion.first, ignoreCase = true) }
                        FilterChip(
                            selected = isAlreadyAdded,
                            onClick = { 
                                if (!isAlreadyAdded) {
                                    viewModel.addPackingItem(suggestion.first, suggestion.second)
                                }
                            },
                            label = { Text(suggestion.first) },
                            leadingIcon = if (isAlreadyAdded) {
                                { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            } else if (!isFetching && aiError == null) {
                Text("No suggestions available. Try refreshing.", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search items...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF5C59D4)) },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5C59D4),
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            val grouped = filteredItems.groupBy { it.category }
            grouped.forEach { (category, categoryItems) ->
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = category,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5C59D4),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF5C59D4).copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${categoryItems.count { it.isChecked }}/${categoryItems.size}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                items(categoryItems) { item ->
                    PackingItemRow(item = item, onToggle = { viewModel.togglePackingItem(item) })
                }
            }
        }
    }
}

@Composable
fun PackingItemRow(
    item: PackingItemEntity,
    onToggle: () -> Unit
) {
    val checkScale by animateFloatAsState(
        targetValue = if (item.isChecked) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkScale"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (item.isChecked) 0.45f else 1f,
        animationSpec = tween(200),
        label = "textAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(checkScale),
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF5C59D4),
                uncheckedColor = Color.LightGray
            )
        )
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
            modifier = Modifier.padding(start = 6.dp),
            color = Color(0xFF1A1A2E).copy(alpha = textAlpha)
        )
    }
}
