package com.greenezone.chromavale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.greenezone.chromavale.ui.PhotoEditorScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.rgb(15, 19, 25)
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.rgb(15, 19, 25)
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
