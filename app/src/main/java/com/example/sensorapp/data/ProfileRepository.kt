package com.example.sensorapp.data

import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    fun getAllProfilesStream(): Flow<List<Profile>>

    suspend fun insertProfile(profile: Profile)

    suspend fun deleteProfile(profile: Profile)

    suspend fun updateProfile(profile: Profile)

    suspend fun upsertProfile(profile: Profile)
}
