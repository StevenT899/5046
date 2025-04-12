package com.example.a5046

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a5046.ui.theme._5046Theme

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.a5046.ui.theme.Home


//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigation
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigationItem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info

import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import com.example.a5046.ui.theme.Formscreen
import com.example.a5046.ui.theme.MyPlant
import com.example.a5046.ui.theme.Reportscreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _5046Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
                BottomNavigationBar()
            }
        }
    }
}




@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Hi, Deshui!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3A915D)
        )
        Text(
            text = "Clayton, Melbourne",
            fontSize = 16.sp,
            color = Color(0xFF3E3E3E),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Reminder",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Daily Reminder", fontWeight = FontWeight.Bold)
                    Text(text = "Water your Snake Plant today.")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeatherStat("Temperature", "25℃", Color(0xFF2E7D32), Modifier.weight(1f))
            WeatherStat("Humidity", "60%", Color(0xFF388E3C), Modifier.weight(1f))
            WeatherStat("Wind Speed", "15 km/h", Color(0xFF43A047), Modifier.weight(1f))
        }
    }
}

@Composable
fun WeatherStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Text(text = value, color = color, fontSize = 16.sp)
    }
}


data class NavRoute(val route: String, val iconResId: Int, val label: String)

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
                backgroundColor = Color.White // 设置背景为白色
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
                        alwaysShowLabel = false // 不显示文字标签
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { Home() }
            composable("plant") { MyPlant() }
            composable("form") { Formscreen() }
            composable("report") { Reportscreen() }
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


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    _5046Theme {
//        Greeting("Android")
//    }
//}



//@Composable
//fun LoginScreen()  {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    Surface(
//        modifier = Modifier.fillMaxSize(), color = Color(0xFFF1F7F5)
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.plantimg),
//                contentDescription = "Logo",
//                modifier = Modifier.size(180.dp)
//            )
//            Text(
//                text = "Welcome to PlantEase",
//                fontSize = 24.sp,
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//            Text(
//                text = "Email",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.align(Alignment.Start)
//            )
//
//            OutlinedTextField(
//                value = email,
//                onValueChange = { email = it },
//                label = { Text("Enter your email") },
//                singleLine = true,
//                modifier = Modifier.align(Alignment.Start).fillMaxWidth()
//            )
//            Text(
//                text = "Password",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.align(Alignment.Start).padding(top = 10.dp)
//            )
//
//            OutlinedTextField(
//                value = password,
//                onValueChange = { email = it },
//                label = { Text("Enter your password") },
//                singleLine = true,
//                modifier = Modifier.align(Alignment.Start).fillMaxWidth()
//            )
//            Spacer(modifier = Modifier.height(24.dp))
//            Button(
//                onClick = {},
//                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(48.dp),
//                shape = RoundedCornerShape(12.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A915D))
//            ) {
//                Text("Sign in", color = Color.White,fontSize = 18.sp)
//            }
//            Row (modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center){
//                Text("Don't have an account? ")
//                Text(
//                    text = "SIGN UP",
//                    color = Color(0xFF3A915D),
//                    fontWeight = FontWeight.Bold,
//                )
//            }
//        }
//    }
//}
//@Preview(showBackground = true)
//@Composable
//fun LoginPreview() {
//    _5046Theme {
//        LoginScreen()
//    }
//}

//@Composable
//fun RegisterScreen()  {
//    var name by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    Surface(
//        modifier = Modifier.fillMaxSize(), color = Color(0xFFF1F7F5)
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = "Create your account",
//                fontSize = 24.sp,
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//            Text(
//                text = "Name",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.align(Alignment.Start)
//            )
//            OutlinedTextField(
//                value = name,
//                onValueChange = { name = it },
//                label = { Text("Enter your name") },
//                singleLine = true,
//                modifier = Modifier.align(Alignment.Start).fillMaxWidth()
//            )
//            Text(
//                text = "Email",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.align(Alignment.Start).padding(top = 10.dp)
//            )
//
//            OutlinedTextField(
//                value = email,
//                onValueChange = { email = it },
//                label = { Text("Enter your email") },
//                singleLine = true,
//                modifier = Modifier.align(Alignment.Start).fillMaxWidth()
//            )
//            Text(
//                text = "Password",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.align(Alignment.Start).padding(top = 10.dp)
//            )
//
//            OutlinedTextField(
//                value = password,
//                onValueChange = { password = it },
//                label = { Text("Enter your password") },
//                singleLine = true,
//                modifier = Modifier.align(Alignment.Start).fillMaxWidth()
//            )
//            Text(
//                text = "Confirm Password",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.align(Alignment.Start).padding(top = 10.dp)
//            )
//
//            OutlinedTextField(
//                value = confirmPassword,
//                onValueChange = { confirmPassword = it },
//                label = { Text("Re-enter your password") },
//                singleLine = true,
//                modifier = Modifier.align(Alignment.Start).fillMaxWidth()
//            )
//            Spacer(modifier = Modifier.height(24.dp))
//            Button(
//                onClick = {},
//                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(48.dp),
//                shape = RoundedCornerShape(12.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A915D))
//            ) {
//                Text("Sign up", color = Color.White,fontSize = 18.sp)
//            }
//            Row (modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center){
//                Text("Have an account ")
//                Text(
//                    text = "SIGN IN",
//                    color = Color(0xFF3A915D),
//                    fontWeight = FontWeight.Bold,
//                )
//            }
//        }
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun RegisterPreview() {
//    _5046Theme {
//        RegisterScreen()
//    }
//}





