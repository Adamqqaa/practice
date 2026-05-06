package ci.nsu.moble.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ci.nsu.moble.main.ui.Screens.HistoryScreen
import ci.nsu.moble.main.ui.theme.PracticeTheme

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PracticeTheme {
                HistoryScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}