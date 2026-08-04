package de.tankzeit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import de.tankzeit.app.ui.navigation.TankzeitNavHost
import de.tankzeit.app.ui.theme.TankzeitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TankzeitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TankzeitNavHost()
                }
            }
        }
    }
}
