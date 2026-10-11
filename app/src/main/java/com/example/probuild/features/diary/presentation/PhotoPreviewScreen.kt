package com.example.probuild.features.diary.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.probuild.di.ServiceLocator
import com.example.probuild.ui.theme.Dimens
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPreviewScreen(
    photoId: String,
    onBackClick: () -> Unit,
    viewModel: DiaryViewModel = viewModel(factory = ServiceLocator.provideViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val photo = uiState.photos.find { it.id == photoId }
    
    var isEditingCaption by remember { mutableStateOf(false) }
    var editedCaption by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isImageError by remember { mutableStateOf(false) }

    LaunchedEffect(photo) {
        if (photo != null) {
            editedCaption = photo.caption
        }
    }

    if (photo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Photo not found")
        }
        return
    }

    val photoFile = remember(photo.filePath) { File(photo.filePath) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Photo Preview", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isImageError || !photoFile.exists()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.BrokenImage,
                        contentDescription = "Image unavailable",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
                    Text(
                        text = "Unable to load image file.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                AsyncImage(
                    model = photoFile,
                    contentDescription = photo.caption,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    onError = { isImageError = true }
                )
            }

            // Caption overlay
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(Dimens.PaddingMedium)) {
                    if (isEditingCaption) {
                        OutlinedTextField(
                            value = editedCaption,
                            onValueChange = { editedCaption = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Caption", color = Color.White) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isEditingCaption = false }) {
                                Text("Cancel", color = Color.White)
                            }
                            Button(onClick = {
                                viewModel.updatePhotoCaption(photoId, editedCaption)
                                isEditingCaption = false
                            }) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = photo.caption.ifBlank { "No caption" },
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { isEditingCaption = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Caption", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Photo") },
            text = { Text("Are you sure you want to delete this photo evidence?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePhoto(photoId)
                        showDeleteDialog = false
                        onBackClick()
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
