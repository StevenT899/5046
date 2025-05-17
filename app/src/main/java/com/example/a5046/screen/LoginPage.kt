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
import com.example.a5046.viewmodel.AuthState
import com.example.a5046.viewmodel.AuthViewModel
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@Composable
fun LoginScreen(authVM: AuthViewModel, onLoginSuccess: () -> Unit,onSignUpClick: () -> Unit)  {
    val context = LocalContext.current

    val authState by authVM.state.collectAsState()
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onLoginSuccess()
    }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMeChecked by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(res.data)
        runCatching { task.getResult(ApiException::class.java) }
            .onSuccess { acct ->
                acct.idToken?.let { token ->
                    authVM.signInWithGoogle(token)
                }
            }
            .onFailure { e ->
                Log.e("GoogleSign", "Login failed", e)
            }
    }
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) onLoginSuccess()
    }

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
                onClick = {authVM.signInEmail(email.trim(), password)},
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A915D))
            ) {
                Text("Sign in", color = Color.White,fontSize = 18.sp)
            }

            Button(
                onClick = { launcher.launch(googleClient.signInIntent) },
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
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }

        }
    }
}
