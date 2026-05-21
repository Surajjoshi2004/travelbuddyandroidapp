package com.example.tripbuddyandriodapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tripbuddyandriodapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val theme by viewModel.theme.collectAsState()
    val lastSynced by viewModel.lastSynced.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = theme == AppTheme.SYSTEM,
                    onClick = { viewModel.setTheme(AppTheme.SYSTEM) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text(stringResource(R.string.system))
                }
                SegmentedButton(
                    selected = theme == AppTheme.LIGHT,
                    onClick = { viewModel.setTheme(AppTheme.LIGHT) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text(stringResource(R.string.light))
                }
                SegmentedButton(
                    selected = theme == AppTheme.DARK,
                    onClick = { viewModel.setTheme(AppTheme.DARK) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text(stringResource(R.string.dark))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Data Sync",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lastSynced?.let { stringResource(R.string.last_synced, it) } 
                    ?: stringResource(R.string.not_synced),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.syncNow() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sync_now))
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { viewModel.clearAllData() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.clear_data))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Trip Planner v1.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
