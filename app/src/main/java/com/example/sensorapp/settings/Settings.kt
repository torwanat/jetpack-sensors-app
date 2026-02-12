package com.example.sensorapp.settings

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.sensorapp.AppViewModelProvider
import com.example.sensorapp.MainActivity
import com.example.sensorapp.R
import com.example.sensorapp.constants.Notifications.CHANNEL_ID
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
            NotificationControls()
            LightSensorControls()
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

@Composable
fun NotificationControls() {
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}


    Button(onClick = {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_android_24)
            .setContentTitle("A notification")
            .setContentText("One of the notifications of all time")
            .setStyle(NotificationCompat.BigTextStyle().bigText("It even has an extendable body text"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@with
            }
            notify(0, builder.build())
        }
    }) {
        Text(text = "Send notification")
    }
}

@Composable
fun LightSensorControls() {
    val context = LocalContext.current
    var lightLevel by remember { mutableFloatStateOf(0f) }
    val decimalSymbols = DecimalFormatSymbols()
    decimalSymbols.decimalSeparator = ','
    val decimalFormatter = DecimalFormat("#.#", decimalSymbols)

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        if (it.sensor.type == Sensor.TYPE_LIGHT) {
                            if (lightLevel - it.values[0] > 200){
                                sendLightSensorNotification(context, decimalFormatter.format(it.values[0]), "darker")
                            } else if (lightLevel - it.values[0] < -200){
                                sendLightSensorNotification(context, decimalFormatter.format(it.values[0]), "brighter")
                            }
                            lightLevel = it.values[0]
                        }
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
        onDispose {}
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50.dp))
            .clip(RoundedCornerShape(50.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Light Level: ${decimalFormatter.format(lightLevel)}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

fun sendLightSensorNotification(context: Context, lightLevel: String, change: String) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.outline_brightness_7_24)
        .setContentTitle("Light level change")
        .setContentText("It is now $change")
        .setStyle(NotificationCompat.BigTextStyle().bigText("The current light level is $lightLevel"))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_EVENT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return@with
        }
        notify(1, builder.build())
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
