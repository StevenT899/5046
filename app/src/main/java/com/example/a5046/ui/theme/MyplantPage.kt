package com.example.a5046.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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

@Composable
fun MyPlant() {
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
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                CustomPlantCard()
            }
        }
    }
}

@Composable
fun CustomPlantCard() {
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
            )
            {
                Icon(
                    painter = painterResource(id = R.drawable.ic_card_1),
                    contentDescription = "plant picture",
                    modifier = Modifier.size(160.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.fillMaxWidth(0.88f).padding(top = 8.dp)) {
                    Text(
                        text = "Peperomia Ginny",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tropical evergreen perennial",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.date),
                            contentDescription = "Date",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Feb/14/2025",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    //
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.fertilize),
                            contentDescription = "fertilize",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fertilize once a month",
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
                            text = "Water every 7-8 days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(6.dp)
                        .align(Alignment.Top)
                        .clickable {
                            //click function
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
