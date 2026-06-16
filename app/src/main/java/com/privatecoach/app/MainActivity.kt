package com.privatecoach.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.privatecoach.app.ui.navigation.PrivateCoachNavHost
import com.privatecoach.app.ui.navigation.Screen
import com.privatecoach.app.ui.theme.PrivateCoachTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle widget deep link
        val navigateTo = intent?.getStringExtra("navigate_to")
        val initialRoute = when (navigateTo) {
            "record" -> Screen.Conversation
            else -> Screen.Conversation
        }

        setContent {
            PrivateCoachTheme {
                PrivateCoachNavHost(initialRoute = initialRoute)
            }
        }
    }
}
