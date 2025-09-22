package com.example.attendanceautomail

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attendanceautomail.ui.login.LoginScreen
import com.example.attendanceautomail.ui.login.LoginState
import com.example.attendanceautomail.ui.login.LoginViewModel
import com.example.attendanceautomail.ui.theme.AttendanceAutoMailTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AttendanceAutoMailTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val loginState by loginViewModel.loginState.collectAsState()

                    // Handle login state changes with LaunchedEffect to prevent multiple triggers
                    LaunchedEffect(loginState) {
                        when (loginState) {
                            is LoginState.Success -> {
                                val email = (loginState as LoginState.Success).email
                                Toast.makeText(
                                    this@MainActivity,
                                    "Signed in as $email",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Navigate without popping, we'll handle back navigation differently
                                navController.navigate("fileSelect")
                            }
                            is LoginState.Error -> {
                                val errorMsg = (loginState as LoginState.Error).message
                                Toast.makeText(
                                    this@MainActivity,
                                    errorMsg,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> { /* No action needed */ }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                onGoogleSignInClick = {
                                    loginViewModel.signInWithGoogle(this@MainActivity)
                                },
                                isLoading = loginState is LoginState.Loading
                            )
                        }

                        composable("fileSelect") {
                            // Temporary placeholder for file selection screen
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("File Selection Screen Coming Soon!")
                            }
                        }
                    }
                }
            }
        }
    }
}