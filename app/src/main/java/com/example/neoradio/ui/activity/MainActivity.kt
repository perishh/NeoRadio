package com.example.neoradio.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.neoradio.ui.screen.main.MainScreen
import com.example.neoradio.ui.theme.NeoRadioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeoRadioTheme {
                MainScreen()
            }
        }
    }
}