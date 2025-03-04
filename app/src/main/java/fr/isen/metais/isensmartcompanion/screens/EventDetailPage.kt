package fr.isen.metais.isensmartcompanion.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavController
import fr.isen.metais.isensmartcompanion.R

@Composable
fun EventDetailPage(navController: NavController, event: Event) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE)
    val notificationKey = "notify_event_${event.id}"

    var isNotified by remember {
        mutableStateOf(sharedPreferences.getBoolean(notificationKey, false))
    }

    createNotificationChannel(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = event.image),
                contentDescription = event.title,
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = {
                    isNotified = !isNotified
                    with(sharedPreferences.edit()) {
                        putBoolean(notificationKey, isNotified)
                        apply()
                    }
                    if (isNotified) {
                        Log.d("EventDetailPage", "Notification planifiée pour ${event.title}")
                        Handler(Looper.getMainLooper()).postDelayed({
                            Log.d("EventDetailPage", "Tentative d'envoi de la notification pour ${event.title}")
                            sendNotification(context, event)
                        }, 10_000)
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.pin),
                    contentDescription = "Notifier cet événement",
                    tint = if (isNotified) Color.Red else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = event.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "📅 ${event.date}", fontSize = 16.sp, color = Color.Gray)
        Text(text = "📍 ${event.location}", fontSize = 16.sp, color = Color.Gray)
        Text(text = "🎭 ${event.category}", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = event.description, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Retour")
        }
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "event_notifications"
        val name = "Event Notifications"
        val descriptionText = "Notifications for pinned events"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d("EventDetailPage", "Canal de notification créé : $channelId")
    }
}

private fun sendNotification(context: Context, event: Event) {
    val channelId = "event_notifications"
    val notificationId = event.id.hashCode()
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.pin)
        .setContentTitle("Événement : ${event.title}")
        .setContentText("Rappel : ${event.date} à ${event.location}")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("EventDetailPage", "Permission manquante pour envoyer la notification")
            return
        }
        notify(notificationId, builder.build())
        Log.d("EventDetailPage", "Notification envoyée pour ${event.title}, ID : $notificationId")
    }
}