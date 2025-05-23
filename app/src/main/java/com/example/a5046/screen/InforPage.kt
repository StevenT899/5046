package com.example.a5046.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a5046.viewmodel.SubmitState
import com.example.a5046.viewmodel.UserInfoViewModel

@Composable
fun UserInfoForm(
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit // callback when profile saved successfully
) {
    val viewModel: UserInfoViewModel = viewModel()
    val submitState by viewModel.submitState.collectAsState()
    var userName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var gardeningLevel by remember { mutableStateOf("") }
// Error strings
    var userNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var gardeningLevelError by remember { mutableStateOf<String?>(null) }

    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female", "Prefer not to say")
    val gardeningLevels = listOf("Gardening Beginner", "Gardening Novice", "Gardening Enthusiast")

    val validateUserName = {
        userNameError = when {
            userName.isBlank() -> "Username is required"
            userName.length > 36 -> "Username must be less than 36 characters"
            else -> null
        }
    }

    val validatePhone = {
        phoneError = when {
            phone.isBlank() -> "Phone number is required"
            !phone.all { it.isDigit() } -> "Phone number must contain only digits"
            phone.length !in 6..15 -> "Phone number must be between 6-15 digits"
            else -> null
        }
    }

    val validateAge = {
        ageError = when {
            age.isBlank() -> "Age is required"
            !age.all { it.isDigit() } -> "Age must be a number"
            age.toIntOrNull() ?: -1 < 0 -> "Age must be positive"
            age.toIntOrNull() ?: 121 > 120 -> "Age must be less than 120"
            else -> null
        }
    }

    val validateGender = {
        genderError = if (gender.isBlank()) "Please select your gender" else null
    }

    val validateGardeningLevel = {
        gardeningLevelError = if (gardeningLevel.isBlank()) "Please select your gardening experience level" else null
    }
    /** Validate all fields and return true only when everything is OK */

    val validateAllFields = {
        validateUserName()
        validatePhone()
        validateAge()
        validateGender()
        validateGardeningLevel()
        userNameError == null && phoneError == null && ageError == null && genderError == null && gardeningLevelError == null
    }

    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) {
            onSubmit()
            viewModel.resetState()
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color(0xFFF1F7F5)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text("Complete your info", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(18.dp))

            FormField("User Name (*)", userName, userNameError) {
                userName = it
                if (hasAttemptedSubmit) validateUserName()
            }

            FormField("Phone Number (*)", phone, phoneError) {
                phone = it
                if (hasAttemptedSubmit) validatePhone()
            }

            FormField("Age (*)", age, ageError) {
                age = it
                if (hasAttemptedSubmit) validateAge()
            }

            DropdownField("Gender (*)", gender, genderOptions, genderError) {
                gender = it
                if (hasAttemptedSubmit) validateGender()
            }

            DropdownField("Gardening Experience Level (*)", gardeningLevel, gardeningLevels, gardeningLevelError) {
                gardeningLevel = it
                if (hasAttemptedSubmit) validateGardeningLevel()
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (submitState is SubmitState.Error) {
                Text((submitState as SubmitState.Error).msg, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (validateAllFields()) {
                        viewModel.submitUserInfo(userName, phone, age, gender, gardeningLevel)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A915D))
            ) {
                Text("Submit", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}
/** Small label text shown above every field */
@Composable
fun InputLabel(label: String) {
    Text(
        text = label,
        fontSize = 14.sp,
        color = Color(0xFF4B5563),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp).fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}
/** Text field with optional error string */
@Composable
fun FormField(label: String, value: String, error: String?, onValueChange: (String) -> Unit) {
    InputLabel(label)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp),
        shape = RoundedCornerShape(12.dp),
        isError = error != null,
        supportingText = {
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    )
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
fun DropdownField(label: String, selected: String, options: List<String>, error: String?, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    InputLabel(label)
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            isError = error != null,
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            supportingText = {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp),
            shape = RoundedCornerShape(12.dp)
        )
        Box(
            modifier = Modifier.matchParentSize().clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
}