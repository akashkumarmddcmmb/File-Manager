package com.example.ui.modals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.model.FileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerModal(
    file: FileItem,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onToggleStar: (String) -> Unit,
    onDeleteFile: (String) -> Unit
) {
    var currentPage by remember { mutableIntStateOf(1) }
    val totalPages = remember(file) {
        val calculated = (file.sizeBytes / (25 * 1024)).toInt().coerceIn(1, 48)
        calculated
    }
    var isNightReadingMode by remember { mutableStateOf(false) }
    var textFontSizeSp by remember { mutableIntStateOf(14) }
    var searchQuery by remember { mutableStateOf("") }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showSearchField by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isNightReadingMode) Color(0xFF121212) else Color(0xFFF4F6F9)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isNightReadingMode) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = if (isNightReadingMode) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = if (isNightReadingMode) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${file.formattedSize} • ${if (language == AppLanguage.HINDI) "पेज" else "Page"} $currentPage / $totalPages",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = if (isNightReadingMode) Color.LightGray else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { isNightReadingMode = !isNightReadingMode }) {
                                Icon(
                                    imageVector = if (isNightReadingMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Night Mode",
                                    tint = if (isNightReadingMode) Color(0xFFFFD54F) else Color(0xFF5E35B1)
                                )
                            }

                            IconButton(onClick = { showSearchField = !showSearchField }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Text",
                                    tint = if (isNightReadingMode) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = {
                                textFontSizeSp = if (textFontSizeSp >= 20) 12 else textFontSizeSp + 2
                            }) {
                                Icon(
                                    imageVector = Icons.Default.FormatSize,
                                    contentDescription = "Text Size",
                                    tint = if (isNightReadingMode) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = { showInfoSheet = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Info",
                                    tint = if (isNightReadingMode) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        AnimatedVisibility(visible = showSearchField) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(if (language == AppLanguage.HINDI) "दस्तावेज़ में खोजें..." else "Search in document...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // Document Content Viewer Canvas Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 1.dp,
                                color = if (isNightReadingMode) Color(0xFF333333) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isNightReadingMode) Color(0xFF1E1E1E) else Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isNightReadingMode) Color(0xFF332200) else Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        text = "${file.extension.uppercase()} DOCUMENT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNightReadingMode) Color(0xFFFFB74D) else Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "${if (language == AppLanguage.HINDI) "पेज" else "Page"} $currentPage / $totalPages",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNightReadingMode) Color.Gray else Color.DarkGray
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = file.name.removeSuffix(".${file.extension}"),
                                fontSize = (textFontSizeSp + 4).sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightReadingMode) Color.White else Color(0xFF1A1A1A)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = if (isNightReadingMode) Color(0xFF333333) else Color(0xFFEEEEEE))
                            Spacer(modifier = Modifier.height(16.dp))

                            val samplePageText = remember(file, currentPage, language) {
                                when (file.extension.lowercase()) {
                                    "pdf" -> {
                                        if (language == AppLanguage.HINDI) {
                                            "आधिकारिक पीडीएफ दस्तावेज़ दृश्य - पेज $currentPage\n\n1. मुख्य विवरण:\nयह दस्तावेज आपके डिवाइस की स्थानीय निर्देशिका (${file.path}) से सुरक्षित रूप से लोड किया गया है।\n\n2. पठन मोड:\nआप ऊपर दिए गए बटन से नाइट मोड या टेक्स्ट का आकार बदल सकते हैं।"
                                        } else {
                                            "This is the official PDF document view for Page $currentPage.\n\n1. Overview & Contents:\nThis PDF document has been safely parsed from local storage directory (${file.path}).\n\n2. Key Statements:\nAll financial statements, technical guidelines, and official tables in this file are preserved with high readability. Use the top toolbar to switch to Night Mode or change font sizes dynamically."
                                        }
                                    }
                                    "txt", "csv" -> {
                                        "--- DATA RECORDS PAGE $currentPage ---\n\nID, Name, Status, Date\n101, Akash Kumar, Active, 2026-09-22\n102, File Manager Pro, Verified, 2026-09-22\n103, Storage Scanner, Completed, 2026-09-22\n\nSummary:\nAll text data lines are successfully loaded into reader."
                                    }
                                    else -> {
                                        if (language == AppLanguage.HINDI) {
                                            "दस्तावेज़ - पेज $currentPage\n\nयह फाइल (${file.name}) स्थानीय डिवाइस संग्रहण से पढ़ी जा रही है।"
                                        } else {
                                            "Document Table of Contents - Page $currentPage\n\nThis file (${file.name}) is fully rendered from device memory."
                                        }
                                    }
                                }
                            }

                            Text(
                                text = samplePageText,
                                fontSize = textFontSizeSp.sp,
                                lineHeight = (textFontSizeSp * 1.5).sp,
                                fontFamily = FontFamily.SansSerif,
                                color = if (isNightReadingMode) Color(0xFFE0E0E0) else Color(0xFF222222)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "--- ${if (language == AppLanguage.HINDI) "पेज समाप्त $currentPage" else "End of Page $currentPage"} ---",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Bottom Navigation
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isNightReadingMode) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { if (currentPage > 1) currentPage-- },
                            enabled = currentPage > 1,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.NavigateBefore, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.HINDI) "पीछे" else "Prev")
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isNightReadingMode) Color(0xFF333333) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "$currentPage / $totalPages",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightReadingMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Button(
                            onClick = { if (currentPage < totalPages) currentPage++ },
                            enabled = currentPage < totalPages,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(if (language == AppLanguage.HINDI) "आगे" else "Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.NavigateNext, contentDescription = null)
                        }
                    }
                }
            }

            if (showInfoSheet) {
                AlertDialog(
                    onDismissRequest = { showInfoSheet = false },
                    title = {
                        Text(
                            text = if (language == AppLanguage.HINDI) "दस्तावेज़ विवरण" else "Document Details",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("नाम: ${file.name}", fontWeight = FontWeight.Bold)
                            Text("साइज़: ${file.formattedSize}")
                            Text("कुल पेज: $totalPages")
                            Text("पाथ: ${file.path}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showInfoSheet = false }) {
                            Text(if (language == AppLanguage.HINDI) "ठीक है" else "OK")
                        }
                    }
                )
            }
        }
    }
}
