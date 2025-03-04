package fr.isen.metais.isensmartcompanion.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fr.isen.metais.isensmartcompanion.gemini.GeminiViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: GeminiViewModel = viewModel { GeminiViewModel(context) }
    val conversationIds = remember { mutableStateListOf<Int>() }
    val lastMessageTimestamps = remember { mutableStateMapOf<Int, Long?>() }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    LaunchedEffect(Unit) {
        viewModel.getAllConversationIds().collectLatest { ids ->
            Log.d("HistoryScreen", "IDs des conversations : $ids")
            conversationIds.clear()
            conversationIds.addAll(ids)
            // Récupérer les derniers timestamps pour chaque conversation
            ids.forEach { id ->
                val lastTimestamp = viewModel.getLastMessageTimestamp(id)
                lastMessageTimestamps[id] = lastTimestamp
            }
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
            if (conversationIds.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center // Centrage dans un Box
                ) {
                    Text(
                        text = "Aucun historique",
                        fontSize = 24.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(conversationIds) { convId ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("detailHistory/$convId") }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Conversation $convId",
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Dernier message le ${
                                        lastMessageTimestamps[convId]?.let { dateFormat.format(Date(it)) }
                                            ?: "inconnu"
                                    }",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}