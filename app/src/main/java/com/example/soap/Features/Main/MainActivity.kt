package com.example.soap.Features.Main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.soap.Features.NavigationBar.MainTabBar
import com.example.soap.ui.theme.SoapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoapTheme {
                MainTabBar()
            }
        }
    }
}
