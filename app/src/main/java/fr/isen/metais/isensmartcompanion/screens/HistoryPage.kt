package fr.isen.metais.isensmartcompanion.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fr.isen.metais.isensmartcompanion.R
import fr.isen.metais.isensmartcompanion.data.ChatMessage
import fr.isen.metais.isensmartcompanion.tools.GeminiViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: GeminiViewModel = viewModel { GeminiViewModel(context) }
    val conversationIds = remember { mutableStateListOf<Int>() }
    var selectedConversationId by remember { mutableStateOf<Int?>(null) }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    LaunchedEffect(Unit) {
        viewModel.getAllConversationIds().collectLatest { ids ->
            Log.d("HistoryScreen", "IDs des conversations : $ids")
            conversationIds.clear()
            conversationIds.addAll(ids)
        }
    }

    LaunchedEffect(selectedConversationId) {
        selectedConversationId?.let { id ->
            viewModel.getChatMessages(id).collectLatest { messages ->
                Log.d("HistoryScreen", "Messages pour ConvID $id : $messages")
                chatMessages.clear()
                chatMessages.addAll(messages)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (conversationIds.isEmpty()) {
            Text(
                text = "Aucun historique",
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (selectedConversationId == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(conversationIds) { convId ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedConversationId = convId }
                    ) {
                        Text(
                            text = "Conversation $convId",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        } else {
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
                    Log.d("HistoryScreen", "Couples pour ConvID $selectedConversationId : $pairs")
                    pairs
                }
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Button(
                    onClick = { selectedConversationId = null },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Start)
                ) {
                    Text("Retour")
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            viewModel.resumeConversation(selectedConversationId!!)
                            navController.navigate("home/$selectedConversationId")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Reprendre la conversation", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.deleteConversation(selectedConversationId!!)
                            selectedConversationId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Supprimer tout", fontSize = 14.sp)
                    }
                }
            }
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
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFF5F5F5), shape = CircleShape)
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