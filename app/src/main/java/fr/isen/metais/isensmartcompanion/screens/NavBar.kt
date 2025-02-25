package fr.isen.metais.isensmartcompanion.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import fr.isen.metais.isensmartcompanion.R
import kotlinx.serialization.json.Json

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen() }
        composable("events") { EventsScreen(navController) }
        composable("history") { HistoryScreen() }
        composable(
            route = "eventDetail/{eventJson}",
            arguments = listOf(navArgument("eventJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventJson = backStackEntry.arguments?.getString("eventJson")
            val event = eventJson?.let { Json.decodeFromString<Event>(it) }
            if (event != null) {
                EventDetailPage(navController, event)
            } else {
                Text("Erreur : Événement non trouvé")
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.home), contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.events), contentDescription = "Events") },
            label = { Text("Events") },
            selected = currentRoute == "events",
            onClick = { navController.navigate("events") }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.history), contentDescription = "History") },
            label = { Text("History") },
            selected = currentRoute == "history",
            onClick = { navController.navigate("history") }
        )
    }
}