package com.example.a5046.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a5046.viewmodel.AuthState
import com.example.a5046.viewmodel.AuthViewModel
import android.util.Patterns

@Composable
fun RegisterScreen(authVM: AuthViewModel,onRegisterSuccess: () -> Unit,onSignInClick: () -> Unit) {
//    auth state listener
    val authState by authVM.state.collectAsState()
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onRegisterSuccess()
    }
//form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
//    Error messages (null = no error)
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

//    validation helpers
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    //    Validate all inputs.
    fun validateInputs(): Boolean {
        var isValid = true
// Email validation
        if (email.isBlank()) {
            emailError = "Email cannot be empty"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailError = "Please enter a valid email address"
            isValid = false
        } else {
            emailError = null
        }
// Password + confirm validation
        if (password.isBlank()) {
            passwordError = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters long"
            isValid = false
        } else if (password != confirmPassword) {
            passwordError = "Passwords do not match"
            isValid = false
        } else {
            passwordError = null
        }

        return isValid
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F7F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
//                title
                text = "Create your account",
                fontSize = 24.sp,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
//            Email field
            Text(
                text = "Email",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 10.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) {
                        emailError = null // clear error while typing
                    }
                },
                label = { Text("Enter your email") },
                singleLine = true,
                isError = emailError != null,
                supportingText = {
                    if (emailError != null) {
                        Text(
                            text = emailError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth()
            )
//            Password field
            Text(
                text = "Password",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 10.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) {
                        passwordError = null
                    }
                },
                label = { Text("Enter your password") },
                singleLine = true,
                isError = passwordError != null,
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val text = if (passwordVisible) "HIDE" else "SHOW"
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(text, color = Color(0xFF3A915D), fontWeight = FontWeight.SemiBold)
                    }
                }
            )
//            Confirm Password field
            Text(
                text = "Confirm Password",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 10.dp)
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (passwordError != null) {
                        passwordError = null
                    }
                },
                label = { Text("Re-enter your password") },
                singleLine = true,
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth(),
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val text = if (confirmVisible) "HIDE" else "SHOW"
                    TextButton(onClick = { confirmVisible = !confirmVisible }) {
                        Text(text, color = Color(0xFF3A915D), fontWeight = FontWeight.SemiBold)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
//            Sign-up button
            Button(
                onClick = {
                    if (validateInputs()) {
                        //reference from AI
                        // call ViewModel registration
                        authVM.signUpEmail(email.trim(), password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A915D))
            ) {
                Text("Sign up", color = Color.White, fontSize = 18.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Have an account? ")
                Text(
                    text = "SIGN IN",
                    color = Color(0xFF3A915D),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignInClick() }
                )
            }
        }
    }
}