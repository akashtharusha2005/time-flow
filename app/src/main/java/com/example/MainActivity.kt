package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.local.ThemeMode
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.security.AppLockScreen
import com.example.ui.theme.TimeflowTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val authUiState by authViewModel.uiState.collectAsState()
            val preferences by mainViewModel.preferences.collectAsState()
            val isAppLocked by mainViewModel.isAppLocked.collectAsState()

            val isDarkTheme = when (preferences.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Sync auth state with MainViewModel
            LaunchedEffect(authUiState.currentUser) {
                mainViewModel.setUser(authUiState.currentUser)
            }

            TimeflowTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (authUiState.currentUser == null) {
                        AuthScreen(
                            authViewModel = authViewModel,
                            onAuthSuccess = {
                                mainViewModel.setUser(authUiState.currentUser)
                            }
                        )
                    } else if (isAppLocked) {
                        AppLockScreen(mainViewModel = mainViewModel)
                    } else {
                        MainScreen(
                            mainViewModel = mainViewModel,
                            onSignOut = {
                                authViewModel.signOut()
                                mainViewModel.setUser(null)
                            }
                        )
                    }
                }
            }
        }
    }
}
