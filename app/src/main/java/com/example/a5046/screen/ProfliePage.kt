package com.example.a5046.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
data class WeekFrequency(val week: String, val water: Int, val fertilize: Int)

@Composable
fun ProfileCard(
    authVM: AuthViewModel,
    onLogout: () -> Unit,
    profileVM: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by profileVM.profileState.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "User Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3A915D)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (state) {
                is ProfileState.Loading -> Text("Loading...")
                is ProfileState.Error -> Text("Error: ${(state as ProfileState.Error).message}")
                is ProfileState.Success -> {
                    val profile = (state as ProfileState.Success).profile
                    Text("Name: ${profile.name}")
                    Text("Phone: ${profile.phone}")
                    Text("Age: ${profile.age}")
                    Text("Gender: ${profile.gender}")
                    Text("Level: ${profile.level}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    authVM.signOut(context)
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}



@Composable
fun PieChart(
    data: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val total = data.sumOf { it.first.toDouble() }.toFloat().coerceAtLeast(1f)
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


@Composable
fun GroupedBarChart(
    data: List<WeekFrequency>,
    modifier: Modifier = Modifier,
    waterColor: Color = Color(0xFF3A915D),
    fertilizeColor: Color = Color(0xFF8CE6A1),
    gridColor: Color = Color(0xFFBDBDBD),
    axisColor: Color = Color(0xFF4C4C4C),
) {
    //calculate dp→px
    val density = LocalDensity.current
    val paddingPx       = with(density) { 16.dp.toPx() }
    val bottomLabelPx   = with(density) { 20.dp.toPx() }   // x text position
    val textOffsetPx    = with(density) { 12.dp.toPx() }   // y text position
    val textPaint = android.graphics.Paint().apply {
        textAlign = android.graphics.Paint.Align.CENTER
        textSize   = 28f
        color      = android.graphics.Color.DKGRAY
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val left   = paddingPx
        val right  = w - paddingPx
        val top    = paddingPx
        val bottom = h - paddingPx - bottomLabelPx

        val maxVal    = data.maxOf { maxOf(it.water, it.fertilize) }.coerceAtLeast(1)
        val lines     = 4
        val stepValue = maxVal / lines.toFloat()


        for (i in 0..lines) {
            val y = top + (bottom - top) * (1f - i / lines.toFloat())
            drawLine(
                color     = gridColor,
                start     = Offset(left, y),
                end       = Offset(right, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect  = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.0f", stepValue * i),
                left - textOffsetPx,
                y + textOffsetPx / 2,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.RIGHT
                    textSize   = 28f
                    color      = android.graphics.Color.DKGRAY
                }
            )
        }

        drawLine(
            color       = axisColor,
            start       = Offset(left, bottom),
            end         = Offset(right, bottom),
            strokeWidth = 2.dp.toPx()
        )

        val groupCount = data.size
        val groupWidth = (right - left) / groupCount
        // single post width
        val barWidth   = groupWidth * 0.3f
        // Corner radius
        val radius     = 4.dp.toPx()

        data.forEachIndexed { idx, item ->
            val centerX = left + groupWidth * idx + groupWidth / 2
            val barGap = 6.dp.toPx()
            val waterLeft = centerX - barGap/2
            val fertLeft  = centerX + barGap/2

            // water bar
            val waterH = (item.water / maxVal.toFloat()) * (bottom - top)
            drawRoundRect(
                color        = waterColor,
                topLeft      = Offset(waterLeft - barWidth, bottom - waterH),
                size         = Size(barWidth, waterH),
                cornerRadius = CornerRadius(radius, radius)
            )

            // fertilizer bar
            val fertH = (item.fertilize / maxVal.toFloat()) * (bottom - top)
            drawRoundRect(
                color        = fertilizeColor,
                topLeft      = Offset(fertLeft, bottom - fertH),
                size         = Size(barWidth, fertH),
                cornerRadius = CornerRadius(radius, radius)
            )

            drawContext.canvas.nativeCanvas.drawText(
                item.week,
                centerX,
                bottom + bottomLabelPx * 0.9f,
                textPaint
            )
        }
    }
}



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

            FrequencyCard()

            Spacer(modifier = Modifier.height(30.dp))

            ViewsByPlantsCard()
        }
    }
}


@Composable
private fun FrequencyCard() {
    val stats = listOf(
        WeekFrequency("Week 1", water = 2, fertilize = 1),
        WeekFrequency("Week 2", water = 3, fertilize = 2),
        WeekFrequency("Week 3", water = 4, fertilize = 3),
        WeekFrequency("Week 4", water = 2, fertilize = 1),
    )

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


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Color(0xFF2C2C2C)
                )

                Spacer(modifier = Modifier.weight(1f))

                Column {
                    LegendItem(R.drawable.bar_water, "Water")
                    Spacer(modifier = Modifier.height(4.dp))
                    LegendItem(R.drawable.bar_fertilize, "Fertilize")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

//            Image(
//                painter = painterResource(id = R.drawable.bar_chart),
//                contentDescription = "Bar Chart",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .heightIn(min = 200.dp),
//                contentScale = ContentScale.Fit
//            )
            GroupedBarChart(
                data = stats,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun ViewsByPlantsCard() {
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
//                Image(
//                    painter = painterResource(id = R.drawable.pie_chart),
//                    contentDescription = "Pie Chart",
//                    modifier = Modifier
//                        .size(120.dp),
//                    contentScale = ContentScale.Fit
//                )
                PieChart(
                    data = listOf(
                        39f to Color(0xFF006A43),
                        28f to Color(0xFF00A86B),
                        23f to Color(0xFF4EDEA9),
                        5f  to Color(0xFFAEF7DC)
                    ),
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    DataRow(R.drawable.pie_1, "Okra", "39.11%")
                    DataRow(R.drawable.pie_2, "Red Spider Lily", "28.02%")
                    DataRow(R.drawable.pie_3, "Sacred Lotus", "23.13%")
                    DataRow(R.drawable.pie_4, "Hippeastrum", "5.03%")
                }
            }
        }
    }
}

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

@Composable
private fun LegendItem(iconId: Int, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = label,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            ),
            color = Color(0xFF4C4C4C)
        )
    }
}
