package fr.isen.metais.isensmartcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import fr.isen.metais.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import fr.isen.metais.isensmartcompanion.screens.NavigationGraph
import fr.isen.metais.isensmartcompanion.screens.BottomNavigationBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ISENSmartCompanionTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
    }
}

@Composable
fun MainScreen(navController: androidx.navigation.NavHostController) {
    androidx.compose.material3.Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
            NavigationGraph(navController)
        }
    }
}
