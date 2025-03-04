package fr.isen.metais.isensmartcompanion.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fr.isen.metais.isensmartcompanion.R
import fr.isen.metais.isensmartcompanion.data.ChatMessage
import fr.isen.metais.isensmartcompanion.gemini.GeminiViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetailHistoryScreen(navController: NavController, conversationId: Int) {
    val context = LocalContext.current
    val viewModel: GeminiViewModel = viewModel { GeminiViewModel(context) }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getChatMessages(conversationId).collectLatest { messages ->
            Log.d("DetailHistoryScreen", "Messages pour ConvID $conversationId : $messages")
            chatMessages.clear()
            chatMessages.addAll(messages)
        }
    }

    val messagePairs by remember(chatMessages) {
        derivedStateOf {
            val pairs = mutableListOf<Pair<ChatMessage, ChatMessage?>>()
            var i = 0
            while (i < chatMessages.size) {
                val question = chatMessages[i]
                val answer = if (i + 1 < chatMessages.size && !chatMessages[i + 1].isFromUser) chatMessages[i + 1] else null
                pairs.add(Pair(question, answer))
                i += if (answer != null) 2 else 1
            }
            Log.d("DetailHistoryScreen", "Couples pour ConvID $conversationId : $pairs")
            pairs
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "Retour",
                        tint = Color.Gray
                    )
                }

                Text(
                    text = "Conversation $conversationId",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Red, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bin),
                        contentDescription = "Supprimer la conversation",
                        tint = Color.White
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messagePairs) { pair ->
                    MessagePairItem(pair, dateFormat) {
                        viewModel.deleteMessagePair(pair.first.id, pair.second?.id)
                        chatMessages.remove(pair.first)
                        pair.second?.let { chatMessages.remove(it) }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.resumeConversation(conversationId)
                    navController.navigate("home/$conversationId")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Reprendre la conversation", fontSize = 14.sp)
            }
        }


        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmer la suppression") },
                text = { Text("Êtes-vous sûr de vouloir supprimer l'intégralité de la conversation $conversationId ?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteConversation(conversationId)
                            navController.popBackStack() // Retour à la liste après suppression
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Oui", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Non")
                    }
                }
            )
        }
    }
}

@Composable
fun MessagePairItem(
    pair: Pair<ChatMessage, ChatMessage?>,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = pair.first.text,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }

        pair.second?.let { answer ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = answer.text,
                    color = Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateFormat.format(Date(pair.first.timestamp)),
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bin),
                    contentDescription = "Supprimer",
                    tint = Color.Gray
                )
            }
        }
    }
}