package com.example.ui.modals

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.FileItem
import com.example.pdf.PdfDocumentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerModal(
    file: FileItem,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onToggleStar: (String) -> Unit,
    onDeleteFile: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var resolvedPdfFile by remember { mutableStateOf<File?>(null) }
    var totalPages by remember { mutableIntStateOf(1) }
    var currentPageIndex by remember { mutableIntStateOf(0) } // 0-based
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Page Bitmaps Cache
    val pageBitmaps = remember { mutableStateMapOf<Int, Bitmap>() }

    // Viewing options
    var isNightMode by remember { mutableStateOf(false) }
    var isContinuousScroll by remember { mutableStateOf(false) }
    var showThumbnailSheet by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Zoom and pan state for single page view
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4.5f)
        if (scale > 1f) {
            val maxOffset = (scale - 1f) * 600f
            val newX = (offset.x + offsetChange.x * scale).coerceIn(-maxOffset, maxOffset)
            val newY = (offset.y + offsetChange.y * scale).coerceIn(-maxOffset, maxOffset)
            offset = Offset(newX, newY)
        } else {
            offset = Offset.Zero
        }
    }

    // Reset zoom when page changes
    LaunchedEffect(currentPageIndex) {
        scale = 1f
        offset = Offset.Zero
    }

    // Load PDF file on launch
    LaunchedEffect(file) {
        isLoading = true
        errorMessage = null
        try {
            val pdf = PdfDocumentManager.getOrCreatePdfFile(context, file)
            resolvedPdfFile = pdf
            val pages = PdfDocumentManager.getPageCount(pdf)
            totalPages = pages.coerceAtLeast(1)
            currentPageIndex = 0

            // Render first page immediately
            val firstBmp = PdfDocumentManager.renderPageBitmap(pdf, 0)
            if (firstBmp != null) {
                pageBitmaps[0] = firstBmp
            }
            isLoading = false

            // Pre-render remaining pages in background
            scope.launch(Dispatchers.IO) {
                for (p in 1 until totalPages) {
                    val bmp = PdfDocumentManager.renderPageBitmap(pdf, p)
                    if (bmp != null) {
                        withContext(Dispatchers.Main) {
                            pageBitmaps[p] = bmp
                        }
                    }
                }
            }
        } catch (e: Exception) {
            isLoading = false
            errorMessage = e.message ?: "Failed to open document"
        }
    }

    // Render current page if not already in cache
    LaunchedEffect(currentPageIndex, resolvedPdfFile) {
        val pdf = resolvedPdfFile ?: return@LaunchedEffect
        if (!pageBitmaps.containsKey(currentPageIndex)) {
            val bmp = PdfDocumentManager.renderPageBitmap(pdf, currentPageIndex)
            if (bmp != null) {
                pageBitmaps[currentPageIndex] = bmp
            }
        }
    }

    BackHandler {
        onDismiss()
    }

    // Invert color matrix for dark night reading
    val nightColorMatrix = remember {
        ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isNightMode) Color(0xFF121212) else Color(0xFF263238)),
        color = if (isNightMode) Color(0xFF121212) else Color(0xFF263238)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isNightMode) Color(0xFF1E1E1E) else Color(0xFF1E293B),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
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
                            text = "${file.formattedSize} • ${if (language == AppLanguage.HINDI) "पेज" else "Page"} ${currentPageIndex + 1} / $totalPages",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Night Mode Toggle
                    IconButton(onClick = { isNightMode = !isNightMode }) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Night Mode",
                            tint = if (isNightMode) Color(0xFFFFD54F) else Color(0xFF818CF8)
                        )
                    }

                    // Continuous vs Single Page view toggle
                    IconButton(onClick = { isContinuousScroll = !isContinuousScroll }) {
                        Icon(
                            imageVector = if (isContinuousScroll) Icons.Default.ViewAgenda else Icons.Default.SingleBed,
                            contentDescription = "View Mode",
                            tint = Color.White
                        )
                    }

                    // Thumbnails list
                    IconButton(onClick = { showThumbnailSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Pages Grid",
                            tint = Color.White
                        )
                    }

                    // Share PDF
                    IconButton(onClick = {
                        resolvedPdfFile?.let {
                            PdfDocumentManager.sharePdfFile(context, it, file.name)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }

                    // Print PDF
                    IconButton(onClick = {
                        val pdf = resolvedPdfFile
                        if (pdf != null) {
                            val success = PdfDocumentManager.printPdfDocument(context, pdf, file.name)
                            if (!success) {
                                Toast.makeText(context, if (language == AppLanguage.HINDI) "प्रिंटिंग शुरू नहीं हो सकी" else "Unable to start printing", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, if (language == AppLanguage.HINDI) "कृपया दस्तावेज़ लोड होने की प्रतीक्षा करें" else "Please wait for document to load", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print PDF",
                            tint = Color(0xFF67E8F9)
                        )
                    }

                    // More / Info
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = Color.White
                        )
                    }
                }
            }

            // Document Canvas Viewer Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(if (isNightMode) Color(0xFF0F0F0F) else Color(0xFF1E242B)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) "दस्तावेज़ लोड हो रहा है..." else "Rendering PDF Document...",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                } else if (errorMessage != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = errorMessage ?: "Error",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                resolvedPdfFile?.let {
                                    PdfDocumentManager.openWithExternalApp(context, it)
                                }
                            }
                        ) {
                            Text(if (language == AppLanguage.HINDI) "बाहरी ऐप में खोलें" else "Open in External App")
                        }
                    }
                } else if (isContinuousScroll) {
                    // Continuous Vertical Scroll Mode
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(totalPages) { pageIdx ->
                            val bmp = pageBitmaps[pageIdx]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isNightMode) Color(0xFF1A1A1A) else Color.White)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = "${if (language == AppLanguage.HINDI) "पेज" else "Page"} ${pageIdx + 1} / $totalPages",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNightMode) Color.Gray else Color.DarkGray
                                        )
                                    }
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Page ${pageIdx + 1}",
                                            contentScale = ContentScale.FillWidth,
                                            colorFilter = if (isNightMode) ColorFilter.colorMatrix(nightColorMatrix) else null,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(400.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Single Page Mode with Pinch-to-Zoom and Double-Tap Zoom
                    val currentBitmap = pageBitmaps[currentPageIndex]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (scale > 1f) {
                                            scale = 1f
                                            offset = Offset.Zero
                                        } else {
                                            scale = 2.4f
                                        }
                                    }
                                )
                            }
                            .transformable(state = transformState),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentBitmap != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth(0.94f)
                                    .wrapContentHeight()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 10.dp,
                                color = if (isNightMode) Color(0xFF1E1E1E) else Color.White
                            ) {
                                Image(
                                    bitmap = currentBitmap.asImageBitmap(),
                                    contentDescription = "Page ${currentPageIndex + 1}",
                                    contentScale = ContentScale.FillWidth,
                                    colorFilter = if (isNightMode) ColorFilter.colorMatrix(nightColorMatrix) else null,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        // Floating Zoom Indicator if zoomed
                        if (scale > 1.05f) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.75f)
                            ) {
                                Text(
                                    text = "${(scale * 100).toInt()}%",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Controller Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isNightMode) Color(0xFF1E1E1E) else Color(0xFF1E293B),
                shadowElevation = 8.dp
            ) {
                Column {
                    // Page Progress Slider (if multi-page)
                    if (totalPages > 1) {
                        Slider(
                            value = currentPageIndex.toFloat(),
                            onValueChange = { currentPageIndex = it.toInt().coerceIn(0, totalPages - 1) },
                            valueRange = 0f..(totalPages - 1).toFloat(),
                            steps = (totalPages - 2).coerceAtLeast(0),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 0.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF38BDF8),
                                activeTrackColor = Color(0xFF38BDF8),
                                inactiveTrackColor = Color(0xFF475569)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Page Button
                        Button(
                            onClick = {
                                if (currentPageIndex > 0) currentPageIndex--
                            },
                            enabled = currentPageIndex > 0,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF334155),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF1E293B),
                                disabledContentColor = Color(0xFF64748B)
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.HINDI) "पिछला" else "Prev")
                        }

                        // Page Counter Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = "${if (language == AppLanguage.HINDI) "पेज" else "Page"} ${currentPageIndex + 1} / $totalPages",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Next Page Button
                        Button(
                            onClick = {
                                if (currentPageIndex < totalPages - 1) currentPageIndex++
                            },
                            enabled = currentPageIndex < totalPages - 1,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF334155),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF1E293B),
                                disabledContentColor = Color(0xFF64748B)
                            )
                        ) {
                            Text(if (language == AppLanguage.HINDI) "अगला" else "Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    // Thumbnails Quick Jump Sheet
    if (showThumbnailSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThumbnailSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (language == AppLanguage.HINDI) "सभी पेज ($totalPages)" else "All Pages ($totalPages)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(totalPages) { idx ->
                        val bmp = pageBitmaps[idx]
                        val isSelected = idx == currentPageIndex
                        Surface(
                            modifier = Modifier
                                .width(90.dp)
                                .height(130.dp)
                                .clickable {
                                    currentPageIndex = idx
                                    showThumbnailSheet = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
                            ),
                            color = Color.White
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Thumb $idx",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    }
                                }
                                Surface(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = if (isSelected) Color.White else Color.Black,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Document Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "दस्तावेज़ विवरण" else "Document Information",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (language == AppLanguage.HINDI) "नाम:" else "Name:", fontWeight = FontWeight.SemiBold)
                        Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (language == AppLanguage.HINDI) "साइज़:" else "Size:", fontWeight = FontWeight.SemiBold)
                        Text(file.formattedSize)
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (language == AppLanguage.HINDI) "कुल पेज:" else "Total Pages:", fontWeight = FontWeight.SemiBold)
                        Text("$totalPages")
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (language == AppLanguage.HINDI) "पाथ:" else "Path:", fontWeight = FontWeight.SemiBold)
                        Text(file.path, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        resolvedPdfFile?.let {
                            PdfDocumentManager.printPdfDocument(context, it, file.name)
                        }
                        showInfoDialog = false
                    }) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == AppLanguage.HINDI) "प्रिंट करें" else "Print")
                    }
                    TextButton(onClick = {
                        resolvedPdfFile?.let {
                            PdfDocumentManager.openWithExternalApp(context, it)
                        }
                        showInfoDialog = false
                    }) {
                        Text(if (language == AppLanguage.HINDI) "ऐप में खोलें" else "Open in App")
                    }
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text(if (language == AppLanguage.HINDI) "ठीक है" else "Close")
                    }
                }
            }
        )
    }
}
