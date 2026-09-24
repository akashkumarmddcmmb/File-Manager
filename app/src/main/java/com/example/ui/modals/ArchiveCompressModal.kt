package com.example.ui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*

@Composable
fun ArchiveCompressModal(
    filesToCompress: List<FileItem>,
    language: AppLanguage,
    onConfirm: (
        name: String,
        format: ArchiveFormat,
        level: CompressionLevel,
        method: CompressionMethod,
        password: String?,
        encryptHeader: Boolean,
        splitOption: SplitVolumeOption,
        deleteSource: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var archiveName by remember { mutableStateOf("Archive_Suite") }
    var selectedFormat by remember { mutableStateOf(ArchiveFormat.SEVEN_ZIP) }
    var selectedLevel by remember { mutableStateOf(CompressionLevel.NORMAL) }
    var selectedMethod by remember { mutableStateOf(CompressionMethod.LZMA2) }
    var selectedDictSize by remember { mutableStateOf(DictionarySize.SIZE_32MB) }
    var isSolidArchive by remember { mutableStateOf(true) }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var encryptHeader by remember { mutableStateOf(false) }
    var splitOption by remember { mutableStateOf(SplitVolumeOption.NONE) }
    var deleteSourceFiles by remember { mutableStateOf(false) }
    var testAfterCreation by remember { mutableStateOf(true) }
    var showLicenseReviewDialog by remember { mutableStateOf(false) }

    val totalInputBytes = remember(filesToCompress) { filesToCompress.sumOf { it.sizeBytes } }
    val totalSizeFormatted = remember(totalInputBytes) { formatFileSize(totalInputBytes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B3B2B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "7-Zip / ZIP कंप्रेशन" else "7-Zip & ZIP Compression",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "${filesToCompress.size} फाइलें चुनी गईं ($totalSizeFormatted)"
                            else
                                "${filesToCompress.size} files selected ($totalSizeFormatted)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Archive Name
                Text(
                    text = if (language == AppLanguage.HINDI) "आर्काइव का नाम" else "Archive Name",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = archiveName,
                    onValueChange = { archiveName = it },
                    singleLine = true,
                    suffix = { Text(".${selectedFormat.extension}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Format Selector (7z, ZIP, TAR, GZ, BZ2, XZ)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "आर्काइव प्रारूप (Format)" else "Archive Format",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (language == AppLanguage.HINDI) "लाइसेंस समीक्षा ℹ️" else "License Review ℹ️",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF00C853),
                        modifier = Modifier.clickable { showLicenseReviewDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        ArchiveFormat.SEVEN_ZIP to "7z",
                        ArchiveFormat.ZIP to "ZIP",
                        ArchiveFormat.TAR to "TAR"
                    ).forEach { (fmt, label) ->
                        val isSelected = selectedFormat == fmt
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedFormat = fmt
                                if (fmt == ArchiveFormat.SEVEN_ZIP) {
                                    selectedMethod = CompressionMethod.LZMA2
                                } else if (fmt == ArchiveFormat.ZIP) {
                                    selectedMethod = CompressionMethod.DEFLATE
                                }
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        ArchiveFormat.TAR_GZ to "GZIP",
                        ArchiveFormat.TAR_BZ2 to "BZIP2",
                        ArchiveFormat.XZ to "XZ"
                    ).forEach { (fmt, label) ->
                        val isSelected = selectedFormat == fmt
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedFormat = fmt
                                if (fmt == ArchiveFormat.XZ) {
                                    selectedMethod = CompressionMethod.LZMA2
                                } else if (fmt == ArchiveFormat.TAR_BZ2) {
                                    selectedMethod = CompressionMethod.BZIP2
                                } else {
                                    selectedMethod = CompressionMethod.DEFLATE
                                }
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Compression Level
                Text(
                    text = if (language == AppLanguage.HINDI) "कंप्रेशन स्तर (Compression Level)" else "Compression Level",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.HINDI) selectedLevel.descriptionHi else selectedLevel.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedLevel.levelName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }

                Slider(
                    value = selectedLevel.levelNumber.toFloat(),
                    onValueChange = { v ->
                        val num = v.toInt()
                        selectedLevel = when {
                            num == 0 -> CompressionLevel.STORE
                            num in 1..2 -> CompressionLevel.FASTEST
                            num in 3..4 -> CompressionLevel.FAST
                            num in 5..6 -> CompressionLevel.NORMAL
                            num in 7..8 -> CompressionLevel.MAXIMUM
                            else -> CompressionLevel.ULTRA
                        }
                    },
                    valueRange = 0f..9f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Compression Method (LZMA2 / Deflate / PPMd)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "कंप्रेशन मेथड:" else "Compression Method:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedMethod.displayName.substringBefore(" ("),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Password & AES Encryption
                Text(
                    text = if (language == AppLanguage.HINDI) "पासवर्ड सुरक्षा (AES-256)" else "Password Protection (AES-256)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    singleLine = true,
                    placeholder = { Text(if (language == AppLanguage.HINDI) "पासवर्ड दर्ज करें (वैकल्पिक)" else "Enter password (optional)") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 7-Zip Encrypt Header Option
                if (selectedFormat == ArchiveFormat.SEVEN_ZIP && passwordInput.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { encryptHeader = !encryptHeader },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = encryptHeader,
                            onCheckedChange = { encryptHeader = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "फ़ाइलों के नाम भी एन्क्रिप्ट करें (Encrypt file names)" else "Encrypt file names (7z Header)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Split Volumes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "वॉल्यूम में विभाजित करें:" else "Split to volumes:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = splitOption.displayName.substringBefore(" ("),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Checkbox: Delete source files after archiving
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { deleteSourceFiles = !deleteSourceFiles },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = deleteSourceFiles,
                        onCheckedChange = { deleteSourceFiles = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "कंप्रेशन के बाद मूल फ़ाइलें हटाएँ" else "Delete files after archiving",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val clean = archiveName.trim().ifEmpty { "Archive_Suite" }
                            onConfirm(
                                clean,
                                selectedFormat,
                                selectedLevel,
                                selectedMethod,
                                passwordInput.ifBlank { null },
                                encryptHeader,
                                splitOption,
                                deleteSourceFiles
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "कंप्रेस करें" else "Compress",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showLicenseReviewDialog) {
            AlertDialog(
                onDismissRequest = { showLicenseReviewDialog = false },
                icon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00C853)) },
                title = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "ओपन-सोर्स लाइसेंस समीक्षा" else "Open-Source Licensing Review",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "हमारे कंप्रेशन सुइट में केवल अत्यधिक सुरक्षित, पेटेंट-मुक्त और स्वतंत्र रूप से उपलब्ध ओपन-सोर्स प्रारूपों का ही उपयोग किया जाता है:"
                            else
                                "Our compression suite strictly implements secure, patent-free, and freely available open-source formats:",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )

                        val formats = listOf(
                            "ZIP" to (if (language == AppLanguage.HINDI) "सार्वजनिक मानक - सुरक्षित और व्यापक रूप से समर्थित।" else "Public Standard - Fully permissive, widely supported."),
                            "7-Zip (.7z)" to (if (language == AppLanguage.HINDI) "LGPL लाइसेंस (LZMA SDK) - अत्यधिक उच्च कंप्रेशन अनुपात।" else "LGPL Licensed (LZMA SDK) - Ultra high compression ratios."),
                            "TAR" to (if (language == AppLanguage.HINDI) "POSIX मानक - बिना कंप्रेशन वाला आर्काइव प्रारूप।" else "POSIX standard - Uncompressed archive format."),
                            "GZIP (.tar.gz)" to (if (language == AppLanguage.HINDI) "GNU स्टैंडर्ड - तेज गति और विश्वसनीय कंप्रेशन।" else "GNU standard - High speed reliable compression."),
                            "BZIP2 (.tar.bz2)" to (if (language == AppLanguage.HINDI) "BSD-लाइसेंस - पेटेंट-मुक्त उच्च-गुणवत्ता कंप्रेशन।" else "BSD-style Licensed - Patent-free high-quality compression."),
                            "XZ (.xz)" to (if (language == AppLanguage.HINDI) "पब्लिक डोमेन (LZMA2) - उत्कृष्ट संपीड़न।" else "Public Domain (LZMA2) - Excellent data compression.")
                        )

                        formats.forEach { (name, desc) ->
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, color = Color(0xFF00C853), fontSize = 14.sp)
                                Text(desc, fontSize = 12.sp, color = Color.White)
                            }
                        }

                        Divider(color = Color(0xFF37393F))

                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "⚠️ मालिकाना (Proprietary) प्रारूपों जैसे RAR को वाणिज्यिक लाइसेंस प्रतिबंधों और बौद्धिक संपदा समीक्षा के कारण बाहर रखा गया है।"
                            else
                                "⚠️ Proprietary formats like RAR are excluded to comply with commercial licensing restrictions and protect intellectual property rights.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD93025)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLicenseReviewDialog = false }) {
                        Text(if (language == AppLanguage.HINDI) "समझ गया" else "I Understand", color = Color(0xFF00C853))
                    }
                },
                containerColor = Color(0xFF1E1F23),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}
