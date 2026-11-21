package com.greenezone.gzphotoeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.greenezone.gzphotoeditor.ui.PhotoEditorScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF9BE7C4),
                onPrimary = Color(0xFF0E2821),
                secondary = Color(0xFFDFC59E),
                background = Color(0xFF0F1319),
                surface = Color(0xFF171D25),
                onSurface = Color(0xFFF2F5F5),
                surfaceVariant = Color(0xFF222A34),
                onSurfaceVariant = Color(0xFFCDD7DB)
            )) { PhotoEditorScreen() }
        }
    }
}
