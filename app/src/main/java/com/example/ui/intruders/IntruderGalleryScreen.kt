package com.example.ui.intruders

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.IntruderGalleryRepository
import com.example.domain.model.IntruderCapture
import com.example.platform.localization.appString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class IntruderUiState(
    val captures: List<IntruderCapture> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCaptureForPreview: IntruderCapture? = null
)

class IntruderViewModel(
    private val context: android.content.Context,
    private val dataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(IntruderUiState())
    val uiState: StateFlow<IntruderUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
        viewModelScope.launch {
            IntruderGalleryRepository.captures.collect { list ->
                _uiState.value = _uiState.value.copy(captures = list)
            }
        }
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            IntruderGalleryRepository.loadCaptures(context)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectCaptureForPreview(capture: IntruderCapture?) {
        _uiState.value = _uiState.value.copy(selectedCaptureForPreview = capture)
    }

    fun deleteCapture(capture: IntruderCapture) {
        viewModelScope.launch {
            IntruderGalleryRepository.deleteCapture(context, capture)
            if (_uiState.value.selectedCaptureForPreview?.id == capture.id) {
                _uiState.value = _uiState.value.copy(selectedCaptureForPreview = null)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            IntruderGalleryRepository.clearAllCaptures(context)
            _uiState.value = _uiState.value.copy(selectedCaptureForPreview = null)
        }
    }

    class Factory(
        private val context: android.content.Context,
        private val dataStore: SettingsDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IntruderViewModel(context, dataStore) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntruderGalleryScreen(
    viewModel: IntruderViewModel,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = appString("intruders"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.captures.size} photos captured",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (uiState.captures.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear all photos")
                        }
                    }
                    IconButton(onClick = { viewModel.loadPhotos() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.captures.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Text(
                        text = "No Intruder Photos Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "When intruder detection is active, secret front camera selfies will be securely preserved here after failed passcode attempts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.captures, key = { it.id }) { capture ->
                        IntruderPhotoCard(
                            capture = capture,
                            onClick = { viewModel.selectCaptureForPreview(capture) }
                        )
                    }
                }
            }
        }
    }

    // Photo Preview Dialog
    uiState.selectedCaptureForPreview?.let { capture ->
        AlertDialog(
            onDismissRequest = { viewModel.selectCaptureForPreview(null) },
            title = {
                Text(
                    text = "Intruder Alert: ${capture.targetAppName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dateFormatted = SimpleDateFormat("MMM d, yyyy • HH:mm:ss", Locale.getDefault()).format(Date(capture.timestamp))

                    val file = File(capture.filePath)
                    if (file.exists()) {
                        val bitmap = remember(capture.filePath) {
                            BitmapFactory.decodeFile(capture.filePath)?.asImageBitmap()
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Intruder selfie",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Text(
                        text = "Timestamp: $dateFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Target Package: ${capture.targetPackage}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteCapture(capture) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(appString("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectCaptureForPreview(null) }) {
                    Text(appString("cancel"))
                }
            }
        )
    }

    // Clear All Confirmation
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(appString("clear_all"), fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all captured intruder photos? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAll()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(appString("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(appString("cancel"))
                }
            }
        )
    }
}

@Composable
fun IntruderPhotoCard(
    capture: IntruderCapture,
    onClick: () -> Unit
) {
    val dateFormatted = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(capture.timestamp))
    val file = File(capture.filePath)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (file.exists()) {
                    val bitmap = remember(capture.filePath) {
                        BitmapFactory.decodeFile(capture.filePath)?.asImageBitmap()
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.PersonOff, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Icon(Icons.Default.BrokenImage, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = capture.targetAppName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
