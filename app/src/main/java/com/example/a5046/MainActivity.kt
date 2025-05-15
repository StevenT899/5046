package com.example.a5046

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.a5046.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _5046Theme {
                BottomNavigationBar()
            }
        }
    }
}
@Composable
fun BottomNavigationBar() {
    val navRoutes = listOf(
        NavRoute("home", R.drawable.homeicon, "Home"),
        NavRoute("plant", R.drawable.myplanticon, "My Plant"),
        NavRoute("form", R.drawable.formicon, "Form"),
        NavRoute("report", R.drawable.reporticon, "Report"),
    )

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigation(
                modifier = Modifier.padding(bottom = 0.dp),
                backgroundColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navRoutes.forEach { navRoute ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = navRoute.iconResId),
                                contentDescription = navRoute.label,
                                tint = if (currentDestination?.route == navRoute.route)
                                    Color(0xFF3A915D)
                                else
                                    Color.Gray
                            )
                        },
                        selected = currentDestination?.route == navRoute.route,
                        onClick = {
                            navController.navigate(navRoute.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "report",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen() }
            composable("plant") { MyPlant() }
            composable("form") { FormScreen() }
            composable("report") { ReportScreen() }
            composable("login") { LoginScreen() }
            composable("register") { RegisterScreen() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavPreview() {
    _5046Theme {
        BottomNavigationBar()
    }
}


data class NavRoute(val route: String, val iconResId: Int, val label: String)






