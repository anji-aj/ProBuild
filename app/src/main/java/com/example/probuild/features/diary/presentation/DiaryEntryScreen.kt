package com.example.probuild.features.diary.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.probuild.di.ServiceLocator
import com.example.probuild.features.diary.domain.model.DiaryPhoto
import com.example.probuild.ui.components.NumericInputField
import com.example.probuild.ui.theme.Dimens
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryScreen(
    entryId: String? = null,
    onBackClick: () -> Unit,
    onNavigateToPhotoPreview: (String) -> Unit,
    viewModel: DiaryViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCameraPermissionError by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempPhotoUri?.let { viewModel.onPhotoAdded(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onPhotoAdded(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            showCameraPermissionError = false
            try {
                val photoFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                // Graceful error handling for temporary file creation
            }
        } else {
            showCameraPermissionError = true
        }
    }

    fun launchCameraFlow() {
        viewModel.ensureEntryId()
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            showCameraPermissionError = false
            try {
                val photoFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                // Graceful error handling for temporary file creation
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    LaunchedEffect(uiState.isEntrySaved, uiState.isEntryDeleted) {
        if (uiState.isEntrySaved || uiState.isEntryDeleted) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId == null && uiState.entryId == null) "New Entry" else "Edit Entry", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.entryId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = viewModel::saveEntry) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(Dimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
        ) {
            val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
            OutlinedTextField(
                value = dateFormat.format(Date(uiState.date)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            OutlinedTextField(
                value = uiState.workCompleted,
                onValueChange = viewModel::onWorkCompletedChange,
                label = { Text("Work Completed*") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.workCompletedError != null,
                supportingText = uiState.workCompletedError?.let { { Text(it) } },
                minLines = 3,
                maxLines = 5,
                shape = MaterialTheme.shapes.large
            )

            NumericInputField(
                value = uiState.labourCount,
                onValueChange = viewModel::onLabourCountChange,
                label = "Labour Count",
                error = uiState.labourCountError
            )

            OutlinedTextField(
                value = uiState.materialsUsed,
                onValueChange = viewModel::onMaterialsUsedChange,
                label = { Text("Materials Used") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            OutlinedTextField(
                value = uiState.equipmentUsed,
                onValueChange = viewModel::onEquipmentUsedChange,
                label = { Text("Equipment Used") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            OutlinedTextField(
                value = uiState.weather,
                onValueChange = viewModel::onWeatherChange,
                label = { Text("Weather") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            OutlinedTextField(
                value = uiState.issues,
                onValueChange = viewModel::onIssuesChange,
                label = { Text("Issues / Delays") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Additional Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = MaterialTheme.shapes.large
            )

            SectionHeader("Site Photos")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium)
            ) {
                Button(
                    onClick = { launchCameraFlow() },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null)
                    Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                    Text("Take Photo")
                }
                
                OutlinedButton(
                    onClick = { 
                        viewModel.ensureEntryId()
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(Dimens.PaddingSmall))
                    Text("Gallery")
                }
            }

            if (uiState.photos.isEmpty()) {
                Text(
                    text = "No photos attached",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Dimens.PaddingMedium)
                )
            } else {
                PhotoGrid(
                    photos = uiState.photos,
                    onPhotoClick = onNavigateToPhotoPreview,
                    onDeleteClick = viewModel::deletePhoto
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

            Button(
                onClick = viewModel::saveEntry,
                modifier = Modifier.fillMaxWidth().height(Dimens.MinTouchTarget),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Save Entry")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDateChange(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCameraPermissionError) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionError = false },
            title = { Text("Camera Permission Required") },
            text = { Text("Camera permission is required to capture site photos. Please allow camera access and try again.") },
            confirmButton = {
                TextButton(onClick = {
                    showCameraPermissionError = false
                    launchCameraFlow()
                }) { Text("Retry") }
            },
            dismissButton = {
                TextButton(onClick = { showCameraPermissionError = false }) { Text("Cancel") }
            }
        )
    }

    uiState.photoError?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { viewModel.clearPhotoError() },
            title = { Text("Photo Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearPhotoError() }) { Text("OK") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete this diary entry? All associated photos will also be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.entryId?.let { viewModel.deleteEntry(it) }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PhotoGrid(
    photos: List<DiaryPhoto>,
    onPhotoClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val chunks = photos.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)) {
        chunks.forEach { rowPhotos ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)) {
                rowPhotos.forEach { photo ->
                    PhotoThumbnail(
                        photo = photo,
                        onClick = { onPhotoClick(photo.id) },
                        onDelete = { onDeleteClick(photo.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowPhotos.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: DiaryPhoto,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Box {
            var isImageError by remember { mutableStateOf(false) }
            val photoFile = remember(photo.filePath) { File(photo.filePath) }

            if (isImageError || !photoFile.exists()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.BrokenImage,
                        contentDescription = "Image unavailable",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AsyncImage(
                    model = photoFile,
                    contentDescription = photo.caption.ifBlank { "Site photo" },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = { isImageError = true }
                )
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                    .size(32.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove photo", modifier = Modifier.size(16.dp))
            }
            
            if (photo.caption.isNotBlank()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = photo.caption,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(4.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = Dimens.PaddingSmall)
    )
}
