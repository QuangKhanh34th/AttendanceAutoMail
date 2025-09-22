package com.example.attendanceautomail.ui.login

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading

                val credentialManager = CredentialManager.create(activity)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(SERVER_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val response = credentialManager.getCredential(
                    request = request,
                    context = activity
                )

                handleSignInResponse(response)
            } catch (e: GetCredentialException) {
                Log.e("LoginViewModel", "Error signing in: ${e.message}")
                _loginState.value = LoginState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    private fun handleSignInResponse(response: GetCredentialResponse) {
        try {
            val credential = response.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            // Here you would typically send this token to your backend or use it to authenticate with Firebase
            // For this example, we'll just consider the user signed in if we get a token

            val email = googleIdTokenCredential.id
            _loginState.value = LoginState.Success(email)

        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error processing credentials: ${e.message}")
            _loginState.value = LoginState.Error(e.message ?: "Failed to process credentials")
        }
    }

    companion object {
        // You'll need to replace this with your own server client ID from Google Cloud Console
        private const val SERVER_CLIENT_ID = "362212576102-1v5dg5ae5018d8131nrh2pdnsan66ee9.apps.googleusercontent.com"
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val email: String) : LoginState()
    data class Error(val message: String) : LoginState()
}