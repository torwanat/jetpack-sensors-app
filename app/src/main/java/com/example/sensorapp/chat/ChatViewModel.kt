package com.example.sensorapp.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorapp.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel (val profileRepository: ProfileRepository) : ViewModel() {
    private val _chatUiState = MutableStateFlow(ChatUiState())
    val chatUiState = _chatUiState.asStateFlow()

    private val _usernameFlow = MutableStateFlow("")

    private val _filenameFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            profileRepository.getAllProfilesStream()
                .collect { profiles ->
                    val profile = profiles.lastOrNull()
                    val username = profile?.username ?: ""
                    val filename = profile?.filename ?: ""

                    _chatUiState.value = ChatUiState(username = username, filename = filename)
                    _usernameFlow.value = username
                    _filenameFlow.value = filename
                }
        }
    }
}

data class ChatUiState(val username: String = "", val filename: String = "")