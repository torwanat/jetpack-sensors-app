package com.example.sensorapp.data

import kotlinx.coroutines.flow.Flow

class OfflineProfileRepository(private val profileDao: ProfileDao) : ProfileRepository {
    override fun getAllProfilesStream(): Flow<List<Profile>> = profileDao.getAllProfiles()

    override suspend fun insertProfile(profile: Profile) = profileDao.insert(profile)

    override suspend fun deleteProfile(profile: Profile) = profileDao.delete(profile)

    override suspend fun updateProfile(profile: Profile) = profileDao.update(profile)

    override suspend fun upsertProfile(profile: Profile) = profileDao.upsert(profile)
}