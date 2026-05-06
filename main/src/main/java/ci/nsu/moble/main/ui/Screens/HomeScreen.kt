package ci.nsu.moble.main.ui.Screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ci.nsu.moble.main.HistoryActivity
import ci.nsu.moble.main.data.db.CatDatabase
import ci.nsu.moble.main.data.db.CatEntity
import ci.nsu.moble.main.data.repository.CatRepository
import ci.nsu.moble.main.ui.theme.PracticeTheme
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HomeViewModel(private val repository: CatRepository) : ViewModel() {
    var currentCatUrl by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadNewCat()
    }

    fun loadNewCat() {
        viewModelScope.launch {
            isLoading = true
            try {
                val catUrl = fetchRandomCatUrl()
                currentCatUrl = catUrl

                val catEntity = CatEntity(
                    imageUrl = catUrl,
                    dateAdded = System.currentTimeMillis()
                )
                repository.insertCat(catEntity)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchRandomCatUrl(): String = withContext(Dispatchers.IO) {
        val url = URL("https://placecats.com/neo/300/200")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        connection.instanceFollowRedirects = false
        connection.connect()

        val redirectUrl = connection.getHeaderField("Location")
            ?: "https://placecats.com/neo/300/200"

        connection.disconnect()
        redirectUrl
    }
}

class HomeViewModelFactory(private val repository: CatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val database = remember { CatDatabase.getDatabase(context) }
    val repository = remember { CatRepository(database.catDao()) }
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
        } else {
            viewModel.currentCatUrl?.let { url ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = "Случайный котик",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Button(
            onClick = {
                val intent = Intent(context, HistoryActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading
        ) {
            Icon(
                Icons.Filled.History,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("История")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PracticeTheme {
        HomeScreen()
    }
}