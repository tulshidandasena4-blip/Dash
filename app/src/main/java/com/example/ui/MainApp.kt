package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.auth.AuthViewModel
import com.example.ui.navigation.AppNavHost

@Composable
fun MainApp(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    AppNavHost(
        authViewModel = authViewModel,
        mainViewModel = mainViewModel,
        modifier = modifier
    )
}
