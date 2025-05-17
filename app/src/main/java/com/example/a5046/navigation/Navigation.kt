package com.example.a5046.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.a5046.R
import com.example.a5046.screen.*
import com.example.a5046.viewmodel.AuthViewModel

data class NavRoute(val route: String, val iconResId: Int, val label: String)

@Composable
fun MainNavigation() {
    val authVM: AuthViewModel = viewModel()
    val currentUser by authVM.currentUserState.collectAsState()
    val loggedIn = currentUser != null
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            if (loggedIn) {
                BottomNavigation(
                    modifier = Modifier.padding(bottom = 0.dp),
                    backgroundColor = Color.White
                ) {
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val current = backStackEntry?.destination?.route
                    val navRoutes = listOf(
                        NavRoute("home", R.drawable.homeicon, "Home"),
                        NavRoute("plant", R.drawable.myplanticon, "My Plant"),
                        NavRoute("form", R.drawable.formicon, "Form"),
                        NavRoute("profile", R.drawable.profile,"Profile"),
                    )
                    navRoutes.forEach { item ->
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    painter = painterResource(item.iconResId),
                                    contentDescription = item.label,
                                    tint = if (current == item.route) Color(0xFF3A915D) else Color.Gray
                                )
                            },
                            selected = current == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id)
                                    { inclusive = false }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (loggedIn) "home" else "login",
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(
                    authVM = authVM,
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onSignUpClick = {
                        navController.navigate("register")
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    authVM = authVM,
                    onRegisterSuccess = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onSignInClick = {
                        navController.navigate("login")
                    }
                )
            }


            composable("home")   { HomeScreen() }
            composable("plant")  { MyPlant() }
            composable("form")   { FormScreen() }
            composable("profile") {
                ProfileScreen(
                    authVM = authVM,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }


        }
    }
}