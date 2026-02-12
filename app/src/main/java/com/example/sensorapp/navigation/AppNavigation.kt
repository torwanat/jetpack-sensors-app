package com.example.sensorapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sensorapp.chat.Chat
import com.example.sensorapp.settings.SettingsScreen


@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.CHAT, builder = {
        composable(Routes.CHAT) {
            Chat(navController = navController)
        }
        composable(Routes.SECOND_SCREEN) {
            SettingsScreen(navController = navController)
        }
    })
}