package com.example.ui.modals

import android.os.Environment
import androidx.compose.foundation.background
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
    onConfirmDestination: (destinationPath: String) -> Unit,
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

    var selectedStorageType by remember { mutableStateOf("PHONE") } // "PHONE" or "SD_CARD"
    var currentPath by remember(selectedStorageType) {
        mutableStateOf(if (selectedStorageType == "PHONE") phoneRoot else sdCardRoot)
    }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderNameInput by remember { mutableStateOf("") }
    val customCreatedFolders = remember { mutableStateListOf<String>() }

    // Breadcrumbs Calculation
    val breadcrumbs = remember(currentPath, selectedStorageType, phoneRoot, sdCardRoot) {
        val root = if (selectedStorageType == "PHONE") phoneRoot else sdCardRoot
        val rootLabel = if (selectedStorageType == "PHONE") {
            if (language == AppLanguage.HINDI) "फोन मेमोरी" else "Phone Memory"
        } else {
            if (language == AppLanguage.HINDI) "एसडी कार्ड" else "SD Card"
        }

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
                            val count = try { f.listFiles()?.size ?: 0 } catch (e: Exception) { 0 }
                            list.add(
                                FolderDisplayItem(
                                    name = f.name,
                                    path = f.absolutePath,
                                    itemCount = count,
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
                listOf("Download", "Documents", "DCIM", "Pictures", "Music", "Movies", "WhatsApp", "Android").forEach { name ->
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
                listOf("SD_Backups", "Media", "Camera_SD", "Music_SD", "Documents_SD").forEach { name ->
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
                .padding(horizontal = 4.dp, vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (action == ClipboardOperationType.MOVE) Color(0xFF3E2723) else Color(0xFF1B3B2B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (action == ClipboardOperationType.MOVE) Icons.Default.DriveFileMove else Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (action == ClipboardOperationType.MOVE) Color(0xFFFFB74D) else Color(0xFF00C853),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (action == ClipboardOperationType.MOVE) {
                                    if (language == AppLanguage.HINDI) "स्थान चुनें (Move To)" else "Move to..."
                                } else {
                                    if (language == AppLanguage.HINDI) "स्थान चुनें (Copy To)" else "Copy to..."
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI)
                                    "${itemsToProcess.size} आइटम चुने गए"
                                else
                                    "${itemsToProcess.size} item(s) selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Storage Switcher Tabs: Phone Memory vs SD Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Phone Storage Button
                    FilterChip(
                        selected = selectedStorageType == "PHONE",
                        onClick = {
                            selectedStorageType = "PHONE"
                            currentPath = phoneRoot
                        },
                        label = {
                            Text(
                                if (language == AppLanguage.HINDI) "📱 फोन मेमोरी" else "📱 Phone Memory",
                                fontWeight = if (selectedStorageType == "PHONE") FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    // SD Card Storage Button
                    FilterChip(
                        selected = selectedStorageType == "SD_CARD",
                        onClick = {
                            selectedStorageType = "SD_CARD"
                            currentPath = sdCardRoot
                        },
                        label = {
                            Text(
                                if (language == AppLanguage.HINDI) "💾 एसडी कार्ड" else "💾 SD Card",
                                fontWeight = if (selectedStorageType == "SD_CARD") FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Breadcrumbs & Up Button
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
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Up directory",
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
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isLast) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier.clickable { currentPath = path }
                            ) {
                                Text(
                                    text = "$label >",
                                    fontSize = 12.sp,
                                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Create New Folder in current destination
                    IconButton(
                        onClick = { showCreateFolderDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New folder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subfolders list in current path
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    if (subfolders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "कोई सब-फ़ोल्डर नहीं है" else "No subfolders in this folder",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(subfolders, key = { it.path }) { folder ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { currentPath = folder.path }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Folder,
                                            contentDescription = null,
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = folder.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Enter",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Destination summary label
                Text(
                    text = if (language == AppLanguage.HINDI)
                        "गंतव्य: ${File(currentPath).name.ifEmpty { "Root" }}"
                    else
                        "Destination: ${File(currentPath).name.ifEmpty { "Root" }}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons: Cancel and Paste/Copy/Move Here
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
                            onConfirmDestination(currentPath)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (action == ClipboardOperationType.MOVE) Color(0xFFFF9800) else Color(0xFF00C853),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (action == ClipboardOperationType.MOVE) Icons.Default.DriveFileMove else Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (action == ClipboardOperationType.MOVE) {
                                if (language == AppLanguage.HINDI) "यहाँ ले जाएँ (Move Here)" else "Move Here"
                            } else {
                                if (language == AppLanguage.HINDI) "यहाँ कॉपी करें (Copy Here)" else "Copy Here"
                            },
                            fontWeight = FontWeight.Bold
                        )
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
                    text = if (language == AppLanguage.HINDI) "नया फ़ोल्डर बनाएँ" else "Create New Folder",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "फ़ोल्डर '${File(currentPath).name}' के अंदर नया फ़ोल्डर बनाएँ:"
                        else
                            "Create new folder inside '${File(currentPath).name}':",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newFolderNameInput,
                        onValueChange = { newFolderNameInput = it },
                        label = { Text(if (language == AppLanguage.HINDI) "फ़ोल्डर का नाम" else "Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
