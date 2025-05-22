package com.example.a5046.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a5046.R
import com.example.a5046.viewmodel.AuthViewModel
import com.example.a5046.viewmodel.ProfileState
import com.example.a5046.viewmodel.ProfileViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import com.example.a5046.viewmodel.PlantViewModel

//Show user progress and level bar
@Composable
fun ProgressBar(
    currentProgress: Float,// user's current progress value
    maxProgress: Float,// maximum progress value (600f currently)
    modifier: Modifier = Modifier,
    rightPadding: Dp = 16.dp
) {
    val progressPercentage = (currentProgress / maxProgress).coerceIn(0f, 1f)// limit between 0 and 1

    Column(modifier = modifier.fillMaxWidth()) {
        //Define dynamic level title
        val tier = when {
            currentProgress < 200f -> "Bronze Seeker"
            currentProgress < 400f -> "Silver Vanguard"
            else                      -> "Golden Aegis"
        }

        //Define dynamic level icon
        val tierIcon = when {
            currentProgress < 200f -> R.drawable.bronze_seeker
            currentProgress < 400f -> R.drawable.silver_vanguard
            else -> R.drawable.golden_aegis
        }

        //Show level title and icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
        ) {
            Text(
                text = "Your Growth Level:  ",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Image(
                painter = painterResource(id = tierIcon),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )

            Text(
                text = "$tier",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    currentProgress < 200f -> Color(0xFFEB710D) // Bronze color
                    currentProgress < 400f -> Color(0xFFA3C3CA) // Silver color
                    else -> Color(0xFFF8B917) // Gold color
                }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        //Draw the progress bar with 3 segments
        BoxWithConstraints(                     // Use BoxWithConstraints to get the total width of the progress bar
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = rightPadding)
                .height(20.dp)
        ) {
            val totalWidth = maxWidth
            val third = totalWidth / 3f

            // background bar
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFECECEC), RoundedCornerShape(10.dp))
            )

            // percentage
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progressPercentage)
                    .background(Color(0xFF00A86B), RoundedCornerShape(10.dp))
            )

            // divider lines
            Box(
                modifier = Modifier
                    .offset(x = third)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.Gray)
            )

            Box(
                modifier = Modifier
                    .offset(x = third * 2f)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.Gray)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        //Progress number labels
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("200", "400", "600").forEach { label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }
}

//Profile information card (name, level, plant count, logout)
@Composable
fun ProfileCard(
    authVM: AuthViewModel,
    onLogout: () -> Unit,
    profileVM: ProfileViewModel = viewModel(),
    plantVM: PlantViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by profileVM.profileState.collectAsState()
    val counts      by plantVM.plantCounts.collectAsState(initial = emptyMap())
    val totalPlants = counts.values.sum()
    val currentProgress by profileVM.currentProgress.collectAsState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (state) {
                is ProfileState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color(0xFF3A915D)
                    )
                }

                is ProfileState.Error -> {
                    Text(
                        text = "Error: ${(state as ProfileState.Error).message}",
                        fontSize = 16.sp,
                        color = Color.Red
                    )
                }

                is ProfileState.Success -> {
                    val profile = (state as ProfileState.Success).profile

                    // Name Row + Sign Out Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.username),
                            contentDescription = "Username icon",
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Name: ${profile.name}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        //Sign out button
                        OutlinedButton(
                            onClick = {
                                authVM.signOut(context)
                                onLogout()
                            },
                            modifier = Modifier
                                .height(30.dp)
                                .width(110.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF3B30)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(Color(0xFFFF3B30))
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Sign out",
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Total Plants Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.totleplan),
                            contentDescription = "Total plants icon",
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Total Plants: $totalPlants",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Level Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.userlevel),
                            contentDescription = "User level icon",
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "My Level: ${profile.level}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    //show progress bar
                    ProgressBar(currentProgress = currentProgress, maxProgress = 600f)
                }
            }
        }
    }
}

//Draw a pie chart for different plant types
@Composable
fun PieChart(
    data: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val total = data.sumOf { it.first.toDouble() }.toFloat().coerceAtLeast(1f)// Ensure total is at least 1 to avoid division by zero
        var startAngle = -90f
        data.forEach { (value, color) ->
            val sweep = value / total * 360f
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }
    }
}

//Main Profile page layout
@Composable
fun ProfileScreen(authVM: AuthViewModel, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF1F7F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileCard(authVM = authVM, onLogout = onLogout)

            Spacer(modifier = Modifier.height(30.dp))

            ViewsByPlantsCard()
        }
    }
}

//Plant view statistics section with pie chart and labels
@Composable
private fun ViewsByPlantsCard(
    viewModel: PlantViewModel = viewModel()
) {
    val counts by viewModel.plantCounts.collectAsState()
    val total = counts.values.sum().toFloat().coerceAtLeast(1f)

    val order = listOf("Flower", "Vegetable", "Fruit", "Herb")
    val colorMap = mapOf(
        "Flower"    to Color(0xFF006A43),
        "Vegetable" to Color(0xFF00A86B),
        "Fruit"     to Color(0xFF4EDEA9),
        "Herb"      to Color(0xFFAEF7DC)
    )
    val iconMap = mapOf(
        "Flower"    to R.drawable.pie_1,
        "Vegetable" to R.drawable.pie_2,
        "Fruit"     to R.drawable.pie_3,
        "Herb"      to R.drawable.pie_4
    )

    val pieData = order.map { type ->
        val cnt = counts[type] ?: 0
        val sweep = cnt / total * 360f
        sweep to (colorMap[type] ?: Color.Gray)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                ),
                color = Color(0xFF9EA0A5)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Views by plants",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = Color(0xFF2C2C2C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Divider(color = Color(0xFFE2E2E2), thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                PieChart(
                    data = pieData,
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))


                Column(modifier = Modifier.fillMaxWidth()) {
                    order.forEach { type ->
                        val cnt = counts[type] ?: 0
                        DataRow(
                            iconId = iconMap[type] ?: R.drawable.pie_1,
                            label = type,
                            value = cnt.toString()
                        )
                    }
                }
            }
        }
    }
}

//Row for each plant type and its count
@Composable
private fun DataRow(iconId: Int, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = label,
            modifier = Modifier.size(12.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF4C4C4C),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            color = Color(0xFF4C4C4C),
            textAlign = TextAlign.Start,
            modifier = Modifier.width(60.dp)
        )
    }
}