package ci.nsu.mobile.main.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ci.nsu.mobile.main.viewmodel.CatImage
import ci.nsu.mobile.main.viewmodel.CatViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CatViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.toggleEditMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF69B4),
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (uiState.isEditMode) "Cancel" else "Edit")
                    }

                    if (uiState.isEditMode && uiState.selectedImages.isNotEmpty()) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Delete (${uiState.selectedImages.size})")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF69B4),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = uiState.historyImages,
                key = { it.id }
            ) { image ->
                HistoryCard(
                    image = image,
                    isEditMode = uiState.isEditMode,
                    isSelected = uiState.selectedImages.contains(image),
                    onSelect = { viewModel.toggleImageSelection(image) },
                    onDelete = { viewModel.deleteImage(image) },
                    onShare = {
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, image.url)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share cat"))
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete images") },
            text = { Text("Are you sure you want to delete ${uiState.selectedImages.size} image(s)?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelectedImages()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HistoryCard(
    image: CatImage,
    isEditMode: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // Красный фон для свайпа (виден при сдвиге)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Red)
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text("Delete", color = Color.White)
        }

        // Основная карточка
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -200f) {
                                onDelete()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-300f, 0f)
                        }
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect() }
                    )
                }

                AsyncImage(
                    model = image.url,
                    contentDescription = "Cat from history",
                    modifier = Modifier.size(60.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = image.url,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(image.date),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                if (!isEditMode) {
                    Button(
                        onClick = onShare,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF69B4)
                        )
                    ) {
                        Text("Share")
                    }
                }
            }
        }
    }
}