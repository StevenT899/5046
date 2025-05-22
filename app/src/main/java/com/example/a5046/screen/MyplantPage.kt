package com.example.a5046.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a5046.R
import com.example.a5046.ui.theme._5046Theme
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import com.example.a5046.data.Plant
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import com.example.a5046.viewmodel.PlantViewModel




@Composable
fun MyPlant(
    viewModel: PlantViewModel = viewModel(),
    homeViewModel: com.example.a5046.viewmodel.HomeViewModel = viewModel()
) {
    val plantList by viewModel.allPlants.collectAsState(initial = emptyList())
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F7F5)
    ) {



        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "My Plant",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plantList) { plant ->
                    CustomPlantCard(
                        plant = plant,
                        onDelete = { viewModel.deletePlant(it, homeViewModel) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomPlantCard(plant: Plant, onDelete: (Plant) -> Unit) {
    val bitmap = remember(plant.image) {
        plant.image?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
    Card(
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.background(MaterialTheme.colorScheme.surface))
        {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Plant photo",
                        modifier = Modifier.size(160.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_card_1),
                        contentDescription = "Placeholder",
                        modifier = Modifier.size(160.dp),
                        tint = Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.fillMaxWidth(0.88f).padding(top = 8.dp)) {
                    Text(
                        text = plant.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Type: ${plant.plantType}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.date),
                            contentDescription = "Date",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Planting data: ${plant.plantingDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    //fertilize
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.fertilize),
                            contentDescription = "fertilize",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fertilize every ${plant.fertilizingFrequency} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // watering
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_water),
                            contentDescription = "water icon",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Water every ${plant.wateringFrequency} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(6.dp)
                        .align(Alignment.Top)
                        .offset(x = (-8).dp, y = (6).dp)
                        .clickable {
                            onDelete(plant)//click function
                        },
                    tint = Color.Unspecified
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PlantScreenPreview() {
    _5046Theme {
        MyPlant()
    }
}