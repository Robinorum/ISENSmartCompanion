package fr.isen.metais.isensmartcompanion

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.Manifest
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import fr.isen.metais.isensmartcompanion.ui.theme.ISENSmartCompanionTheme
import fr.isen.metais.isensmartcompanion.screens.NavigationGraph
import fr.isen.metais.isensmartcompanion.screens.BottomNavigationBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Demande de permission POST_NOTIFICATIONS")
                requestPermissionLauncher.launch(permission)
            } else {
                Log.d("MainActivity", "Permission POST_NOTIFICATIONS déjà accordée")
            }
        }

        setContent {
            ISENSmartCompanionTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("MainActivity", "Permission POST_NOTIFICATIONS accordée")
            } else {
                Log.d("MainActivity", "Permission POST_NOTIFICATIONS refusée")
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
