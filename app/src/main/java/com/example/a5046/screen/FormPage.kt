package com.example.a5046.screen

// Import necessary Android and Compose libraries
import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a5046.data.Plant
import com.example.a5046.ui.theme._5046Theme
import com.example.a5046.viewmodel.PlantViewModel
import com.example.a5046.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/** Convert Bitmap image to ByteArray for storage */
fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}


/** Convert URI to Bitmap (supporting different Android versions) */
fun uriToBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/** Main form screen UI */
@Composable
fun FormScreen(modifier: Modifier = Modifier) {
    val viewModel: PlantViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()

    // State variables for form fields
    var plantName by remember { mutableStateOf("") }
    var plantingDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"))) }
    var plantType by remember { mutableStateOf("") }
    var wateringFrequency by remember { mutableStateOf("") }
    var fertilizingFrequency by remember { mutableStateOf("") }
    var lastWateredDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"))) }
    var lastFertilizedDate by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"))) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var plantNameError by remember { mutableStateOf<String?>(null) }
    var plantTypeError by remember { mutableStateOf<String?>(null) }
    var wateringFrequencyError by remember { mutableStateOf<String?>(null) }
    var fertilizingFrequencyError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val plantTypes = listOf("Flower", "Vegetable", "Fruit", "Herb", "Other")
    val frequencyOptions = listOf("1", "2", "3", "5", "7", "10", "14")

    val context = LocalContext.current

    val validatePlantName = {
        plantNameError = when {
            plantName.isBlank() -> "Plant name is required"
            plantName.length > 36 -> "Plant name must be less than 36 characters"
            else -> null
        }
    }

    val validatePlantType = {
        plantTypeError = if (plantType.isBlank()) "Plant type is required" else null
    }

    val validateWateringFrequency = {
        wateringFrequencyError = if (wateringFrequency.isBlank()) "Watering frequency is required" else null
    }

    val validateFertilizingFrequency = {
        fertilizingFrequencyError = if (fertilizingFrequency.isBlank()) "Fertilizing frequency is required" else null
    }

    val validateAllFields = {
        validatePlantName()
        validatePlantType()
        validateWateringFrequency()
        validateFertilizingFrequency()
        plantNameError == null && plantTypeError == null &&
                wateringFrequencyError == null && fertilizingFrequencyError == null
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF1F7F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text("Add Plant", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(18.dp))

            // Image card for plant photo upload
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 14.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1EFE7))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val bitmap = imageUri?.let { uriToBitmap(context, it) }
                    if (bitmap != null) {
                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null)
                    } else {
                        Text("+ Add Plant Photo", color = Color(0xFF3A915D), fontSize = 16.sp)
                    }
                }
            }

            PlantFormLabel("Plant Name(*)")
            StyledTextField(
                value = plantName,
                onValueChange = {
                    plantName = it
                    if (hasAttemptedSubmit) validatePlantName()
                },
                error = plantNameError
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    PlantFormLabel("Planting Date(*)")
                    DatePickerField(plantingDate) { plantingDate = it }
                }
                Column(modifier = Modifier.weight(1f)) {
                    PlantFormLabel("Plant Type(*)")
                    DropdownMenuField(
                        options = plantTypes,
                        selectedOption = plantType,
                        onOptionSelected = {
                            plantType = it
                            if (hasAttemptedSubmit) validatePlantType()
                        },
                        error = plantTypeError
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            PlantFormLabel("Watering Frequency(*)")
            DropdownMenuField(
                options = frequencyOptions,
                selectedOption = wateringFrequency,
                onOptionSelected = {
                    wateringFrequency = it
                    if (hasAttemptedSubmit) validateWateringFrequency()
                },
                error = wateringFrequencyError
            )
            Spacer(modifier = Modifier.height(14.dp))
            PlantFormLabel("Fertilizing Frequency(*)")
            DropdownMenuField(
                options = frequencyOptions,
                selectedOption = fertilizingFrequency,
                onOptionSelected = {
                    fertilizingFrequency = it
                    if (hasAttemptedSubmit) validateFertilizingFrequency()
                },
                error = fertilizingFrequencyError
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    PlantFormLabel("Last Watered Date")
                    DatePickerField(lastWateredDate) { lastWateredDate = it }
                }
                Column(modifier = Modifier.weight(1f)) {
                    PlantFormLabel("Last Fertilized Date")
                    DatePickerField(lastFertilizedDate) { lastFertilizedDate = it }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            // Submit button: validate and save form data
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    val uid = FirebaseAuth.getInstance().currentUser?.uid

                    if (uid == null) {
                        Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (validateAllFields()) {
                        Toast.makeText(context, "Saving plant data...", Toast.LENGTH_SHORT).show()

                        val imageBytes = imageUri?.let { uriToBitmap(context, it) }?.let { bitmapToByteArray(it) }

                        val newPlant = Plant(
                            name = plantName,
                            plantingDate = plantingDate,
                            plantType = plantType,
                            wateringFrequency = wateringFrequency,
                            fertilizingFrequency = fertilizingFrequency,
                            lastWateredDate = lastWateredDate,
                            lastFertilizedDate = lastFertilizedDate,
                            image = imageBytes,
                            userId = uid
                        )

                        Log.d("FormScreen", "Inserting plant: ${newPlant}")

                        viewModel.insertPlant(newPlant, homeViewModel)

                        plantName = ""
                        plantingDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                        plantType = ""
                        wateringFrequency = ""
                        fertilizingFrequency = ""
                        lastWateredDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                        lastFertilizedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-M-d"))
                        imageUri = null
                        hasAttemptedSubmit = false

                        Toast.makeText(context, "Plant saved to your collection!", Toast.LENGTH_SHORT).show()
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

@Composable
fun PlantFormLabel(label: String) {
    Text(
        text = label,
        fontSize = 14.sp,
        color = Color(0xFF4B5563),
        modifier = Modifier
            .padding(start = 4.dp, bottom = 4.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Start
    )
}


@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp),
            shape = RoundedCornerShape(12.dp),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else Color(0xFFBDBDBD),
                unfocusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else Color(0xFFBDBDBD)
            )
        )
    }
}

@Composable
fun DropdownMenuField(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp),
            shape = RoundedCornerShape(12.dp),
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else Color(0xFFBDBDBD),
                unfocusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else Color(0xFFBDBDBD)
            )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var showDialog by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFBDBDBD),
                unfocusedBorderColor = Color(0xFFBDBDBD)
            )
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                onDateSelected("$year-${month + 1}-$day")
                showDialog = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showDialog = false }
        }.show()
    }
}

@Preview(showBackground = true)
@Composable
fun FormScreenPreview() {
    _5046Theme {
        FormScreen()
    }
}
