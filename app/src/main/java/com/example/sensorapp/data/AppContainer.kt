package com.example.sensorapp.data

import android.content.Context

interface AppContainer {
    val profileRepository: ProfileRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val profileRepository: ProfileRepository by lazy {
        OfflineProfileRepository(ProfileDatabase.getDatabase(context).profileDao())
    }
}