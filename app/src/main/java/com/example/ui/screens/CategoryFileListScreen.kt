package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.*
import com.example.pdf.PdfDocumentManager
import kotlinx.coroutines.launch
import java.io.File

data class FolderDisplayItem(
    val name: String,
    val path: String,
    val itemCount: Int = 0,
    val dateModified: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryFileListScreen(
    category: FileCategoryType?,
    storageDevice: StorageDeviceInfo?,
    files: List<FileItem>,
    language: AppLanguage,
    onBack: () -> Unit,
    onFileClick: (FileItem) -> Unit,
    onToggleStar: (String) -> Unit,
    onMoveToTrash: (String) -> Unit,
    onMoveToSafeFolder: (String) -> Unit,
    onCompressFiles: (List<FileItem>) -> Unit = {},
    onCopyItems: (List<ClipboardItem>) -> Unit = {},
    onMoveItems: (List<ClipboardItem>) -> Unit = {},
    isOverlayActive: Boolean = false
) {
    var isGridView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showMenuDropdown by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("name_asc") }
    var newFolderNameInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var folderActionTrigger by remember { mutableStateOf(0) }
    var showRenameFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<FolderDisplayItem?>(null) }
    var renameFolderInput by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var folderToDelete by remember { mutableStateOf<FolderDisplayItem?>(null) }

    // Multi-Selection State (मल्टी-सेलेक्शन मोड)
    val selectedFolderPaths = remember { mutableStateListOf<String>() }
    val selectedFileIds = remember { mutableStateListOf<String>() }
    val isSelectionMode by remember { derivedStateOf { selectedFolderPaths.isNotEmpty() || selectedFileIds.isNotEmpty() } }
    val totalSelectedCount by remember { derivedStateOf { selectedFolderPaths.size + selectedFileIds.size } }

    val toggleFolderSelection: (String) -> Unit = { path ->
        if (selectedFolderPaths.contains(path)) {
            selectedFolderPaths.remove(path)
        } else {
            selectedFolderPaths.add(path)
        }
    }

    val toggleFileSelection: (String) -> Unit = { id ->
        if (selectedFileIds.contains(id)) {
            selectedFileIds.remove(id)
        } else {
            selectedFileIds.add(id)
        }
    }

    val clearSelection: () -> Unit = {
        selectedFolderPaths.clear()
        selectedFileIds.clear()
    }

    var isFullStorageGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
        )
    }

    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isFullStorageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    val rootPath = remember(storageDevice) {
        if (storageDevice != null) {
            when {
                storageDevice.rootPath.isNotBlank() && File(storageDevice.rootPath).exists() -> storageDevice.rootPath
                storageDevice.isExternal -> {
                    val storageDir = File("/storage")
                    val sdCard = try {
                        storageDir.listFiles()?.firstOrNull { 
                            it.isDirectory && !it.name.equals("self", true) && !it.name.equals("emulated", true)
                        }
                    } catch (e: Exception) { null }
                    
                    val foundPath = sdCard?.absolutePath
                    when {
                        foundPath != null && File(foundPath).exists() -> foundPath
                        File("/sdcard").exists() -> "/sdcard"
                        File("/mnt/sdcard").exists() -> "/mnt/sdcard"
                        else -> storageDevice.rootPath.ifBlank { "/storage/sdcard" }
                    }
                }
                else -> {
                    try {
                        Environment.getExternalStorageDirectory().absolutePath
                    } catch (e: Exception) {
                        "/storage/emulated/0"
                    }
                }
            }
        } else {
            ""
        }
    }

    var currentFolderPath by remember(storageDevice) { mutableStateOf(rootPath) }

    val performScreenBack: () -> Unit = {
        when {
            isSelectionMode -> {
                clearSelection()
            }
            showMenuDropdown -> {
                showMenuDropdown = false
            }
            showCreateFolderDialog -> {
                showCreateFolderDialog = false
            }
            searchQuery.isNotBlank() -> {
                searchQuery = ""
            }
            storageDevice != null && currentFolderPath.isNotBlank() && currentFolderPath != rootPath -> {
                val parent = File(currentFolderPath).parent
                if (parent != null && parent.startsWith(rootPath)) {
                    currentFolderPath = parent
                } else {
                    currentFolderPath = rootPath
                }
            }
            else -> {
                onBack()
            }
        }
    }

    BackHandler(enabled = !isOverlayActive) {
        performScreenBack()
    }

    val customFolders = remember { mutableStateListOf<FolderDisplayItem>() }

    val breadcrumbs = remember(currentFolderPath, rootPath, storageDevice, language) {
        if (storageDevice == null || currentFolderPath.isBlank()) return@remember emptyList<Pair<String, String>>()
        val deviceLabel = if (language == AppLanguage.HINDI) storageDevice.nameHi else storageDevice.nameEn
        
        if (currentFolderPath == rootPath) {
            listOf(deviceLabel to rootPath)
        } else {
            val relative = currentFolderPath.removePrefix(rootPath).trim('/')
            val parts = relative.split('/')
            val list = mutableListOf(deviceLabel to rootPath)
            var accum = rootPath
            parts.forEach { part ->
                if (part.isNotBlank()) {
                    accum = "$accum/$part"
                    list.add(part to accum)
                }
            }
            list
        }
    }

    val screenTitle = remember(category, storageDevice, language, currentFolderPath) {
        when {
            category != null -> if (language == AppLanguage.HINDI) category.titleHi else category.titleEn
            storageDevice != null -> {
                if (currentFolderPath == rootPath) {
                    if (language == AppLanguage.HINDI) storageDevice.nameHi else storageDevice.nameEn
                } else {
                    File(currentFolderPath).name
                }
            }
            else -> if (language == AppLanguage.HINDI) "फाइलें" else "Files"
        }
    }

    val (displayedFolders, displayedFiles) = remember(
        category, storageDevice, currentFolderPath, files, customFolders.size, searchQuery, folderActionTrigger
    ) {
        if (category != null) {
            val matchingFiles = files.filter { file ->
                !file.isInSafeFolder && !file.isInTrash &&
                        file.category == category &&
                        (searchQuery.isBlank() || file.name.contains(searchQuery, ignoreCase = true))
            }
            emptyList<FolderDisplayItem>() to matchingFiles
        } else if (storageDevice != null) {
            val foldersList = mutableListOf<FolderDisplayItem>()
            val filesList = mutableListOf<FileItem>()
            var readFromFsSuccess = false
            try {
                val dir = File(currentFolderPath)
                if (dir.exists() && dir.isDirectory) {
                    val childFiles = dir.listFiles()
                    if (childFiles != null) {
                        readFromFsSuccess = true
                        for (child in childFiles) {
                            val name = child.name
                            if (name.startsWith(".")) continue
                            
                            if (searchQuery.isNotBlank() && !name.contains(searchQuery, ignoreCase = true)) {
                                continue
                            }
                            if (child.isDirectory) {
                                foldersList.add(
                                    FolderDisplayItem(
                                        name = name,
                                        path = child.absolutePath,
                                        itemCount = 0,
                                        dateModified = child.lastModified()
                                    )
                                )
                            } else if (child.isFile) {
                                val ext = child.extension.lowercase()
                                val cat = when {
                                    ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic") -> FileCategoryType.IMAGES
                                    ext in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp") -> FileCategoryType.VIDEOS
                                    ext in listOf("mp3", "flac", "wav", "m4a", "ogg", "aac", "wma", "amr") -> FileCategoryType.AUDIO
                                    ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "txt", "ppt", "pptx", "vcf", "vcard", "csv") -> FileCategoryType.DOCUMENTS
                                    ext in listOf("apk", "xapk") -> FileCategoryType.APPS
                                    ext in listOf("zip", "7z", "tar", "gz", "bz2", "xz") -> FileCategoryType.ARCHIVES
                                    else -> FileCategoryType.DOWNLOADS
                                }
                                filesList.add(
                                    FileItem(
                                        id = "fs_${child.absolutePath.hashCode()}",
                                        name = name,
                                        path = child.absolutePath,
                                        sizeBytes = child.length(),
                                        category = cat,
                                        extension = ext,
                                        dateModified = child.lastModified()
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                readFromFsSuccess = false
            }

            if (!readFromFsSuccess) {
                val activeFiles = files.filter { !it.isInSafeFolder && !it.isInTrash }
                if (currentFolderPath == rootPath) {
                    val folderSpecs = listOf(
                        "Music" to activeFiles.filter { it.category == FileCategoryType.AUDIO },
                        "Videos" to activeFiles.filter { it.category == FileCategoryType.VIDEOS },
                        "DCIM" to activeFiles.filter { it.category == FileCategoryType.IMAGES },
                        "Pictures" to activeFiles.filter { it.category == FileCategoryType.IMAGES },
                        "Documents" to activeFiles.filter { it.category == FileCategoryType.DOCUMENTS },
                        "Download" to activeFiles.filter { it.category == FileCategoryType.DOWNLOADS },
                        "WhatsApp" to activeFiles.take(5),
                        "Android" to emptyList()
                    )
                    folderSpecs.forEach { (fName, fFiles) ->
                        if (searchQuery.isBlank() || fName.contains(searchQuery, ignoreCase = true)) {
                            if (foldersList.none { it.name.equals(fName, ignoreCase = true) }) {
                                foldersList.add(
                                    FolderDisplayItem(
                                        name = fName,
                                        path = "$rootPath/$fName",
                                        itemCount = fFiles.size,
                                        dateModified = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                    activeFiles.filter { searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) }.forEach { f ->
                        if (filesList.none { it.id == f.id }) {
                            filesList.add(f)
                        }
                    }
                } else {
                    val folderName = File(currentFolderPath).name.lowercase()
                    val targetCategory = when {
                        folderName.contains("music") || folderName.contains("song") || folderName.contains("audio") -> FileCategoryType.AUDIO
                        folderName.contains("video") || folderName.contains("movie") -> FileCategoryType.VIDEOS
                        folderName.contains("dcim") || folderName.contains("picture") || folderName.contains("photo") || folderName.contains("camera") -> FileCategoryType.IMAGES
                        folderName.contains("doc") || folderName.contains("pdf") -> FileCategoryType.DOCUMENTS
                        folderName.contains("download") -> FileCategoryType.DOWNLOADS
                        else -> null
                    }
                    val matchedFiles = activeFiles.filter { file ->
                        val matchesPath = file.path.startsWith(currentFolderPath, ignoreCase = true)
                        val matchesCategory = targetCategory != null && file.category == targetCategory
                        (matchesPath || matchesCategory) && (searchQuery.isBlank() || file.name.contains(searchQuery, ignoreCase = true))
                    }
                    matchedFiles.forEach { f ->
                        if (filesList.none { it.id == f.id }) {
                            filesList.add(f)
                        }
                    }
                }
            }

            customFolders.filter { it.path.startsWith(currentFolderPath) && File(it.path).parent == currentFolderPath }.forEach { cFolder ->
                if (foldersList.none { it.path == cFolder.path }) {
                    if (searchQuery.isBlank() || cFolder.name.contains(searchQuery, ignoreCase = true)) {
                        foldersList.add(cFolder)
                    }
                }
            }

            when (sortOption) {
                "name_asc" -> {
                    foldersList.sortBy { it.name.lowercase() }
                    filesList.sortBy { it.name.lowercase() }
                }
                "name_desc" -> {
                    foldersList.sortByDescending { it.name.lowercase() }
                    filesList.sortByDescending { it.name.lowercase() }
                }
                "date_desc" -> {
                    foldersList.sortByDescending { it.dateModified }
                    filesList.sortByDescending { it.dateModified }
                }
                "size_desc" -> {
                    foldersList.sortBy { it.name.lowercase() }
                    filesList.sortByDescending { it.sizeBytes }
                }
                else -> {
                    foldersList.sortBy { it.name.lowercase() }
                    filesList.sortBy { it.name.lowercase() }
                }
            }
            foldersList to filesList
        } else {
            emptyList<FolderDisplayItem>() to emptyList<FileItem>()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (storageDevice != null) {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CreateNewFolder, contentDescription = "New Folder")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "नया फ़ोल्डर" else "New Folder",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (displayedFiles.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { onCompressFiles(displayedFiles.take(4)) },
                    icon = { Icon(Icons.Default.FolderZip, contentDescription = null, tint = Color.White) },
                    text = {
                        Text(
                            text = if (language == AppLanguage.HINDI) ".ZIP आर्काइव बनाएं" else "Create .ZIP Archive",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color(0xFF00C853),
                    contentColor = Color.White
                )
            }
        },
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = if (isSelectionMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column {
                    if (isSelectionMode) {
                        // Multi-Selection Contextual Action Bar (मल्टी-सेलेक्शन बार)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { clearSelection() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Selection",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Text(
                                text = "$totalSelectedCount " + if (language == AppLanguage.HINDI) "चुने गए" else "selected",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )

                            // Select All / Deselect All
                            val totalItems = displayedFolders.size + displayedFiles.size
                            val isAllSelected = totalSelectedCount == totalItems && totalItems > 0
                            IconButton(onClick = {
                                if (isAllSelected) {
                                    clearSelection()
                                } else {
                                    selectedFolderPaths.clear()
                                    selectedFolderPaths.addAll(displayedFolders.map { it.path })
                                    selectedFileIds.clear()
                                    selectedFileIds.addAll(displayedFiles.map { it.id })
                                }
                            }) {
                                Icon(
                                    imageVector = if (isAllSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                                    contentDescription = if (isAllSelected) "Deselect All" else "Select All",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Delete Selected Items
                            IconButton(onClick = {
                                selectedFileIds.forEach { onMoveToTrash(it) }
                                val count = totalSelectedCount
                                clearSelection()
                                val msg = if (language == AppLanguage.HINDI) "$count आइटम ट्रैश में भेजे गए" else "$count items moved to trash"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Selected",
                                    tint = Color(0xFFD93025)
                                )
                            }

                            // Selection overflow options (3-Dot Menu)
                            var showSelectionMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showSelectionMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Selection Options",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSelectionMenu,
                                    onDismissRequest = { showSelectionMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (language == AppLanguage.HINDI) "यहाँ कॉपी करें (Copy to...)" else "Copy to...") },
                                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF00C853)) },
                                        onClick = {
                                            showSelectionMenu = false
                                            val itemsToCopy = mutableListOf<ClipboardItem>()
                                            selectedFolderPaths.forEach { path ->
                                                itemsToCopy.add(ClipboardItem(path = path, name = File(path).name, isFolder = true))
                                            }
                                            selectedFileIds.forEach { id ->
                                                displayedFiles.firstOrNull { it.id == id }?.let { f ->
                                                    itemsToCopy.add(ClipboardItem(path = f.path, name = f.name, isFolder = false, sizeBytes = f.sizeBytes, fileItem = f))
                                                }
                                            }
                                            if (itemsToCopy.isNotEmpty()) {
                                                onCopyItems(itemsToCopy)
                                            }
                                            clearSelection()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (language == AppLanguage.HINDI) "यहाँ ले जाएँ (Move to...)" else "Move to...") },
                                        leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = Color(0xFFFF9800)) },
                                        onClick = {
                                            showSelectionMenu = false
                                            val itemsToMove = mutableListOf<ClipboardItem>()
                                            selectedFolderPaths.forEach { path ->
                                                itemsToMove.add(ClipboardItem(path = path, name = File(path).name, isFolder = true))
                                            }
                                            selectedFileIds.forEach { id ->
                                                displayedFiles.firstOrNull { it.id == id }?.let { f ->
                                                    itemsToMove.add(ClipboardItem(path = f.path, name = f.name, isFolder = false, sizeBytes = f.sizeBytes, fileItem = f))
                                                }
                                            }
                                            if (itemsToMove.isNotEmpty()) {
                                                onMoveItems(itemsToMove)
                                            }
                                            clearSelection()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Zip Archive", fontWeight = FontWeight.SemiBold) },
                                        leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null, tint = Color(0xFF00C853)) },
                                        onClick = {
                                            showSelectionMenu = false
                                            val selectedFilesList = displayedFiles.filter { selectedFileIds.contains(it.id) }
                                            if (selectedFilesList.isNotEmpty()) {
                                                onCompressFiles(selectedFilesList)
                                            } else if (displayedFiles.isNotEmpty()) {
                                                onCompressFiles(displayedFiles)
                                            } else {
                                                Toast.makeText(context, if (language == AppLanguage.HINDI) "Zip Archive के लिए फ़ाइलें चुनें" else "Select files for Zip Archive", Toast.LENGTH_SHORT).show()
                                            }
                                            clearSelection()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (language == AppLanguage.HINDI) "सुरक्षित फ़ोल्डर में भेजें" else "Move to Safe folder") },
                                        leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00C853)) },
                                        onClick = {
                                            showSelectionMenu = false
                                            selectedFileIds.forEach { onMoveToSafeFolder(it) }
                                            clearSelection()
                                            Toast.makeText(context, if (language == AppLanguage.HINDI) "सुरक्षित फ़ोल्डर में स्थानांतरित" else "Moved to Safe folder", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (language == AppLanguage.HINDI) "तारांकित में जोड़ें" else "Add to Starred") },
                                        leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF9AB00)) },
                                        onClick = {
                                            showSelectionMenu = false
                                            selectedFileIds.forEach { onToggleStar(it) }
                                            clearSelection()
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard Top Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = performScreenBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = screenTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val subtitle = if (language == AppLanguage.HINDI) {
                                    "${displayedFolders.size} फ़ोल्डर • ${displayedFiles.size} फाइलें"
                                } else {
                                    "${displayedFolders.size} folders • ${displayedFiles.size} files"
                                }
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Removed duplicate top bar folder icon (kept in 3-dot menu & bottom FAB)

                            IconButton(onClick = { isGridView = !isGridView }) {
                                Icon(
                                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                    contentDescription = "Toggle View",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // 3-Dot Corner Overflow Menu (तीन बिंदु मेनू)
                            Box {
                                IconButton(onClick = { showMenuDropdown = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenuDropdown,
                                    onDismissRequest = { showMenuDropdown = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    // ZIP Compress Option (Zip Archive)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Zip Archive",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.FolderZip,
                                                contentDescription = "Zip Archive",
                                                tint = Color(0xFF00C853)
                                            )
                                        },
                                        onClick = {
                                            showMenuDropdown = false
                                            if (displayedFiles.isNotEmpty()) {
                                                onCompressFiles(displayedFiles)
                                            } else {
                                                val noFilesMsg = if (language == AppLanguage.HINDI) {
                                                    "Zip Archive बनाने के लिए इस फ़ोल्डर में कोई फ़ाइल नहीं है"
                                                } else {
                                                    "No files for Zip Archive in this folder"
                                                }
                                                Toast.makeText(context, noFilesMsg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(if (language == AppLanguage.HINDI) "नया फ़ोल्डर बनाएं" else "New folder")
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.CreateNewFolder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            showMenuDropdown = false
                                            showCreateFolderDialog = true
                                        }
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    // Sort Options
                                    DropdownMenuItem(
                                        text = {
                                            Text(if (language == AppLanguage.HINDI) "नाम अनुसार (A to Z)" else "Sort by name (A to Z)")
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.SortByAlpha, contentDescription = null)
                                        },
                                        trailingIcon = if (sortOption == "name_asc") {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        } else null,
                                        onClick = {
                                            sortOption = "name_asc"
                                            showMenuDropdown = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(if (language == AppLanguage.HINDI) "तारीख अनुसार (नई पहले)" else "Sort by date (Newest first)")
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Schedule, contentDescription = null)
                                        },
                                        trailingIcon = if (sortOption == "date_desc") {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        } else null,
                                        onClick = {
                                            sortOption = "date_desc"
                                            showMenuDropdown = false
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Text(if (language == AppLanguage.HINDI) "साइज अनुसार (बड़ी पहले)" else "Sort by size (Largest first)")
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.TrendingDown, contentDescription = null)
                                        },
                                        trailingIcon = if (sortOption == "size_desc") {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                        } else null,
                                        onClick = {
                                            sortOption = "size_desc"
                                            showMenuDropdown = false
                                        }
                                    )

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (isGridView) {
                                                    if (language == AppLanguage.HINDI) "सूची दृश्य (List View)" else "Switch to List View"
                                                } else {
                                                    if (language == AppLanguage.HINDI) "ग्रिड दृश्य (Grid View)" else "Switch to Grid View"
                                                }
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            isGridView = !isGridView
                                            showMenuDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (storageDevice != null && breadcrumbs.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(breadcrumbs) { (label, path) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (path == currentFolderPath) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        modifier = Modifier.clickable { currentFolderPath = path }
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (path == currentFolderPath) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            ),
                                            color = if (path == currentFolderPath) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    if (path != breadcrumbs.last().second) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (storageDevice != null && !isFullStorageGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "SD कार्ड व स्टोरेज अनुमति आवश्यक" else "Full Storage Access Required",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI) "सभी फ़ोल्डर्स को मैनेज करने के लिए अनुमति दें" else "Grant permission to browse and manage raw folders",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    try {
                                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        manageStorageLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        isFullStorageGranted = true
                                    }
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(if (language == AppLanguage.HINDI) "अनुमति दें" else "Allow", fontSize = 12.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "फ़ोल्डर और फ़ाइलों में खोजें..." else "Search folders and files...",
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )

            if (displayedFolders.isEmpty() && displayedFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "यह फ़ोल्डर खाली है" else "This folder is empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedFolders, key = { "folder_${it.path}" }) { folder ->
                        val isFolderSelected = selectedFolderPaths.contains(folder.path)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onLongClick = {
                                        toggleFolderSelection(folder.path)
                                    },
                                    onClick = {
                                        if (isSelectionMode) {
                                            toggleFolderSelection(folder.path)
                                        } else {
                                            currentFolderPath = folder.path
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isFolderSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else Color(0xFF2C2416),
                            tonalElevation = 2.dp,
                            border = if (isFolderSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color(0xFF5A4321))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Folder,
                                        contentDescription = "Folder",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${folder.itemCount} items",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = Color(0xFFFFD54F),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelectionMode) {
                                        Icon(
                                            imageVector = if (isFolderSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = if (isFolderSelected) "Selected" else "Not selected",
                                            tint = if (isFolderSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    } else {
                                        var showFolderMenu by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(
                                                onClick = { showFolderMenu = true },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Folder Options",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showFolderMenu,
                                                onDismissRequest = { showFolderMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text(if (language == AppLanguage.HINDI) "खोलें (Open)" else "Open Folder") },
                                                    leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFFFFC107)) },
                                                    onClick = {
                                                        showFolderMenu = false
                                                        currentFolderPath = folder.path
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(if (language == AppLanguage.HINDI) "नाम बदलें" else "Rename") },
                                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00C853)) },
                                                    onClick = {
                                                        showFolderMenu = false
                                                        folderToRename = folder
                                                        renameFolderInput = folder.name
                                                        showRenameFolderDialog = true
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(if (language == AppLanguage.HINDI) "हटाएं (Delete)" else "Delete Folder") },
                                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD93025)) },
                                                    onClick = {
                                                        showFolderMenu = false
                                                        folderToDelete = folder
                                                        showDeleteConfirmDialog = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = folder.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    items(displayedFiles, key = { it.id }) { file ->
                        val isFileSelected = selectedFileIds.contains(file.id)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onLongClick = {
                                        toggleFileSelection(file.id)
                                    },
                                    onClick = {
                                        if (isSelectionMode) {
                                            toggleFileSelection(file.id)
                                        } else {
                                            onFileClick(file)
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isFileSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp,
                            border = if (isFileSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF14171C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val ext = file.extension.lowercase()
                                    val isImage = file.category == FileCategoryType.IMAGES || ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
                                    val isVideo = file.category == FileCategoryType.VIDEOS || ext in listOf("mp4", "mkv", "avi", "mov", "webm")
                                    val localFile = remember(file.path) { File(file.path) }
                                    val imageModel = if (localFile.exists() && localFile.canRead() && localFile.length() > 0) {
                                        localFile
                                    } else {
                                        when {
                                            isImage -> "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=400&q=80"
                                            isVideo -> "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&q=80"
                                            else -> null
                                        }
                                    }
                                    if (imageModel != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(imageModel)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = file.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        if (isVideo) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.6f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = when (file.category) {
                                                FileCategoryType.VIDEOS -> Icons.Default.Videocam
                                                FileCategoryType.IMAGES -> Icons.Default.Image
                                                FileCategoryType.AUDIO -> Icons.Default.Audiotrack
                                                else -> Icons.Default.InsertDriveFile
                                            },
                                            contentDescription = null,
                                            tint = file.category.iconColor,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    // Selection Indicator Overlay in Grid
                                    if (isSelectionMode) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp)
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(if (isFileSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isFileSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                                contentDescription = if (isFileSelected) "Selected" else "Unselected",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = file.formattedSize,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (!isSelectionMode) {
                                        var showMenu by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(
                                                onClick = { showMenu = true },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "More Options",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            if (file.isStarred) {
                                                                if (language == AppLanguage.HINDI) "तारांकित से हटाएं" else "Remove Star"
                                                            } else {
                                                                if (language == AppLanguage.HINDI) "तारांकित में जोड़ें" else "Add to Starred"
                                                            }
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = if (file.isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                            contentDescription = null,
                                                            tint = if (file.isStarred) Color(0xFFF9AB00) else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    },
                                                    onClick = {
                                                        showMenu = false
                                                        onToggleStar(file.id)
                                                    }
                                                )
                                                if (file.category == FileCategoryType.DOCUMENTS || file.extension.lowercase() in listOf("pdf", "doc", "docx", "txt")) {
                                                    DropdownMenuItem(
                                                        text = { Text(if (language == AppLanguage.HINDI) "प्रिंट करें" else "Print") },
                                                        leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF0288D1)) },
                                                        onClick = {
                                                            showMenu = false
                                                            scope.launch {
                                                                try {
                                                                    val pdf = PdfDocumentManager.getOrCreatePdfFile(context, file)
                                                                    val ok = PdfDocumentManager.printPdfDocument(context, pdf, file.name)
                                                                    if (!ok) {
                                                                        Toast.makeText(context, if (language == AppLanguage.HINDI) "प्रिंटिंग शुरू नहीं हो सकी" else "Unable to start printing", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                } catch (e: Exception) {
                                                                    Toast.makeText(context, "Print error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                                DropdownMenuItem(
                                                    text = { Text(if (language == AppLanguage.HINDI) "सुरक्षित फ़ोल्डर में भेजें" else "Move to Safe folder") },
                                                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00C853)) },
                                                    onClick = {
                                                        showMenu = false
                                                        onMoveToSafeFolder(file.id)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text(if (language == AppLanguage.HINDI) "ट्रैश में भेजें" else "Move to Trash") },
                                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD93025)) },
                                                    onClick = {
                                                        showMenu = false
                                                        onMoveToTrash(file.id)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedFolders, key = { "folder_${it.path}" }) { folder ->
                        val isFolderSelected = selectedFolderPaths.contains(folder.path)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onLongClick = {
                                        toggleFolderSelection(folder.path)
                                    },
                                    onClick = {
                                        if (isSelectionMode) {
                                            toggleFolderSelection(folder.path)
                                        } else {
                                            currentFolderPath = folder.path
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isFolderSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp,
                            border = if (isFolderSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isFolderSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color(0xFF3B2F17)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Folder,
                                        contentDescription = "Folder",
                                        tint = if (isFolderSelected) MaterialTheme.colorScheme.primary else Color(0xFFFFC107),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (language == AppLanguage.HINDI) "${folder.itemCount} आइटम्स" else "${folder.itemCount} items",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelectionMode) {
                                    Icon(
                                        imageVector = if (isFolderSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = if (isFolderSelected) "Selected" else "Unselected",
                                        tint = if (isFolderSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    var showFolderMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { showFolderMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Folder Options",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showFolderMenu,
                                            onDismissRequest = { showFolderMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(if (language == AppLanguage.HINDI) "खोलें (Open)" else "Open Folder") },
                                                leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFFFFC107)) },
                                                onClick = {
                                                    showFolderMenu = false
                                                    currentFolderPath = folder.path
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(if (language == AppLanguage.HINDI) "नाम बदलें" else "Rename") },
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00C853)) },
                                                onClick = {
                                                    showFolderMenu = false
                                                    folderToRename = folder
                                                    renameFolderInput = folder.name
                                                    showRenameFolderDialog = true
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(if (language == AppLanguage.HINDI) "हटाएं (Delete)" else "Delete Folder") },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD93025)) },
                                                onClick = {
                                                    showFolderMenu = false
                                                    folderToDelete = folder
                                                    showDeleteConfirmDialog = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    items(displayedFiles, key = { it.id }) { file ->
                        var showMenu by remember { mutableStateOf(false) }
                        val isFileSelected = selectedFileIds.contains(file.id)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onLongClick = {
                                        toggleFileSelection(file.id)
                                    },
                                    onClick = {
                                        if (isSelectionMode) {
                                            toggleFileSelection(file.id)
                                        } else {
                                            onFileClick(file)
                                        }
                                    }
                                ),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isFileSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp,
                            border = if (isFileSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(file.category.iconBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val ext = file.extension.lowercase()
                                    val isImage = file.category == FileCategoryType.IMAGES || ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
                                    val isVideo = file.category == FileCategoryType.VIDEOS || ext in listOf("mp4", "mkv", "avi", "mov", "webm")
                                    val localFile = remember(file.path) { File(file.path) }
                                    val imageModel = if (localFile.exists() && localFile.canRead() && localFile.length() > 0) {
                                        localFile
                                    } else {
                                        when {
                                            isImage -> "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=200&q=80"
                                            isVideo -> "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=200&q=80"
                                            else -> null
                                        }
                                    }
                                    if (imageModel != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(imageModel)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = file.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        if (isVideo) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.35f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = when (file.category) {
                                                FileCategoryType.VIDEOS -> Icons.Default.Videocam
                                                FileCategoryType.IMAGES -> Icons.Default.Image
                                                FileCategoryType.AUDIO -> Icons.Default.Audiotrack
                                                else -> Icons.Default.InsertDriveFile
                                            },
                                            contentDescription = null,
                                            tint = file.category.iconColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${file.formattedSize} • ${file.extension.uppercase()}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelectionMode) {
                                    Icon(
                                        imageVector = if (isFileSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = if (isFileSelected) "Selected" else "Unselected",
                                        tint = if (isFileSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    if (file.isStarred) {
                                        IconButton(onClick = { onToggleStar(file.id) }) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Starred",
                                                tint = Color(0xFFF9AB00)
                                            )
                                        }
                                    }

                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "More Options",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        if (file.isStarred) {
                                                            if (language == AppLanguage.HINDI) "तारांकित से हटाएं" else "Remove Star"
                                                        } else {
                                                            if (language == AppLanguage.HINDI) "तारांकित में जोड़ें" else "Add to Starred"
                                                        }
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = if (file.isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                        contentDescription = null,
                                                        tint = if (file.isStarred) Color(0xFFF9AB00) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    onToggleStar(file.id)
                                                }
                                            )
                                            if (file.category == FileCategoryType.DOCUMENTS || file.extension.lowercase() in listOf("pdf", "doc", "docx", "txt")) {
                                                DropdownMenuItem(
                                                    text = { Text(if (language == AppLanguage.HINDI) "प्रिंट करें" else "Print") },
                                                    leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF0288D1)) },
                                                    onClick = {
                                                        showMenu = false
                                                        scope.launch {
                                                            try {
                                                                val pdf = PdfDocumentManager.getOrCreatePdfFile(context, file)
                                                                val ok = PdfDocumentManager.printPdfDocument(context, pdf, file.name)
                                                                if (!ok) {
                                                                    Toast.makeText(context, if (language == AppLanguage.HINDI) "प्रिंटिंग शुरू नहीं हो सकी" else "Unable to start printing", Toast.LENGTH_SHORT).show()
                                                                }
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Print error: ${e.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = { Text(if (language == AppLanguage.HINDI) "सुरक्षित फ़ोल्डर में भेजें" else "Move to Safe folder") },
                                                leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00C853)) },
                                                onClick = {
                                                    showMenu = false
                                                    onMoveToSafeFolder(file.id)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(if (language == AppLanguage.HINDI) "ट्रैश में भेजें" else "Move to Trash") },
                                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD93025)) },
                                                onClick = {
                                                    showMenu = false
                                                    onMoveToTrash(file.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
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
                    text = if (language == AppLanguage.HINDI) "नया फ़ोल्डर बनाएं" else "Create New Folder",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (language == AppLanguage.HINDI) "वर्तमान फ़ोल्डर में नया फ़ोल्डर बनाएं:" else "Create a new folder in current directory:",
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
                            val newPath = "$currentFolderPath/$name"
                            try {
                                val dir = File(newPath)
                                dir.mkdirs()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            customFolders.add(
                                FolderDisplayItem(
                                    name = name,
                                    path = newPath,
                                    itemCount = 0
                                )
                            )
                        }
                        showCreateFolderDialog = false
                        newFolderNameInput = ""
                    }
                ) {
                    Text(if (language == AppLanguage.HINDI) "बनाएं" else "Create")
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

    if (showRenameFolderDialog && folderToRename != null) {
        AlertDialog(
            onDismissRequest = {
                showRenameFolderDialog = false
                folderToRename = null
                renameFolderInput = ""
            },
            title = {
                Text(
                    text = if (language == AppLanguage.HINDI) "फ़ोल्डर का नाम बदलें" else "Rename Folder",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = renameFolderInput,
                        onValueChange = { renameFolderInput = it },
                        label = { Text(if (language == AppLanguage.HINDI) "नया नाम" else "New Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = renameFolderInput.trim()
                        val currentFolder = folderToRename
                        if (newName.isNotBlank() && currentFolder != null) {
                            val srcFile = File(currentFolder.path)
                            val destFile = File(srcFile.parentFile, newName)
                            var success = false
                            if (srcFile.exists() && !destFile.exists()) {
                                try {
                                    success = srcFile.renameTo(destFile)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            if (success) {
                                val idx = customFolders.indexOfFirst { it.path == currentFolder.path }
                                if (idx != -1) {
                                    val oldFolder = customFolders[idx]
                                    customFolders[idx] = oldFolder.copy(name = newName, path = destFile.absolutePath)
                                }
                                folderActionTrigger++
                                Toast.makeText(context, if (language == AppLanguage.HINDI) "फ़ोल्डर का नाम बदला गया" else "Folder renamed successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, if (language == AppLanguage.HINDI) "नाम नहीं बदला जा सका" else "Failed to rename folder", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showRenameFolderDialog = false
                        folderToRename = null
                        renameFolderInput = ""
                    }
                ) {
                    Text(if (language == AppLanguage.HINDI) "बदलें" else "Rename")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRenameFolderDialog = false
                        folderToRename = null
                        renameFolderInput = ""
                    }
                ) {
                    Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmDialog && folderToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                folderToDelete = null
            },
            title = {
                Text(
                    text = if (language == AppLanguage.HINDI) "फ़ोल्डर हटाएं?" else "Delete Folder?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == AppLanguage.HINDI) 
                        "क्या आप सचमुच फ़ोल्डर '${folderToDelete?.name}' और उसकी सभी फ़ाइलों को हमेशा के लिए हटाना चाहते हैं? यह प्रक्रिया वापस नहीं ली जा सकती।" 
                        else 
                        "Are you sure you want to delete '${folderToDelete?.name}' and all its contents permanently? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentFolder = folderToDelete
                        if (currentFolder != null) {
                            val folderFile = File(currentFolder.path)
                            var success = false
                            if (folderFile.exists()) {
                                try {
                                    success = folderFile.deleteRecursively()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            } else {
                                success = true
                            }
                            if (success) {
                                customFolders.removeAll { it.path == currentFolder.path }
                                folderActionTrigger++
                                Toast.makeText(context, if (language == AppLanguage.HINDI) "फ़ोल्डर हटा दिया गया" else "Folder deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, if (language == AppLanguage.HINDI) "हटाया नहीं जा सका" else "Failed to delete folder", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showDeleteConfirmDialog = false
                        folderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD93025), contentColor = Color.White)
                ) {
                    Text(if (language == AppLanguage.HINDI) "हटाएं" else "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        folderToDelete = null
                    }
                ) {
                    Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}
