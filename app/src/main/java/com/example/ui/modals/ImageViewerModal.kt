package com.example.ui.modals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AppLanguage
import com.example.model.FileItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerModal(
    file: FileItem,
    allImageFiles: List<FileItem>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onToggleStar: (String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onNavigateFile: (FileItem) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showControls by remember { mutableStateOf(true) }
    var showInfoSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentIndex = remember(file, allImageFiles) {
        allImageFiles.indexOfFirst { it.id == file.id }.coerceAtLeast(0)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main Image Box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showControls = !showControls }
                    .pointerInput(file.id) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.8f, 5f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val imageFile = File(file.path)
                val imageModel = if (imageFile.exists() && imageFile.canRead()) {
                    imageFile
                } else {
                    "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1080&q=80"
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = file.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .rotate(rotationAngle)
                )
            }

            // Top Toolbar
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${file.formattedSize} • ${file.extension.uppercase()} ${if (allImageFiles.isNotEmpty()) "(${currentIndex + 1}/${allImageFiles.size})" else ""}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(onClick = { rotationAngle = (rotationAngle + 90f) % 360f }) {
                            Icon(
                                imageVector = Icons.Default.RotateRight,
                                contentDescription = "Rotate",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = { onToggleStar(file.id) }) {
                            Icon(
                                imageVector = if (file.isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Star",
                                tint = if (file.isStarred) Color(0xFFF9AB00) else Color.White
                            )
                        }

                        IconButton(onClick = { showInfoSheet = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Details",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom Navigation & Actions Bar
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = Color.Black.copy(alpha = 0.75f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentIndex > 0) {
                                        scale = 1f
                                        rotationAngle = 0f
                                        offsetX = 0f
                                        offsetY = 0f
                                        onNavigateFile(allImageFiles[currentIndex - 1])
                                    }
                                },
                                enabled = currentIndex > 0
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous",
                                    tint = if (currentIndex > 0) Color.White else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (scale == 1f) Color(0xFF1E8E3E) else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.clickable {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                ) {
                                    Text(
                                        text = "1x",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = if (scale == 2f) Color(0xFF1E8E3E) else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.clickable {
                                        scale = 2f
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                ) {
                                    Text(
                                        text = "2x",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = if (scale == 3f) Color(0xFF1E8E3E) else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.clickable {
                                        scale = 3f
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                ) {
                                    Text(
                                        text = "3x",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (currentIndex < allImageFiles.size - 1) {
                                        scale = 1f
                                        rotationAngle = 0f
                                        offsetX = 0f
                                        offsetY = 0f
                                        onNavigateFile(allImageFiles[currentIndex + 1])
                                    }
                                },
                                enabled = currentIndex < allImageFiles.size - 1
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next",
                                    tint = if (currentIndex < allImageFiles.size - 1) Color.White else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { showInfoSheet = true }
                            ) {
                                Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color.White)
                                Text(
                                    text = if (language == AppLanguage.HINDI) "विवरण" else "Details",
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    onDeleteFile(file.id)
                                    onDismiss()
                                }
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A80))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "हटाएं" else "Delete",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF8A80)
                                )
                            }
                        }
                    }
                }
            }

            if (showInfoSheet) {
                AlertDialog(
                    onDismissRequest = { showInfoSheet = false },
                    title = {
                        Text(
                            text = if (language == AppLanguage.HINDI) "फोटो विवरण" else "Photo Details",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("नाम: ${file.name}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("साइज़: ${if (language == AppLanguage.HINDI) "साइज़" else "Size"}: ${file.formattedSize}")
                            Text("फॉर्मेट: ${if (language == AppLanguage.HINDI) "फॉर्मेट" else "Format"}: ${file.extension.uppercase()}")
                            Text("पाथ: ${if (language == AppLanguage.HINDI) "पाथ" else "Path"}: ${file.path}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showInfoSheet = false }) {
                            Text(if (language == AppLanguage.HINDI) "बंद करें" else "Close")
                        }
                    }
                )
            }
        }
    }
}
