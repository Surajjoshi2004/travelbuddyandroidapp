package com.example.tripbuddyandriodapp.ui.screens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _theme = MutableStateFlow(AppTheme.SYSTEM)
    val theme: StateFlow<AppTheme> = _theme

    private val _lastSynced = MutableStateFlow<String?>(null)
    val lastSynced: StateFlow<String?> = _lastSynced

    fun setTheme(theme: AppTheme) {
        _theme.value = theme
    }

    fun syncNow() {
        // Trigger SyncManager
    }

    fun clearAllData() {
        // Database clear logic
    }
}
