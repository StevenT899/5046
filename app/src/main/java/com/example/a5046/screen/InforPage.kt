package com.example.a5046.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun UserInfoForm(modifier: Modifier = Modifier) {
    var userName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var gardeningLevel by remember { mutableStateOf("") }

    val genderOptions = listOf("Male", "Female", "Prefer not to say")
    val gardeningLevels = listOf(
        "Gardening Beginner",
        "Gardening Novice",
        "Gardening Enthusiast",
        "Gardening Specialist",
        "Gardening Master"
    )

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
                text = "User Information",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(18.dp))

            FormLabel("User Name (*)")
            StyledTextField(userName) { userName = it }

            Spacer(modifier = Modifier.height(14.dp))

            FormLabel("Phone Number (*)")
            StyledTextField(phone) { phone = it }

            Spacer(modifier = Modifier.height(14.dp))

            FormLabel("Age (*)")
            StyledTextField(age) { age = it }

            Spacer(modifier = Modifier.height(14.dp))

            FormLabel("Gender (*)")
            DropdownMenuField(genderOptions, gender) { gender = it }

            Spacer(modifier = Modifier.height(14.dp))

            FormLabel("Gardening Experience Level (*)")
            DropdownMenuField(gardeningLevels, gardeningLevel) { gardeningLevel = it }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A915D))
            ) {
                Text("Submit", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun UserInfoFormPreview() {
    UserInfoForm()
}
