package com.example.a5046.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import android.Manifest
import com.example.a5046.R
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a5046.viewmodel.HomeState
import com.example.a5046.viewmodel.HomeViewModel
import com.example.a5046.viewmodel.WeatherState
import com.example.a5046.viewmodel.WeatherViewModel
import com.example.a5046.viewmodel.RecommendationState
import com.example.a5046.viewmodel.RecommendationViewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button


@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    recommendationViewModel: RecommendationViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val homeState by homeViewModel.homeState.collectAsState()
    val address by homeViewModel.address.collectAsState()
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val recommendationState by recommendationViewModel.recommendationState.collectAsState()
    val reminders by homeViewModel.reminders.collectAsState()

    LaunchedEffect(homeState) {
        when (val state = homeState) {
            is HomeState.Success -> {
                recommendationViewModel.loadRecommendations(state.userData.level)
            }
            else -> {
                recommendationViewModel.loadRecommendations("Gardening Beginner")
            }
        }
    }

    var isFertilizeDone by remember { mutableStateOf(false) }
    var isWaterSnakeDone by remember { mutableStateOf(false) }
    var isWaterFlowerDone by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                    .setMaxUpdates(1)
                    .build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        val location = result.lastLocation
                        if (location != null) {
                            homeViewModel.updateAddress(context, location.latitude, location.longitude)
                            weatherViewModel.updateWeather(location.latitude, location.longitude)
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        homeViewModel.loadReminders()
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMaxUpdates(1)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    if (location != null) {
                        homeViewModel.updateAddress(context, location.latitude, location.longitude)
                        weatherViewModel.updateWeather(location.latitude, location.longitude)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F7F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            when (val state = homeState) {
                is HomeState.Loading -> {
                    Text(
                        text = "Loading...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A915D)
                    )
                }
                is HomeState.Success -> {
                    Text(
                        text = "Hi, ${state.userData.name}!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A915D)
                    )
                }
                is HomeState.Error -> {
                    Text(
                        text = "Hi, User!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A915D)
                    )
                }
            }
            
            Text(
                text = address,
                fontSize = 20.sp,
                color = Color(0xFF3E3E3E),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Daily Reminder", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    reminders.filter { !it.isDone }.forEach { reminder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val taskText = when {
                                reminder.needWater -> "${reminder.plantName} need watering"
                                reminder.needFertilize -> "${reminder.plantName} need fertilizing"
                                else -> null
                            }

                            taskText?.let {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val iconRes = if (reminder.needWater) R.drawable.watering else R.drawable.temperature
                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(it, fontSize = 18.sp)
                                }

                                IconButton(onClick = {
                                    homeViewModel.markReminderDone(reminder)
                                }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.undo),
                                        contentDescription = "Mark as done",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击图标标记为完成",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Button(
                        onClick = { homeViewModel.debugRunReminderCheck() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("🔍 Check button")
                    }

                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Weather",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (val state = weatherState) {
                            is WeatherState.Loading -> {
                                Text("Loading weather data...", fontSize = 18.sp)
                            }
                            is WeatherState.Success -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${state.weatherData.temperature.toInt()}°C",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9800),
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.temperature),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Temperature", fontSize = 16.sp, color = Color(0xFFFF9800))
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${state.weatherData.humidity}%",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3),
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.humanity),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Humidity", fontSize = 16.sp, color = Color(0xFF2196F3))
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = state.weatherData.uvIndex.toInt().toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF44336),
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painter = painterResource(id = R.drawable.uv),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "UV Level", fontSize = 16.sp, color = Color(0xFFF44336))
                                    }
                                }
                            }
                            is WeatherState.Error -> {
                                Text("Failed to load weather data", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recommendation",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when (val state = recommendationState) {
                is RecommendationState.Loading -> {
                    Text("Loading recommendations...", fontSize = 18.sp)
                }
                is RecommendationState.Success -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // First recommendation card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(400.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(id = R.drawable.recommendation),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        state.recommendations.firstOrNull()?.title ?: "Loading...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        state.recommendations.firstOrNull()?.content ?: "Loading content...",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Second recommendation card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(400.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(id = R.drawable.recommendation2),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        state.recommendations.getOrNull(1)?.title ?: "Loading...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        state.recommendations.getOrNull(1)?.content ?: "Loading content...",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
                is RecommendationState.Error -> {
                    Text(
                        "Failed to load recommendations: ${state.message}",
                        fontSize = 18.sp,
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
