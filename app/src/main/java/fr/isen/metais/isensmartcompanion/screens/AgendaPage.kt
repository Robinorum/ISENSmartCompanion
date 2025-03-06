package fr.isen.metais.isensmartcompanion.screens

import android.content.Context
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
import fr.isen.metais.isensmartcompanion.events.EventViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.util.Locale

@Composable
fun AgendaScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("fr"))
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("fr"))

    val eventViewModel: EventViewModel = viewModel()
    val allEvents by eventViewModel.events.collectAsState()


    val likedEvents = remember(allEvents) {
        allEvents.filter { event ->
            sharedPreferences.getBoolean("notify_event_${event.id}", false)
        }
    }


    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().apply { set(2024, 0, 1) }) }


    val daysInMonth = selectedMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = selectedMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Dimanche, 6 = Samedi
    val monthEvents = likedEvents.filter { event ->
        val eventDate = try {
            dateFormat.parse(event.date)
        } catch (e: Exception) {
            null
        }
        eventDate?.let {
            it.month == selectedMonth.get(Calendar.MONTH) && it.year + 1900 == selectedMonth.get(Calendar.YEAR)
        } ?: false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // navigation des mois
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    selectedMonth = Calendar.getInstance().apply {
                        time = selectedMonth.time
                        add(Calendar.MONTH, -1)
                        if (get(Calendar.YEAR) < 2024) set(2024, 0, 1) // Limite à janvier 2024 (aucun evenement avant)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Mois précédent",
                    tint = Color.Gray
                )
            }

            Text(
                text = monthFormat.format(selectedMonth.time),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = {
                    selectedMonth = Calendar.getInstance().apply {
                        time = selectedMonth.time
                        add(Calendar.MONTH, 1)
                        if (get(Calendar.YEAR) > 2024) set(2024, 11, 1) // Limite à décembre 2024 (aucun evenement après)
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward),
                    contentDescription = "Mois suivant",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //affichage jours de la semaine en haut de la page
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam").forEach { day ->
                Text(
                    text = day,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }

        //grille de jours
        Column {
            val totalCells = daysInMonth + firstDayOfMonth
            val rows = (totalCells + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayIndex = row * 7 + col - firstDayOfMonth + 1
                        val isValidDay = dayIndex in 1..daysInMonth
                        val dayDate = if (isValidDay) {
                            Calendar.getInstance().apply {
                                time = selectedMonth.time
                                set(Calendar.DAY_OF_MONTH, dayIndex)
                            }.time
                        } else null

                        val hasEvent = dayDate?.let { date ->
                            likedEvents.any { event ->
                                val eventDate = try {
                                    dateFormat.parse(event.date)?.time
                                } catch (e: Exception) {
                                    null
                                }
                                eventDate?.let { (it / (1000 * 60 * 60 * 24))+1 == date.time / (1000 * 60 * 60 * 24) } ?: false
                            }
                        } ?: false

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .background(
                                    if (hasEvent) Color(0xFF4CAF50) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValidDay) {
                                Text(
                                    text = dayIndex.toString(),
                                    fontSize = 16.sp,
                                    color = if (hasEvent) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        if (monthEvents.isEmpty()) {
            Text(
                text = "Aucun événement liké ce mois-ci",
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(monthEvents) { event ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val eventJson = Json.encodeToString(event)
                                navController.navigate("eventDetail/${URLEncoder.encode(eventJson, "UTF-8")}")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "📅 ${event.date}",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "📍 ${event.location}",
                                    fontSize = 16.sp,
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