package fr.isen.metais.isensmartcompanion.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.metais.isensmartcompanion.R
import fr.isen.metais.isensmartcompanion.tools.GeminiViewModel

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val viewModel: GeminiViewModel = viewModel()
    var textState by remember { mutableStateOf("") }
    val responseText by viewModel.responseText.collectAsState()
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()

    // Ajouter la réponse de l'IA quand elle arrive
    LaunchedEffect(responseText) {
        if (responseText.isNotEmpty()) {
            chatMessages.add(ChatMessage(responseText, isFromUser = false))
            viewModel.clearResponse()
        }
    }

    // Scroll vers le bas quand chatMessages change
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.pink_background)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.isen),
            contentDescription = "ISEN Logo",
            modifier = Modifier.size(150.dp)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { message ->
                ChatMessageItem(message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(colorResource(id = R.color.text_area_color), shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Pour espacer les boutons
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorResource(id = R.color.arrow_circle_color), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        chatMessages.clear() // Réinitialise la conversation
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.reset),
                        contentDescription = "Reset",
                        tint = Color.White
                    )
                }
            }

            TextField(
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("   Entrez votre message") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorResource(id = R.color.arrow_circle_color), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (textState.isNotBlank()) {
                            chatMessages.add(ChatMessage(textState, isFromUser = true))
                            viewModel.sendMessage(textState)
                            textState = ""
                        } else {
                            Toast.makeText(context, "Entrez un message", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_forward),
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) Color(0xFFE91E63) else Color.White
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isFromUser) Color.White else Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}