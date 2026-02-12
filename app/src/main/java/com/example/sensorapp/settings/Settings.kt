package com.example.sensorapp.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.sensorapp.AppViewModelProvider
import com.example.sensorapp.navigation.Routes
import com.example.sensorapp.ui.theme.SensorAppTheme
import java.io.File
import java.io.OutputStream
import java.util.UUID

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val settingsUiState by viewModel.settingsUiState.collectAsState()

    SettingsScreenBody(
        navController = navController,
        settingsUiState = settingsUiState,
        onUsernameChange = { viewModel.updateUsername(it) },
        onImageChange = { viewModel.updateImage(it) }
    )
}

@Composable
fun SettingsScreenBody(
    navController: NavController,
    settingsUiState: SettingsViewModel.SettingsUiState,
    onUsernameChange: (String) -> Unit,
    onImageChange: (String) -> Unit
){
    SensorAppTheme {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PhotoContainer(settingsUiState, onImageChange)
            Text(
                text = "Username",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(all = 10.dp)
            )
            TextField(
                value = settingsUiState.username,
                onValueChange = onUsernameChange
                )
            Button(onClick = {
                navController.popBackStack(Routes.CHAT, false)
            }) {
                Text(text = "Go back to chat")
            }
        }
    }
}

@Composable
fun PhotoContainer(settingsUiState: SettingsViewModel.SettingsUiState, onImageChange: (String) -> Unit){
    val savedImageFileName = settingsUiState.filename
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            onImageChange(context.savePhoto(it))
        }
    }

    Column (
        modifier = Modifier.padding(16.dp),
    ) {
        if (savedImageFileName.isNotEmpty()) {
            val imageFile = File(context.filesDir, savedImageFileName)
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp)
                        .padding(10.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
            launcher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Text(text = "Choose a photo")
        }

    }
}

fun Context.savePhoto(photoUri: Uri) : String {
    val fileName = "profile_image_${UUID.randomUUID()}.jpg"
    val inputStream = contentResolver.openInputStream(photoUri)
    val outputStream: OutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    inputStream?.close()
    outputStream.close()
    return fileName
}
