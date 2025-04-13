package com.example.a5046

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen() }
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

        // Reminder Section
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Daily Reminder", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Water your Snake Plant today.")
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        ) {}
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weather Section
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FB))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Weather", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Temperature", "Humanity", "Wind Speed").forEach {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            ) {}
                            Text(text = it, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recommendation
        Text(
            text = "Recommendation",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .border(1.dp, Color.LightGray)
                ) {}
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Heading", fontWeight = FontWeight.Bold)
                    Text("Content")
                }
            }
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





