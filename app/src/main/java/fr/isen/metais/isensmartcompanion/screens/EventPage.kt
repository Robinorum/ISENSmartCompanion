package fr.isen.metais.isensmartcompanion.screens

import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import fr.isen.metais.isensmartcompanion.R
import fr.isen.metais.isensmartcompanion.events.EventViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String,
    val image: Int = R.drawable.placeholder
) {
    companion object {
        fun fromJsonEvent(jsonEvent: Event): Event {
            val imageRes = when (jsonEvent.category) {
                "BDE" -> R.drawable.bde
                "BDS" -> R.drawable.sportif
                "Technologique" -> R.drawable.puant
                else -> R.drawable.isen_logo
            }
            return Event(
                id = jsonEvent.id,
                title = jsonEvent.title,
                description = jsonEvent.description,
                date = jsonEvent.date,
                location = jsonEvent.location,
                category = jsonEvent.category,
                image = imageRes
            )
        }
    }
}

@Composable
fun EventsScreen(navController: NavController) {
    val eventViewModel: EventViewModel = viewModel()
    val events by eventViewModel.events.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (events.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(events) { event ->
                    EventItem(event, navController)
                }
            }
        }
    }
}

@Composable
fun EventItem(event: Event, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val eventJson = Uri.encode(Json.encodeToString(event))
                navController.navigate("eventDetail/$eventJson")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = event.image),
                contentDescription = event.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "📅 ${event.date}", fontSize = 12.sp, color = Color.DarkGray)
                Text(text = "📍 ${event.location}", fontSize = 12.sp, color = Color.DarkGray)
                Text(text = "🎭 ${event.category}", fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}