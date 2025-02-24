package fr.isen.metais.isensmartcompanion.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.isen.metais.isensmartcompanion.R

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen() }
        composable("events") { EventsScreen() }
        composable("history") { HistoryScreen() }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.home), contentDescription = "Home") },
            label = { Text("Home") },
            selected = false,
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.events), contentDescription = "Events") },
            label = { Text("Events") },
            selected = false,
            onClick = { navController.navigate("events") }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.history), contentDescription = "History") },
            label = { Text("History") },
            selected = false,
            onClick = { navController.navigate("history") }
        )
    }
}
