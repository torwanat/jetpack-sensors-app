package com.example.sensorapp.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorapp.data.Profile
import com.example.sensorapp.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(val profileRepository: ProfileRepository) : ViewModel() {
    private val _settingsUiState = MutableStateFlow(SettingsUiState())
    val settingsUiState = _settingsUiState.asStateFlow()

    private val _usernameFlow = MutableStateFlow("")

    private val _filenameFlow = MutableStateFlow("")


    init {
        viewModelScope.launch {
            profileRepository.getAllProfilesStream()
                .collect { profiles ->
                    val profile = profiles.lastOrNull()
                    val username = profile?.username ?: ""
                    val filename = profile?.filename ?: ""

                    _settingsUiState.value = SettingsUiState(username = username, filename = filename)
                    _usernameFlow.value = username
                    _filenameFlow.value = filename
                }
        }

        viewModelScope.launch {
            _usernameFlow
                .debounce(500)
                .distinctUntilChanged()
                .collect { username ->
                    val currentProfile = profileRepository.getAllProfilesStream().first().lastOrNull()
                    if (currentProfile == null || currentProfile.username != username) {
                        saveProfile(username, _filenameFlow.value)
                    }
                }
        }

        viewModelScope.launch {
            _filenameFlow
                .debounce(500)
                .distinctUntilChanged()
                .collect { filename ->
                    val currentProfile = profileRepository.getAllProfilesStream().first().lastOrNull()
                    if (currentProfile == null || currentProfile.filename != filename) {
                        saveProfile(_usernameFlow.value, filename)
                    }
                }
        }
    }

    fun updateUsername(username: String) {
        _usernameFlow.value = username
        _settingsUiState.value = _settingsUiState.value.copy(username = username)
    }

    fun updateImage(filename: String) {
        _filenameFlow.value = filename
        _settingsUiState.value = _settingsUiState.value.copy(filename = filename)
    }

    private suspend fun saveProfile(username: String, filename: String) {
        profileRepository.insertProfile(Profile(username = username, filename = filename))
    }

    data class SettingsUiState(val username: String = "", val filename: String = "")
}
