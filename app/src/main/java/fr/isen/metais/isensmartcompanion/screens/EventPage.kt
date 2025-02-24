package fr.isen.metais.isensmartcompanion.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.metais.isensmartcompanion.R

@Composable
fun EventsScreen() {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Events Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = {
            },
            containerColor = colorResource(id = R.color.arrow_circle_color),
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(painterResource(id = R.drawable.add), contentDescription = "Add Event", tint = Color.White)
        }
    }
}

