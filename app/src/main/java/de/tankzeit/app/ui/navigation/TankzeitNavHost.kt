package de.tankzeit.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.tankzeit.app.ui.screens.preise.PreiseScreen
import de.tankzeit.app.ui.screens.prognose.PrognoseScreen
import de.tankzeit.app.ui.screens.settings.SettingsScreen

sealed class TankzeitDestination(val route: String, val label: String) {
    data object Preise : TankzeitDestination("preise", "Preise")
    data object Prognose : TankzeitDestination("prognose", "Prognose")
    data object Einstellungen : TankzeitDestination("einstellungen", "Einstellungen")
}

private val destinations = listOf(
    TankzeitDestination.Preise,
    TankzeitDestination.Prognose,
    TankzeitDestination.Einstellungen
)

@Composable
fun TankzeitNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                destinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(iconFor(destination), contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TankzeitDestination.Preise.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(TankzeitDestination.Preise.route) { PreiseScreen() }
            composable(TankzeitDestination.Prognose.route) { PrognoseScreen() }
            composable(TankzeitDestination.Einstellungen.route) { SettingsScreen() }
        }
    }
}

private fun iconFor(destination: TankzeitDestination) = when (destination) {
    TankzeitDestination.Preise -> Icons.Filled.LocalGasStation
    TankzeitDestination.Prognose -> Icons.Filled.TrendingUp
    TankzeitDestination.Einstellungen -> Icons.Filled.Settings
}
