package com.example.tripbuddyandriodapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripbuddyandriodapp.data.remote.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isLogin = MutableStateFlow(true)
    val isLogin: StateFlow<Boolean> = _isLogin

    fun toggleMode() {
        _isLogin.value = !_isLogin.value
        _authState.value = AuthState.Idle
    }

    fun authenticate(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = if (_isLogin.value) {
                authApi.login(email, pass)
            } else {
                authApi.signUp(email, pass)
            }

            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Authentication failed")
            }
        }
    }
}
