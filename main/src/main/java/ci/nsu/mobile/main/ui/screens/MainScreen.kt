package ci.nsu.mobile.main.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ci.nsu.mobile.main.viewmodel.CatViewModel

@Composable
fun MainScreen(
    viewModel: CatViewModel = viewModel(),
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.currentCatUrl.isNotEmpty() && !uiState.isLoading) {
            AsyncImage(
                model = uiState.currentCatUrl,
                contentDescription = "Random cat",
                modifier = Modifier.size(300.dp)
            )
        } else if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Text("Press button to load a cat!")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.loadNewCat() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF69B4),
                contentColor = Color.White
            )
        ) {
            Text("Load new cat")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToHistory,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF69B4),
                contentColor = Color.White
            )
        ) {
            Text("History")
        }
    }
}