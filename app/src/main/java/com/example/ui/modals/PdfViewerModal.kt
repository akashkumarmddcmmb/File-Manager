package com.example.ui.modals

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import kotlin.math.abs

enum class PdfDisplayFitMode {
    FIT_WIDTH,       // Fills width, scrollable up & down (Best for readability)
    CONTINUOUS_ALL,  // Continuous vertical list of all pages
    FIT_PAGE         // Fits entire page in viewport
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerModal(
    file: FileItem,
    allPdfFiles: List<FileItem> = listOf(file),
    language: AppLanguage,
    onDismiss: () -> Unit,
    onToggleStar: (String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onNavigateFile: (FileItem) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentFileIndex = remember(file.id, allPdfFiles) {
        val idx = allPdfFiles.indexOfFirst { it.id == file.id }
        if (idx >= 0) idx else 0
    }

    var resolvedPdfFile by remember { mutableStateOf<File?>(null) }
    var totalPages by remember { mutableIntStateOf(1) }
    var currentPageIndex by remember { mutableIntStateOf(0) } // 0-based
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Page Bitmaps Cache
    val pageBitmaps = remember { mutableStateMapOf<Int, Bitmap>() }

    // Viewing options
    var isNightMode by remember { mutableStateOf(false) }
    var fitMode by remember { mutableStateOf(PdfDisplayFitMode.FIT_WIDTH) }
    var showControls by remember { mutableStateOf(true) }
    var showThumbnailSheet by remember { mutableStateOf(false) }
    var showFileListSheet by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Zoom and pan state for single page view
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val verticalScrollState = rememberScrollState()

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

    // Reset zoom and scroll when page or file changes
    LaunchedEffect(currentPageIndex, file.id) {
        scale = 1f
        offset = Offset.Zero
        verticalScrollState.scrollTo(0)
    }

    // Helper functions to navigate between PDF files
    fun goToNextPdf() {
        if (allPdfFiles.isNotEmpty() && currentFileIndex < allPdfFiles.size - 1) {
            onNavigateFile(allPdfFiles[currentFileIndex + 1])
        } else {
            Toast.makeText(
                context,
                if (language == AppLanguage.HINDI) "यह अंतिम PDF दस्तावेज़ है" else "This is the last PDF document",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun goToPrevPdf() {
        if (allPdfFiles.isNotEmpty() && currentFileIndex > 0) {
            onNavigateFile(allPdfFiles[currentFileIndex - 1])
        } else {
            Toast.makeText(
                context,
                if (language == AppLanguage.HINDI) "यह पहला PDF दस्तावेज़ है" else "This is the first PDF document",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun goToNextPage() {
        if (currentPageIndex < totalPages - 1) {
            currentPageIndex++
        } else if (allPdfFiles.size > 1 && currentFileIndex < allPdfFiles.size - 1) {
            goToNextPdf()
        }
    }

    fun goToPrevPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--
        } else if (allPdfFiles.size > 1 && currentFileIndex > 0) {
            goToPrevPdf()
        }
    }

    // Load PDF file on launch or when file changes
    LaunchedEffect(file.id) {
        isLoading = true
        errorMessage = null
        pageBitmaps.clear()
        try {
            val pdf = PdfDocumentManager.getOrCreatePdfFile(context, file)
            resolvedPdfFile = pdf
            val pages = PdfDocumentManager.getPageCount(pdf)
            totalPages = pages.coerceAtLeast(1)
            currentPageIndex = 0

            // Render first page immediately in high quality
            val firstBmp = PdfDocumentManager.renderPageBitmap(pdf, 0, targetWidthPx = 1080)
            if (firstBmp != null) {
                pageBitmaps[0] = firstBmp
            }
            isLoading = false

            // Pre-render remaining pages in background
            scope.launch(Dispatchers.IO) {
                for (p in 1 until totalPages) {
                    val bmp = PdfDocumentManager.renderPageBitmap(pdf, p, targetWidthPx = 1080)
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
            val bmp = PdfDocumentManager.renderPageBitmap(pdf, currentPageIndex, targetWidthPx = 1080)
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
            .background(if (isNightMode) Color(0xFF121212) else Color(0xFF1E242B)),
        color = if (isNightMode) Color(0xFF121212) else Color(0xFF1E242B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // TOP APP BAR
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isNightMode) Color(0xFF1E1E1E) else Color(0xFF1E293B),
                    shadowElevation = 6.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
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
                                    .clickable { showFileListSheet = true }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (allPdfFiles.size > 1) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Switch PDF",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (allPdfFiles.size > 1) {
                                        "PDF ${currentFileIndex + 1}/${allPdfFiles.size} • ${file.formattedSize} • ${if (language == AppLanguage.HINDI) "पेज" else "Page"} ${currentPageIndex + 1}/$totalPages"
                                    } else {
                                        "${file.formattedSize} • ${if (language == AppLanguage.HINDI) "पेज" else "Page"} ${currentPageIndex + 1}/$totalPages"
                                    },
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

                            // View Fit Mode Toggle (Fit Width vs Continuous vs Fit Page)
                            IconButton(onClick = {
                                fitMode = when (fitMode) {
                                    PdfDisplayFitMode.FIT_WIDTH -> PdfDisplayFitMode.CONTINUOUS_ALL
                                    PdfDisplayFitMode.CONTINUOUS_ALL -> PdfDisplayFitMode.FIT_PAGE
                                    PdfDisplayFitMode.FIT_PAGE -> PdfDisplayFitMode.FIT_WIDTH
                                }
                                val modeLabel = when (fitMode) {
                                    PdfDisplayFitMode.FIT_WIDTH -> if (language == AppLanguage.HINDI) "चौड़ाई में फिट (स्क्रॉल)" else "Fit Width (Scrollable)"
                                    PdfDisplayFitMode.CONTINUOUS_ALL -> if (language == AppLanguage.HINDI) "लगातार सभी पेज" else "Continuous Scroll"
                                    PdfDisplayFitMode.FIT_PAGE -> if (language == AppLanguage.HINDI) "पूरा पेज फिट" else "Fit Whole Page"
                                }
                                Toast.makeText(context, modeLabel, Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = when (fitMode) {
                                        PdfDisplayFitMode.FIT_WIDTH -> Icons.Default.ViewStream
                                        PdfDisplayFitMode.CONTINUOUS_ALL -> Icons.Default.ViewAgenda
                                        PdfDisplayFitMode.FIT_PAGE -> Icons.Default.Fullscreen
                                    },
                                    contentDescription = "Fit Mode",
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

                            // Print PDF
                            IconButton(onClick = {
                                val pdf = resolvedPdfFile
                                if (pdf != null) {
                                    val success = PdfDocumentManager.printPdfDocument(context, pdf, file.name)
                                    if (!success) {
                                        Toast.makeText(
                                            context,
                                            if (language == AppLanguage.HINDI) "प्रिंटिंग शुरू नहीं हो सकी" else "Unable to start printing",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (language == AppLanguage.HINDI) "कृपया लोड होने की प्रतीक्षा करें" else "Please wait for document to load",
                                        Toast.LENGTH_SHORT
                                    ).show()
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

                        // MULTI-DOCUMENT QUICK SLIDE SWITCHER BAR (If multiple PDFs exist)
                        if (allPdfFiles.size > 1) {
                            Surface(
                                color = Color(0xFF0F172A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Previous Document Button
                                    TextButton(
                                        onClick = { goToPrevPdf() },
                                        enabled = currentFileIndex > 0,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.NavigateBefore,
                                            contentDescription = null,
                                            tint = if (currentFileIndex > 0) Color(0xFF38BDF8) else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "पिछला PDF" else "Prev PDF",
                                            fontSize = 11.sp,
                                            color = if (currentFileIndex > 0) Color(0xFF38BDF8) else Color.Gray
                                        )
                                    }

                                    // Document Counter & Switcher Chip
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.clickable { showFileListSheet = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Description,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB74D),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "PDF ${currentFileIndex + 1} / ${allPdfFiles.size}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Next Document Button
                                    TextButton(
                                        onClick = { goToNextPdf() },
                                        enabled = currentFileIndex < allPdfFiles.size - 1,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "अगला PDF" else "Next PDF",
                                            fontSize = 11.sp,
                                            color = if (currentFileIndex < allPdfFiles.size - 1) Color(0xFF38BDF8) else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            Icons.AutoMirrored.Filled.NavigateNext,
                                            contentDescription = null,
                                            tint = if (currentFileIndex < allPdfFiles.size - 1) Color(0xFF38BDF8) else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DOCUMENT CANVAS VIEWER BODY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(if (isNightMode) Color(0xFF0A0A0A) else Color(0xFF1E242B)),
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
                            text = if (language == AppLanguage.HINDI) "PDF दस्तावेज़ लोड हो रहा है..." else "Loading PDF Document...",
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
                } else if (fitMode == PdfDisplayFitMode.CONTINUOUS_ALL) {
                    // 1. CONTINUOUS VERTICAL SCROLL MODE (All pages scrollable vertically)
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { showControls = !showControls }
                                )
                            },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(totalPages) { pageIdx ->
                            val bmp = pageBitmaps[pageIdx]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isNightMode) Color(0xFF1E1E1E) else Color.White
                                )
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isNightMode) Color(0xFF262626) else Color(0xFFF1F5F9))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = "${if (language == AppLanguage.HINDI) "पेज" else "Page"} ${pageIdx + 1} / $totalPages",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNightMode) Color(0xFF94A3B8) else Color(0xFF475569)
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
                                                .height(450.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (fitMode == PdfDisplayFitMode.FIT_WIDTH) {
                    // 2. FIT WIDTH MODE - FULL WIDTH AND SMOOTH VERTICAL SCROLL (ऊपर नीचे स्क्रॉल)
                    val currentBitmap = pageBitmaps[currentPageIndex]
                    var totalDragX by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(file.id, currentPageIndex) {
                                detectTapGestures(
                                    onTap = { showControls = !showControls },
                                    onDoubleTap = {
                                        if (scale > 1f) {
                                            scale = 1f
                                            offset = Offset.Zero
                                        } else {
                                            scale = 2.2f
                                        }
                                    }
                                )
                            }
                            .pointerInput(file.id, currentPageIndex, scale) {
                                if (scale == 1f) {
                                    // Detect horizontal slide gesture to switch pages or PDF files
                                    detectDragGestures(
                                        onDragStart = { totalDragX = 0f },
                                        onDragEnd = {
                                            if (totalDragX < -150f) {
                                                goToNextPage()
                                            } else if (totalDragX > 150f) {
                                                goToPrevPage()
                                            }
                                            totalDragX = 0f
                                        },
                                        onDragCancel = { totalDragX = 0f },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            totalDragX += dragAmount.x
                                        }
                                    )
                                }
                            }
                            .transformable(state = transformState),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (currentBitmap != null) {
                            // Vertically scrollable container so any long document can be scrolled up/down
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(verticalScrollState)
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offset.x,
                                            translationY = offset.y
                                        ),
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 8.dp,
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
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        // Floating Zoom Indicator
                        if (scale > 1.05f) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.8f)
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
                } else {
                    // 3. FIT PAGE MODE (Fits entire page inside viewport height & width)
                    val currentBitmap = pageBitmaps[currentPageIndex]
                    var totalDragX by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(file.id, currentPageIndex) {
                                detectTapGestures(
                                    onTap = { showControls = !showControls },
                                    onDoubleTap = {
                                        if (scale > 1f) {
                                            scale = 1f
                                            offset = Offset.Zero
                                        } else {
                                            scale = 2.2f
                                        }
                                    }
                                )
                            }
                            .pointerInput(file.id, currentPageIndex, scale) {
                                if (scale == 1f) {
                                    detectDragGestures(
                                        onDragStart = { totalDragX = 0f },
                                        onDragEnd = {
                                            if (totalDragX < -150f) {
                                                goToNextPage()
                                            } else if (totalDragX > 150f) {
                                                goToPrevPage()
                                            }
                                            totalDragX = 0f
                                        },
                                        onDragCancel = { totalDragX = 0f },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            totalDragX += dragAmount.x
                                        }
                                    )
                                }
                            }
                            .transformable(state = transformState),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentBitmap != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                shape = RoundedCornerShape(6.dp),
                                color = if (isNightMode) Color(0xFF1E1E1E) else Color.White
                            ) {
                                Image(
                                    bitmap = currentBitmap.asImageBitmap(),
                                    contentDescription = "Page ${currentPageIndex + 1}",
                                    contentScale = ContentScale.Fit,
                                    colorFilter = if (isNightMode) ColorFilter.colorMatrix(nightColorMatrix) else null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        } else {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

            // BOTTOM NAVIGATION CONTROLLER BAR
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
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
                                onClick = { goToPrevPage() },
                                enabled = currentPageIndex > 0 || (allPdfFiles.size > 1 && currentFileIndex > 0),
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
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.clickable { showThumbnailSheet = true }
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
                                onClick = { goToNextPage() },
                                enabled = currentPageIndex < totalPages - 1 || (allPdfFiles.size > 1 && currentFileIndex < allPdfFiles.size - 1),
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
    }

    // ALL PDF FILES SELECTOR SHEET (Swipe/Pick other documents)
    if (showFileListSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFileListSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "सभी PDF दस्तावेज़ (${allPdfFiles.size})" else "All PDF Documents (${allPdfFiles.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { showFileListSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(allPdfFiles) { idx, item ->
                        val isSelected = item.id == file.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onNavigateFile(item)
                                    showFileListSheet = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFEF5350).copy(alpha = 0.15f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else Color(0xFFEF5350),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = item.formattedSize,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "खुला है" else "Active",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // THUMBNAILS QUICK JUMP SHEET
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

    // DOCUMENT INFO DIALOG
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
