package com.example.soap.Features.Main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.soap.Domain.Services.AuthenticationCallbackHandler
import com.example.soap.Features.NavigationBar.MainTabBar
import com.example.soap.Features.SignIn.SignInView
import com.example.soap.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent?.data?.let { uri ->
            AuthenticationCallbackHandler.handleUri(uri)
        }

        setContent {
            Theme {
                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                when {
                    isLoading -> {}
                    isAuthenticated == true -> {
                        MainTabBar()
                    }
                    else -> {
                        SignInView()
                    }
                }
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            AuthenticationCallbackHandler.handleUri(uri)
        }
    }
}