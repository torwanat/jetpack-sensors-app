package com.example.sensorapp.chat

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.sensorapp.AppViewModelProvider
import com.example.sensorapp.R
import com.example.sensorapp.SampleData
import com.example.sensorapp.navigation.Routes
import com.example.sensorapp.ui.theme.SensorAppTheme
import java.io.File

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message, author: String, imageFile: File) {
    Row {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageFile)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = author,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun Conversation(messages: List<Message>, modifier: Modifier, navController: NavController, chatUiState: ChatUiState) {
    val context = LocalContext.current
    val imageFile = File(context.filesDir, chatUiState.filename)


    Column(modifier) {
        Row(
            Modifier
                .padding(bottom = 10.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.End,

        ) {
            IconButton(onClick = {
                navController.navigate(Routes.SECOND_SCREEN)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_settings_24),
                    contentDescription = "Settings button"
                )
            }
        }
        LazyColumn {
            items(messages) {
                    message -> MessageCard(message, chatUiState.username, imageFile)
            }
        }
    }
}

@Composable
fun Chat(navController: NavController, viewModel: ChatViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val chatUiState by viewModel.chatUiState.collectAsState()

    SensorAppTheme {
        Scaffold(
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        ) { padding ->
            Conversation(
                SampleData.conversationSample,
                modifier = Modifier.padding(padding),
                navController = navController,
                chatUiState = chatUiState
            )
        }
    }
}