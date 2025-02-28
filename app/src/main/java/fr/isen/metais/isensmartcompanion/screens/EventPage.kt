package fr.isen.metais.isensmartcompanion.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import fr.isen.metais.isensmartcompanion.R
import kotlinx.coroutines.launch
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
    val image: Int = R.drawable.placeholder // Champ pour l'image, par défaut placeholder
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


@Serializable
data class JsonEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String
)

@Composable
fun EventsScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.getEvents()
                if (response.isSuccessful) {
                    val jsonEvents = response.body() ?: emptyList()
                    val eventList = jsonEvents.map { Event.fromJsonEvent(it) }
                    Log.d("Events", "Événements récupérés : $eventList")
                    events = eventList
                } else {
                    Log.e("Events", "Erreur HTTP : ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Events", "Exception : ${e.message}")
                e.printStackTrace()
            }
        }
    }

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
                painter = painterResource(id = event.image), // Utilise le champ image
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