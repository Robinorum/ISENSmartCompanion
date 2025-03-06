package fr.isen.metais.isensmartcompanion.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.isen.metais.isensmartcompanion.R
import fr.isen.metais.isensmartcompanion.data.ChatMessage
import fr.isen.metais.isensmartcompanion.gemini.GeminiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(conversationId: Int? = null) {
    val context = LocalContext.current
    val viewModel: GeminiViewModel = viewModel { GeminiViewModel(context) }
    var textState by remember { mutableStateOf("") }
    val responseText by viewModel.responseText.collectAsState()
    val currentConversationId by viewModel.currentConversationId.collectAsState()
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    var showConfetti by remember { mutableStateOf(false) }


    val leftParty = remember {
        Party(
            emitter = Emitter(duration = 1, TimeUnit.SECONDS).perSecond(100), // 100 confettis en 1s
            position = Position.Relative(0.0, 0.5), // Côté gauche, milieu vertical
            speed = 15f,
            maxSpeed = 30f,
            spread = 90,
            angle = 325,

        )
    }

    val rightParty = remember {
        Party(
            emitter = Emitter(duration = 1, TimeUnit.SECONDS).perSecond(100), // 100 confettis en 1s
            position = Position.Relative(1.0, 0.5), // Côté droit, milieu vertical
            speed = 15f,
            maxSpeed = 30f,
            spread = 90,
            angle = 225
        )
    }


    LaunchedEffect(showConfetti) {
        if (showConfetti) {
            delay(4000L)
            showConfetti = false

        }
    }

    LaunchedEffect(conversationId) {
        conversationId?.let {
            viewModel.resumeConversation(it)
        }
    }

    LaunchedEffect(currentConversationId) {
        viewModel.getChatMessages(currentConversationId).collectLatest { messages ->
            Log.d("HomeScreen", "Messages chargés pour ConvID $currentConversationId : $messages")
            chatMessages.clear()
            chatMessages.addAll(messages)
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LaunchedEffect(responseText) {
        if (responseText.isNotEmpty()) {
            viewModel.clearResponse()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                modifier = Modifier
                    .size(150.dp)
                    .clickable {
                        showConfetti = true
                        Log.d("HomeScreen", "Clic sur le logo ISEN, confettis déclenchés")
                    }
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(colorResource(id = R.color.arrow_circle_color), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            viewModel.startNewConversation()
                            chatMessages.clear()
                            Log.d("HomeScreen", "Après clic sur +, ConvID actuel : $currentConversationId")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "Nouvelle conversation",
                            tint = Color.White
                        )
                    }
                }

                TextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Entrez votre message") },
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

        // Afficher les confettis des deux côtés
        if (showConfetti) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(leftParty, rightParty) // Deux sources de confettis
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) Color(0xFFE91E63) else Color.White
            ),
            modifier = Modifier.widthIn(max = 300.dp)
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