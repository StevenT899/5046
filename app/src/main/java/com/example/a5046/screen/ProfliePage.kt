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
import androidx.compose.ui.graphics.SolidColor
import com.example.a5046.viewmodel.PlantViewModel

data class WeekFrequency(val week: String, val water: Int, val fertilize: Int)

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
                }
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
    if (data.size < 2) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    data.isEmpty() -> "No data"
                    else -> "Only ${data[0].week} data"
                },
                fontSize = 14.sp,
                color = Color(0xFF9EA0A5)
            )
        }
        return
    }
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
        val groupCount = data.size.coerceAtLeast(1)
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
private fun FrequencyCard(plantVM: PlantViewModel = viewModel()) {
    val statsFromVm by plantVM.frequencyByWeek.collectAsState(initial = emptyList())
    val statsForChart = statsFromVm.map {
        WeekFrequency(week = it.label, water = it.waterCount, fertilize = it.fertilizeCount)
    }
//    val stats = listOf(
//        WeekFrequency("Week 1", water = 2, fertilize = 1),
//        WeekFrequency("Week 2", water = 3, fertilize = 2),
//        WeekFrequency("Week 3", water = 4, fertilize = 3),
//        WeekFrequency("Week 4", water = 2, fertilize = 1),


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
            if (statsForChart.isEmpty()) {
                Text(
                    text = "No data",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF9EA0A5),
                    fontSize = 16.sp
                )
            } else {
                GroupedBarChart(
                    data = statsForChart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun ViewsByPlantsCard(
    viewModel: PlantViewModel = viewModel()
) {
    // 从 ViewModel 获取动态的 plantCounts 数据
    val counts by viewModel.plantCounts.collectAsState()
    val total = counts.values.sum().toFloat().coerceAtLeast(1f)

    // 设置植物类型的显示顺序
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

    // 计算饼图每个扇区的角度和颜色
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
//                Image(
//                    painter = painterResource(id = R.drawable.pie_chart),
//                    contentDescription = "Pie Chart",
//                    modifier = Modifier
//                        .size(120.dp),
//                    contentScale = ContentScale.Fit
//                )
                PieChart(
                    data = pieData,
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 展示植物类型的数量
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
