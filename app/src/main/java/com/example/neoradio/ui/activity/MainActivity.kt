package com.example.neoradio.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.neoradio.ui.screen.main.MainScreen
import com.example.neoradio.ui.theme.NeoRadioTheme

val LocalSheetPrompt = staticCompositionLocalOf<MutableState<Boolean>> {
    error("LocalSheetPrompt not provided")
}

class MainActivity : ComponentActivity() {

    private val sheetPromptState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            NeoRadioTheme {
                CompositionLocalProvider(LocalSheetPrompt provides sheetPromptState) {
                    MainScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sheetPromptState.value = intent.extras?.containsKey("PROMPT_PLAYER") == true
    }
}