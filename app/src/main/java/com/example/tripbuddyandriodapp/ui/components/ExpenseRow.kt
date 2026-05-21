package com.example.tripbuddyandriodapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripbuddyandriodapp.data.local.ExpenseEntity
import com.example.tripbuddyandriodapp.utils.DateUtils

@Composable
fun ExpenseRow(
    expense: ExpenseEntity,
    modifier: Modifier = Modifier
) {
    val (icon, color) = getCategoryInfo(expense.category)

    // Animate amount text appearing
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = expense.category,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.category,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = DateUtils.formatDate(expense.date),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Surface(
            color = color.copy(alpha = 0.10f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "₹${String.format("%.2f", expense.amount)}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = color
            )
        }
    }
}

private fun getCategoryInfo(category: String): Pair<ImageVector, Color> {
    return when (category) {
        "Food" -> Icons.Default.Restaurant to Color(0xFFE57373)
        "Transport" -> Icons.Default.DirectionsCar to Color(0xFF64B5F6)
        "Stay" -> Icons.Default.Hotel to Color(0xFF81C784)
        "Shopping" -> Icons.Default.ShoppingBag to Color(0xFFFFB74D)
        else -> Icons.Default.Category to Color(0xFFBA68C8)
    }
}