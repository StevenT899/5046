package com.example.a5046.screen


import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a5046.R

@Composable
fun LoginScreen()  {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMeChecked by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(), color = Color(0xFFF1F7F5)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.plantimg),
                contentDescription = "Logo",
                modifier = Modifier.size(180.dp)
            )
            Text(
                text = "Welcome to PlantEase",
                fontSize = 24.sp,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Text(
                text = "Email",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your email") },
                singleLine = true,
                modifier = Modifier.align(Alignment.Start).fillMaxWidth()
            )
            Text(
                text = "Password",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start).padding(top = 10.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter your password") },
                singleLine = true,
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), //reference from chatGPT
                trailingIcon = {
                    val text = if (passwordVisible) "HIDE" else "SHOW"
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(text, color = Color(0xFF3A915D), fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMeChecked,
                    onCheckedChange = { rememberMeChecked = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3A915D))
                )
                Text(
                    text = "Remember me",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Forgot Password?",
                    color = Color(0xFF3A915D),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A915D))
            ) {
                Text("Sign in", color = Color.White,fontSize = 18.sp)
            }

            Button(
                onClick = { /* TODO: launchGoogleSignIn() */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    "Sign in with Google",
                    fontSize = 18.sp,
                    color = Color(0xFF3A915D)
                )
            }
            Row (modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center){
                Text("Don't have an account? ")
                Text(
                    text = "SIGN UP",
                    color = Color(0xFF3A915D),
                    fontWeight = FontWeight.Bold,
                )
            }



        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginScreen()
}
