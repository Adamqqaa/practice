package ci.nsu.moble.main.ui.Screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ci.nsu.moble.main.data.db.CatDatabase
import ci.nsu.moble.main.data.db.CatEntity
import ci.nsu.moble.main.data.repository.CatRepository
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(private val repository: CatRepository) : ViewModel() {
    val cats: StateFlow<List<CatEntity>> = repository.getAllCatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var isEditMode by mutableStateOf(false)
        private set

    var selectedCats by mutableStateOf<Set<Long>>(emptySet())
        private set

    fun toggleEditMode() {
        isEditMode = !isEditMode
        if (!isEditMode) {
            selectedCats = emptySet()
        }
    }

    fun toggleCatSelection(catId: Long) {
        selectedCats = selectedCats.toMutableSet().apply {
            if (contains(catId)) remove(catId) else add(catId)
        }
    }

    fun deleteCat(cat: CatEntity) {
        viewModelScope.launch {
            repository.deleteCat(cat)
        }
    }

    fun deleteSelectedCats() {
        viewModelScope.launch {
            repository.deleteCatsByIds(selectedCats.toList())
            selectedCats = emptySet()
            isEditMode = false
        }
    }
}

class HistoryViewModelFactory(private val repository: CatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { CatDatabase.getDatabase(context) }
    val repository = remember { CatRepository(database.catDao()) }
    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(repository)
    )

    val cats by viewModel.cats.collectAsStateWithLifecycle()
    val isEditMode = viewModel.isEditMode
    val selectedCats = viewModel.selectedCats

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    var catToDelete by remember { mutableStateOf<CatEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История котиков") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    if (cats.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.toggleEditMode() }
                        ) {
                            Text(if (isEditMode) "Готово" else "Править")
                        }
                        if (isEditMode && selectedCats.isNotEmpty()) {
                            TextButton(
                                onClick = { showDeleteSelectedDialog = true }
                            ) {
                                Text(
                                    "Удалить (${selectedCats.size})",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("История пуста", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cats, key = { it.id }) { cat ->
                    CatHistoryItem(
                        cat = cat,
                        isEditMode = isEditMode,
                        isSelected = selectedCats.contains(cat.id),
                        onItemClick = {
                            if (isEditMode) {
                                viewModel.toggleCatSelection(cat.id)
                            } else {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT,
                                        "Смотри какой котик! 🐱\n${cat.imageUrl}"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "Поделиться")
                                )
                            }
                        },
                        onLongClick = {
                            if (!isEditMode) {
                                catToDelete = cat
                                showDeleteDialog = true
                            }
                        }
                    )
                }
            }
        }

        if (showDeleteDialog && catToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    catToDelete = null
                },
                title = { Text("Удалить картинку?") },
                text = { Text("Вы уверены, что хотите удалить эту картинку из истории?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            catToDelete?.let { viewModel.deleteCat(it) }
                            showDeleteDialog = false
                            catToDelete = null
                        }
                    ) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        catToDelete = null
                    }) {
                        Text("Отмена")
                    }
                }
            )
        }

        if (showDeleteSelectedDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteSelectedDialog = false },
                title = { Text("Удалить выбранные?") },
                text = {
                    Text("Вы уверены, что хотите удалить ${selectedCats.size} выбранных картинок?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSelectedCats()
                            showDeleteSelectedDialog = false
                        }
                    ) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteSelectedDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CatHistoryItem(
    cat: CatEntity,
    isEditMode: Boolean,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = cat.imageUrl,
                contentDescription = "Котик",
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(cat.dateAdded),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cat.imageUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isEditMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onItemClick() }
                )
            } else {
                IconButton(onClick = onItemClick) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Поделиться",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}