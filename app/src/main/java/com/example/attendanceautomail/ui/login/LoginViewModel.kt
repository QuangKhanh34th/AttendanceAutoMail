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

    /*
    User clicks "Sign in with Google" button
    ↓
    CredentialManager shows Google account picker
    ↓
    User selects account
    ↓
    Google returns a credential object
    ↓
    We convert it to GoogleIdTokenCredential
    ↓
    We extract the ID token for server authentication
     */
    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            try {
                // 1. Set loading state
                _loginState.value = LoginState.Loading

                // 2. Initialize credential manager
                val credentialManager = CredentialManager.create(activity)

                // 3. Configure Google Sign-In options
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Show all Google accounts, not just previously used ones
                    .setServerClientId(SERVER_CLIENT_ID) // Server client ID so Google recognizes the app
                    .build()

                // 4. Create credential request from the IdOption configured above
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // 5. Request credentials (Show Google's account picker and wait for user selection)
                val response = credentialManager.getCredential(
                    request = request,
                    context = activity
                )

                // 6. Handle response
                handleSignInResponse(response)
            } catch (e: GetCredentialException) {
                Log.e("LoginViewModel", "Error signing in: ${e.message}")
                _loginState.value = LoginState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    private fun handleSignInResponse(response: GetCredentialResponse) {
        try {
            // Get the credential data
            val credential = response.credential

            // Convert it to a Google ID token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Get the actual token and email
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