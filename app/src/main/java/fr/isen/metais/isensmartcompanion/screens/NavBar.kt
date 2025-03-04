package fr.isen.metais.isensmartcompanion.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.navigation.navArgument
import fr.isen.metais.isensmartcompanion.R

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen() }
        composable(
            "home/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getInt("conversationId") ?: 1
            HomeScreen(conversationId)
        }
        composable("events") { EventsScreen(navController) }
        composable("history") { HistoryScreen(navController) }
        composable(
            "detailHistory/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getInt("conversationId") ?: 1
            DetailHistoryScreen(navController, conversationId)
        }
        composable(
            route = "eventDetail/{eventJson}",
            arguments = listOf(navArgument("eventJson") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val eventJson = backStackEntry.arguments?.getString("eventJson")?.let { java.net.URLDecoder.decode(it, "UTF-8") }
            val event = eventJson?.let { kotlinx.serialization.json.Json.decodeFromString<Event>(it) }
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
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/{") ?: navBackStackEntry?.destination?.route

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