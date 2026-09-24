package com.example.ui.modals

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import java.io.File

@Composable
fun FolderPickerModal(
    action: ClipboardOperationType,
    itemsToProcess: List<ClipboardItem>,
    storageDevices: List<StorageDeviceInfo>,
    language: AppLanguage,
    onConfirmDestination: (destinationPath: String, targetMbps: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val phoneRoot = remember {
        try {
            Environment.getExternalStorageDirectory().absolutePath
        } catch (e: Exception) {
            "/storage/emulated/0"
        }
    }

    val sdCardDevice = remember(storageDevices) {
        storageDevices.firstOrNull { it.isExternal }
    }

    val sdCardRoot = remember(sdCardDevice) {
        if (sdCardDevice != null && sdCardDevice.rootPath.isNotBlank() && File(sdCardDevice.rootPath).exists()) {
            sdCardDevice.rootPath
        } else {
            val storageDir = File("/storage")
            val found = try {
                storageDir.listFiles()?.firstOrNull {
                    it.isDirectory && !it.name.equals("emulated", true) && !it.name.equals("self", true)
                }?.absolutePath
            } catch (e: Exception) {
                null
            }
            found ?: (sdCardDevice?.rootPath?.ifBlank { "/storage/sdcard1" } ?: "/storage/sdcard1")
        }
    }

    val totalSizeBytes = remember(itemsToProcess) {
        itemsToProcess.sumOf { it.sizeBytes }
    }
    val totalSizeFormatted = remember(totalSizeBytes) {
        formatFileSize(totalSizeBytes)
    }

    val defaultSpeedForSize = remember(totalSizeBytes) {
        if (totalSizeBytes >= 500 * 1024 * 1024L) 120 else 35
    }

    var selectedStorageType by remember { mutableStateOf("PHONE") } // "PHONE" or "SD_CARD"
    var selectedSpeedMbps by remember(defaultSpeedForSize) { mutableStateOf(defaultSpeedForSize) } // 35 MB/s, 75 MB/s, 120 MB/s

    var currentPath by remember(selectedStorageType) {
        mutableStateOf(if (selectedStorageType == "PHONE") phoneRoot else sdCardRoot)
    }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderNameInput by remember { mutableStateOf("") }
    val customCreatedFolders = remember { mutableStateListOf<String>() }

    // Breadcrumbs Calculation
    val breadcrumbs = remember(currentPath, selectedStorageType, phoneRoot, sdCardRoot) {
        val root = if (selectedStorageType == "PHONE") phoneRoot else sdCardRoot
        val rootLabel = if (selectedStorageType == "PHONE") "Internal" else "SD Card"

        if (currentPath == root || !currentPath.startsWith(root)) {
            listOf(rootLabel to root)
        } else {
            val rel = currentPath.removePrefix(root).trim('/')
            val parts = rel.split('/')
            val list = mutableListOf(rootLabel to root)
            var accum = root
            for (p in parts) {
                if (p.isNotBlank()) {
                    accum = "$accum/$p"
                    list.add(p to accum)
                }
            }
            list
        }
    }

    // List of subfolders inside current directory
    val subfolders = remember(currentPath, customCreatedFolders.size) {
        val list = mutableListOf<FolderDisplayItem>()
        val dir = File(currentPath)
        var fsReadOk = false
        if (dir.exists() && dir.isDirectory) {
            try {
                val files = dir.listFiles()
                if (files != null) {
                    fsReadOk = true
                    for (f in files) {
                        if (f.isDirectory && !f.name.startsWith(".")) {
                            list.add(
                                FolderDisplayItem(
                                    name = f.name,
                                    path = f.absolutePath,
                                    itemCount = 0,
                                    dateModified = f.lastModified()
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                fsReadOk = false
            }
        }

        if (!fsReadOk || list.isEmpty()) {
            if (currentPath == phoneRoot) {
                listOf("Android", "Music", "Pictures", "Movies", "Download", "snaptube", "DCIM", "Documents").forEach { name ->
                    val p = "$phoneRoot/$name"
                    list.add(
                        FolderDisplayItem(
                            name = name,
                            path = p,
                            itemCount = 0,
                            dateModified = System.currentTimeMillis()
                        )
                    )
                }
            } else if (currentPath == sdCardRoot) {
                listOf("File-Explorer-Build-Outputs", "File-Manager-debug-APK", "Android", "Music", "DCIM", "Download").forEach { name ->
                    val p = "$sdCardRoot/$name"
                    list.add(
                        FolderDisplayItem(
                            name = name,
                            path = p,
                            itemCount = 0,
                            dateModified = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        customCreatedFolders.filter { it.startsWith(currentPath) && File(it).parent == currentPath }.forEach { cPath ->
            val cName = File(cPath).name
            if (list.none { it.path == cPath }) {
                list.add(
                    FolderDisplayItem(
                        name = cName,
                        path = cPath,
                        itemCount = 0,
                        dateModified = System.currentTimeMillis()
                    )
                )
            }
        }

        list.sortedBy { it.name.lowercase() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header (Matching Video: "कॉपी करें..." or "मूव करें...")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (action == ClipboardOperationType.MOVE) {
                                if (language == AppLanguage.HINDI) "मूव करें..." else "Move to..."
                            } else {
                                if (language == AppLanguage.HINDI) "कॉपी करें..." else "Copy to..."
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "${itemsToProcess.size} आइटम ($totalSizeFormatted)"
                            else
                                "${itemsToProcess.size} item(s) ($totalSizeFormatted)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (language == AppLanguage.HINDI) "गंतव्य स्टोरेज डिवाइस चुनें" else "Select Target Storage Device",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Side-by-side Storage Device Selectors (Video design)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Internal Storage Card
                    val isPhoneSelected = selectedStorageType == "PHONE"
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isPhoneSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isPhoneSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedStorageType = "PHONE"
                                currentPath = phoneRoot
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "आंतरिक स्टोरेज" else "Internal Storage",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Device Storage",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // SD Card Storage Card
                    val isSdSelected = selectedStorageType == "SD_CARD"
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSdSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSdSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedStorageType = "SD_CARD"
                                currentPath = sdCardRoot
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SdCard,
                                    contentDescription = null,
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "SD कार्ड (मेमो..." else "SD Card",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Removable SD",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Speed Preset Selector (Video frames 00:00 - 00:06)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "ट्रांसफर स्पीड:" else "Transfer Speed:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(35, 75, 120).forEach { speedVal ->
                            val isSelected = selectedSpeedMbps == speedVal
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedSpeedMbps = speedVal }
                            ) {
                                Text(
                                    text = if (isSelected) "⚡ $speedVal MB/s" else "$speedVal MB/s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Smart Recommendation Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (totalSizeBytes >= 500 * 1024 * 1024L) Color(0xFF1E3A2B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (totalSizeBytes >= 500 * 1024 * 1024L) Icons.Default.ElectricBolt else Icons.Default.Tune,
                            contentDescription = null,
                            tint = if (totalSizeBytes >= 500 * 1024 * 1024L) Color(0xFF00C853) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (totalSizeBytes >= 500 * 1024 * 1024L) {
                                if (language == AppLanguage.HINDI) "⚡ बड़ी फ़ाइलों/4K वीडियो ($totalSizeFormatted) के लिए 120 MB/s स्वतः सेट है" else "⚡ Auto-selected 120 MB/s for large files/4K videos ($totalSizeFormatted)"
                            } else {
                                if (language == AppLanguage.HINDI) "💡 सामान्य फ़ाइलों ($totalSizeFormatted) के लिए 35 MB/s संतुलित स्पीड सेट है" else "💡 Selected 35 MB/s balanced speed for normal files ($totalSizeFormatted)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (totalSizeBytes >= 500 * 1024 * 1024L) Color(0xFF75F9A7) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Directory Breadcrumb Path Bar & New Folder Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val root = if (selectedStorageType == "PHONE") phoneRoot else sdCardRoot
                    val canGoUp = currentPath != root && currentPath.startsWith(root)

                    if (canGoUp) {
                        IconButton(
                            onClick = {
                                val parent = File(currentPath).parent
                                if (parent != null && parent.startsWith(root)) {
                                    currentPath = parent
                                } else {
                                    currentPath = root
                                }
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(breadcrumbs) { (label, path) ->
                            val isLast = path == currentPath
                            Text(
                                text = if (isLast) label else "$label >",
                                fontSize = 13.sp,
                                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                                color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { currentPath = path }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    TextButton(
                        onClick = { showCreateFolderDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "नया फ़ोल्डर" else "New Folder",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Folder List Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    if (subfolders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "यह फ़ोल्डर खाली है" else "This folder is empty",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(subfolders, key = { it.path }) { folder ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { currentPath = folder.path }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Navigate",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Summary Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val root = if (selectedStorageType == "PHONE") phoneRoot else sdCardRoot
                    val displayTarget = if (currentPath == root) {
                        if (selectedStorageType == "PHONE") "Internal (Root)" else "SD Card (Root)"
                    } else {
                        if (selectedStorageType == "PHONE") "Internal > ${File(currentPath).name}" else "SD Card > ${File(currentPath).name}"
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "गंतव्य:" else "Destination:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = displayTarget,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                onConfirmDestination(currentPath, selectedSpeedMbps)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (action == ClipboardOperationType.MOVE) {
                                    if (language == AppLanguage.HINDI) "यहाँ ले जाएँ" else "Move Here"
                                } else {
                                    if (language == AppLanguage.HINDI) "यहाँ कॉपी करें" else "Copy Here"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateFolderDialog = false
                newFolderNameInput = ""
            },
            title = {
                Text(
                    text = if (language == AppLanguage.HINDI) "नया फ़ोल्डर बनाएँ" else "Create Folder",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newFolderNameInput,
                    onValueChange = { newFolderNameInput = it },
                    label = { Text(if (language == AppLanguage.HINDI) "फ़ोल्डर का नाम" else "Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFolderNameInput.trim()
                        if (name.isNotBlank()) {
                            val newPath = "$currentPath/$name"
                            try {
                                val dir = File(newPath)
                                dir.mkdirs()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            customCreatedFolders.add(newPath)
                            currentPath = newPath
                        }
                        showCreateFolderDialog = false
                        newFolderNameInput = ""
                    }
                ) {
                    Text(if (language == AppLanguage.HINDI) "बनाएँ" else "Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateFolderDialog = false
                        newFolderNameInput = ""
                    }
                ) {
                    Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}
