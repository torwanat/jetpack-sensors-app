package com.example.sensorapp

import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sensorapp.data.SensorApplication
import com.example.sensorapp.settings.SettingsViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.example.sensorapp.chat.ChatViewModel


object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            SettingsViewModel(sensorApplication().container.profileRepository)
        }
        initializer {
            ChatViewModel(sensorApplication().container.profileRepository)
        }
    }
}

fun CreationExtras.sensorApplication(): SensorApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as SensorApplication)