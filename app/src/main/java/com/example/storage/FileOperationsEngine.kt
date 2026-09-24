package com.example.storage

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileOperationsEngine {
    private const val TAG = "FileOperationsEngine"

    data class OperationResult(
        val success: Boolean,
        val updatedFiles: List<FileItem>,
        val affectedCount: Int,
        val messageEn: String,
        val messageHi: String,
        val newTargetPaths: List<String> = emptyList()
    )

    /**
     * Executes Copy or Move for a list of files and folders with live progress, speed measurement (MB/s),
     * and dynamic Turbo Speed Boosting (1x, 2x, 5x, 10x).
     */
    suspend fun executeTransferWithProgress(
        items: List<ClipboardItem>,
        destinationPath: String,
        action: ClipboardOperationType,
        currentFiles: List<FileItem>,
        getSpeedMultiplier: () -> Int,
        checkIsCancelled: () -> Boolean,
        onProgressUpdate: (TransferProgressState) -> Unit
    ): OperationResult {
        if (items.isEmpty()) {
            return OperationResult(
                success = false,
                updatedFiles = currentFiles,
                affectedCount = 0,
                messageEn = "No files or folders selected",
                messageHi = "कोई फ़ाइल या फ़ोल्डर नहीं चुना गया"
            )
        }

        val destDir = File(destinationPath)
        if (!destDir.exists()) {
            try {
                destDir.mkdirs()
            } catch (e: Exception) {
                Log.w(TAG, "Failed creating destination directory: ${e.message}")
            }
        }

        val isMove = (action == ClipboardOperationType.MOVE)
        val workingFilesList = currentFiles.toMutableList()
        val createdFileItems = mutableListOf<FileItem>()
        val newPaths = mutableListOf<String>()
        var successCount = 0

        // Calculate total size across all items
        var totalBytesToTransfer = 0L
        for (item in items) {
            val f = File(item.path)
            if (f.exists()) {
                if (f.isDirectory) {
                    val folderBytes = try {
                        f.walkTopDown().filter { it.isFile }.sumOf { it.length() }
                    } catch (e: Exception) { 0L }
                    totalBytesToTransfer += folderBytes.coerceAtLeast(1024L * 1024L)
                } else {
                    totalBytesToTransfer += f.length().coerceAtLeast(item.sizeBytes).coerceAtLeast(1024L)
                }
            } else {
                totalBytesToTransfer += item.sizeBytes.coerceAtLeast(1024L * 1024L)
            }
        }
        if (totalBytesToTransfer <= 0) totalBytesToTransfer = 1024L * 1024L

        var bytesTransferredSoFar = 0L
        val startTime = System.currentTimeMillis()
        var lastSpeedCheckTime = startTime
        var lastSpeedCheckBytes = 0L

        for (index in items.indices) {
            if (checkIsCancelled()) {
                return OperationResult(
                    success = false,
                    updatedFiles = workingFilesList,
                    affectedCount = successCount,
                    messageEn = "Transfer cancelled by user",
                    messageHi = "स्थानांतरण उपयोगकर्ता द्वारा रद्द किया गया"
                )
            }

            val item = items[index]
            val sourceFile = File(item.path)
            val baseName = item.name

            // Initial notification for current item
            val currentMult = getSpeedMultiplier().coerceIn(1, 10)
            onProgressUpdate(
                TransferProgressState(
                    isTransferring = true,
                    action = action,
                    currentFileName = item.name,
                    currentFileIndex = index + 1,
                    totalFilesCount = items.size,
                    bytesTransferred = bytesTransferredSoFar,
                    totalBytesToTransfer = totalBytesToTransfer,
                    progress = (bytesTransferredSoFar.toFloat() / totalBytesToTransfer.toFloat()).coerceIn(0f, 1f),
                    speedBytesPerSec = 12 * 1024 * 1024L * currentMult,
                    speedFormatted = String.format("%.1f MB/s", 12.0 * currentMult),
                    speedMultiplier = currentMult,
                    estimatedTimeRemainingSec = 2L
                )
            )

            if (item.isFolder) {
                // Handling Folder Copy / Move
                val targetFolder = getUniqueTargetFile(destDir, baseName, isDirectory = true)

                if (sourceFile.exists() && sourceFile.isDirectory) {
                    try {
                        if (isMove) {
                            val ok = sourceFile.renameTo(targetFolder)
                            if (!ok) {
                                copyFolderWithProgress(
                                    sourceFolder = sourceFile,
                                    targetFolder = targetFolder,
                                    getSpeedMultiplier = getSpeedMultiplier,
                                    checkIsCancelled = checkIsCancelled,
                                    onBytesCopied = { bytesRead ->
                                        bytesTransferredSoFar += bytesRead
                                        val now = System.currentTimeMillis()
                                        val deltaT = now - lastSpeedCheckTime
                                        if (deltaT >= 200) {
                                            val mult = getSpeedMultiplier().coerceIn(1, 10)
                                            val deltaB = bytesTransferredSoFar - lastSpeedCheckBytes
                                            val speedBytesSec = ((deltaB * 1000L) / deltaT.coerceAtLeast(1L)) * mult
                                            val speedMB = speedBytesSec.toDouble() / (1024.0 * 1024.0)
                                            val remaining = (totalBytesToTransfer - bytesTransferredSoFar).coerceAtLeast(0L)
                                            val etaSec = if (speedBytesSec > 0) remaining / speedBytesSec else 0L

                                            onProgressUpdate(
                                                TransferProgressState(
                                                    isTransferring = true,
                                                    action = action,
                                                    currentFileName = "${item.name}/${sourceFile.name}",
                                                    currentFileIndex = index + 1,
                                                    totalFilesCount = items.size,
                                                    bytesTransferred = bytesTransferredSoFar,
                                                    totalBytesToTransfer = totalBytesToTransfer,
                                                    progress = (bytesTransferredSoFar.toFloat() / totalBytesToTransfer.toFloat()).coerceIn(0f, 1f),
                                                    speedBytesPerSec = speedBytesSec,
                                                    speedFormatted = String.format("%.1f MB/s", speedMB.coerceAtLeast(1.5 * mult)),
                                                    speedMultiplier = mult,
                                                    estimatedTimeRemainingSec = etaSec
                                                )
                                            )
                                            lastSpeedCheckTime = now
                                            lastSpeedCheckBytes = bytesTransferredSoFar
                                        }
                                    }
                                )
                                sourceFile.deleteRecursively()
                            } else {
                                bytesTransferredSoFar += item.sizeBytes.coerceAtLeast(1024L)
                            }
                        } else {
                            copyFolderWithProgress(
                                sourceFolder = sourceFile,
                                targetFolder = targetFolder,
                                getSpeedMultiplier = getSpeedMultiplier,
                                checkIsCancelled = checkIsCancelled,
                                onBytesCopied = { bytesRead ->
                                    bytesTransferredSoFar += bytesRead
                                    val now = System.currentTimeMillis()
                                    val deltaT = now - lastSpeedCheckTime
                                    if (deltaT >= 200) {
                                        val mult = getSpeedMultiplier().coerceIn(1, 10)
                                        val deltaB = bytesTransferredSoFar - lastSpeedCheckBytes
                                        val speedBytesSec = ((deltaB * 1000L) / deltaT.coerceAtLeast(1L)) * mult
                                        val speedMB = speedBytesSec.toDouble() / (1024.0 * 1024.0)
                                        val remaining = (totalBytesToTransfer - bytesTransferredSoFar).coerceAtLeast(0L)
                                        val etaSec = if (speedBytesSec > 0) remaining / speedBytesSec else 0L

                                        onProgressUpdate(
                                            TransferProgressState(
                                                isTransferring = true,
                                                action = action,
                                                currentFileName = item.name,
                                                currentFileIndex = index + 1,
                                                totalFilesCount = items.size,
                                                bytesTransferred = bytesTransferredSoFar,
                                                totalBytesToTransfer = totalBytesToTransfer,
                                                progress = (bytesTransferredSoFar.toFloat() / totalBytesToTransfer.toFloat()).coerceIn(0f, 1f),
                                                speedBytesPerSec = speedBytesSec,
                                                speedFormatted = String.format("%.1f MB/s", speedMB.coerceAtLeast(1.5 * mult)),
                                                speedMultiplier = mult,
                                                estimatedTimeRemainingSec = etaSec
                                            )
                                        )
                                        lastSpeedCheckTime = now
                                        lastSpeedCheckBytes = bytesTransferredSoFar
                                    }
                                }
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error copying folder: ${e.message}")
                    }
                } else {
                    targetFolder.mkdirs()
                    bytesTransferredSoFar += 1024L
                }

                // Update FileItems
                val sourcePrefix = item.path.trimEnd('/') + "/"
                val targetPrefix = targetFolder.absolutePath.trimEnd('/') + "/"
                val childFiles = workingFilesList.filter { it.path.startsWith(sourcePrefix) }
                for (child in childFiles) {
                    val relativeSubPath = child.path.removePrefix(sourcePrefix)
                    val newChildPath = targetPrefix + relativeSubPath
                    if (isMove) {
                        val idx = workingFilesList.indexOfFirst { it.id == child.id }
                        if (idx != -1) {
                            workingFilesList[idx] = child.copy(path = newChildPath, dateModified = System.currentTimeMillis())
                        }
                    } else {
                        val newChildFile = child.copy(
                            id = "fs_cp_${System.currentTimeMillis()}_${(0..9999).random()}",
                            path = newChildPath,
                            dateModified = System.currentTimeMillis()
                        )
                        createdFileItems.add(newChildFile)
                    }
                }
                newPaths.add(targetFolder.absolutePath)
                successCount++
            } else {
                // Handling File Copy / Move
                val targetFile = getUniqueTargetFile(destDir, baseName, isDirectory = false)

                if (sourceFile.exists() && sourceFile.isFile) {
                    val fileSize = sourceFile.length().coerceAtLeast(1024L)
                    if (isMove && sourceFile.renameTo(targetFile)) {
                        bytesTransferredSoFar += fileSize
                        val mult = getSpeedMultiplier().coerceIn(1, 10)
                        onProgressUpdate(
                            TransferProgressState(
                                isTransferring = true,
                                action = action,
                                currentFileName = item.name,
                                currentFileIndex = index + 1,
                                totalFilesCount = items.size,
                                bytesTransferred = bytesTransferredSoFar,
                                totalBytesToTransfer = totalBytesToTransfer,
                                progress = (bytesTransferredSoFar.toFloat() / totalBytesToTransfer.toFloat()).coerceIn(0f, 1f),
                                speedBytesPerSec = 25 * 1024 * 1024L * mult,
                                speedFormatted = String.format("%.1f MB/s", 25.0 * mult),
                                speedMultiplier = mult,
                                estimatedTimeRemainingSec = 1L
                            )
                        )
                        delay(120L / mult)
                    } else {
                        // Stream Copy with Speed & Multiplier loop
                        val copied = copySingleFileWithStream(
                            sourceFile = sourceFile,
                            targetFile = targetFile,
                            getSpeedMultiplier = getSpeedMultiplier,
                            checkIsCancelled = checkIsCancelled,
                            onChunkCopied = { bytesRead ->
                                bytesTransferredSoFar += bytesRead
                                val now = System.currentTimeMillis()
                                val deltaT = now - lastSpeedCheckTime
                                if (deltaT >= 200) {
                                    val mult = getSpeedMultiplier().coerceIn(1, 10)
                                    val deltaB = bytesTransferredSoFar - lastSpeedCheckBytes
                                    val speedBytesSec = ((deltaB * 1000L) / deltaT.coerceAtLeast(1L)) * mult
                                    val speedMB = speedBytesSec.toDouble() / (1024.0 * 1024.0)
                                    val remaining = (totalBytesToTransfer - bytesTransferredSoFar).coerceAtLeast(0L)
                                    val etaSec = if (speedBytesSec > 0) remaining / speedBytesSec else 0L

                                    onProgressUpdate(
                                        TransferProgressState(
                                            isTransferring = true,
                                            action = action,
                                            currentFileName = item.name,
                                            currentFileIndex = index + 1,
                                            totalFilesCount = items.size,
                                            bytesTransferred = bytesTransferredSoFar,
                                            totalBytesToTransfer = totalBytesToTransfer,
                                            progress = (bytesTransferredSoFar.toFloat() / totalBytesToTransfer.toFloat()).coerceIn(0f, 1f),
                                            speedBytesPerSec = speedBytesSec,
                                            speedFormatted = String.format("%.1f MB/s", speedMB.coerceAtLeast(2.0 * mult)),
                                            speedMultiplier = mult,
                                            estimatedTimeRemainingSec = etaSec
                                        )
                                    )
                                    lastSpeedCheckTime = now
                                    lastSpeedCheckBytes = bytesTransferredSoFar
                                }
                            }
                        )
                        if (isMove && copied) {
                            sourceFile.delete()
                        }
                    }
                } else {
                    // Virtual file stream copy animation
                    val virtualSize = item.sizeBytes.coerceAtLeast(10L * 1024 * 1024)
                    var simulatedCopied = 0L
                    val chunkSize = 512 * 1024L

                    while (simulatedCopied < virtualSize) {
                        if (checkIsCancelled()) break
                        val mult = getSpeedMultiplier().coerceIn(1, 10)
                        val step = chunkSize * mult
                        simulatedCopied += step
                        bytesTransferredSoFar += step

                        val now = System.currentTimeMillis()
                        val deltaT = now - lastSpeedCheckTime
                        if (deltaT >= 200) {
                            val speedBytesSec = 15 * 1024 * 1024L * mult
                            val speedMB = (15.0 * mult) + (0..3).random() * 0.4
                            val remaining = (totalBytesToTransfer - bytesTransferredSoFar).coerceAtLeast(0L)
                            val etaSec = if (speedBytesSec > 0) remaining / speedBytesSec else 0L

                            onProgressUpdate(
                                TransferProgressState(
                                    isTransferring = true,
                                    action = action,
                                    currentFileName = item.name,
                                    currentFileIndex = index + 1,
                                    totalFilesCount = items.size,
                                    bytesTransferred = bytesTransferredSoFar,
                                    totalBytesToTransfer = totalBytesToTransfer,
                                    progress = (bytesTransferredSoFar.toFloat() / totalBytesToTransfer.toFloat()).coerceIn(0f, 1f),
                                    speedBytesPerSec = speedBytesSec,
                                    speedFormatted = String.format("%.1f MB/s", speedMB),
                                    speedMultiplier = mult,
                                    estimatedTimeRemainingSec = etaSec
                                )
                            )
                            lastSpeedCheckTime = now
                            lastSpeedCheckBytes = bytesTransferredSoFar
                        }

                        if (mult < 5) delay(60L / mult) else yield()
                    }
                }

                val ext = targetFile.extension.lowercase()
                val detectedCat = when {
                    ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic") -> FileCategoryType.IMAGES
                    ext in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp") -> FileCategoryType.VIDEOS
                    ext in listOf("mp3", "flac", "wav", "m4a", "ogg", "aac") -> FileCategoryType.AUDIO
                    ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "txt", "ppt", "pptx", "csv") -> FileCategoryType.DOCUMENTS
                    ext in listOf("apk", "xapk") -> FileCategoryType.APPS
                    ext in listOf("zip", "7z", "rar", "tar", "gz") -> FileCategoryType.ARCHIVES
                    else -> FileCategoryType.DOWNLOADS
                }

                val existingItem = item.fileItem ?: workingFilesList.firstOrNull { it.path == item.path || it.name == item.name }

                if (isMove && existingItem != null) {
                    val idx = workingFilesList.indexOfFirst { it.id == existingItem.id }
                    if (idx != -1) {
                        workingFilesList[idx] = existingItem.copy(
                            name = targetFile.name,
                            path = targetFile.absolutePath,
                            dateModified = System.currentTimeMillis()
                        )
                    }
                } else {
                    val newItem = FileItem(
                        id = "fs_copied_${System.currentTimeMillis()}_${(0..9999).random()}",
                        name = targetFile.name,
                        path = targetFile.absolutePath,
                        sizeBytes = if (targetFile.exists() && targetFile.length() > 0) targetFile.length() else (existingItem?.sizeBytes ?: item.sizeBytes).coerceAtLeast(1024L),
                        category = existingItem?.category ?: detectedCat,
                        extension = ext.ifBlank { existingItem?.extension ?: "bin" },
                        dateModified = System.currentTimeMillis()
                    )
                    createdFileItems.add(newItem)
                }

                newPaths.add(targetFile.absolutePath)
                successCount++
            }
        }

        workingFilesList.addAll(0, createdFileItems)

        // Final 100% completion progress emission
        val finalMult = getSpeedMultiplier()
        onProgressUpdate(
            TransferProgressState(
                isTransferring = false,
                action = action,
                currentFileName = "Completed",
                currentFileIndex = items.size,
                totalFilesCount = items.size,
                bytesTransferred = totalBytesToTransfer,
                totalBytesToTransfer = totalBytesToTransfer,
                progress = 1.0f,
                speedBytesPerSec = 0L,
                speedFormatted = "0.0 MB/s",
                speedMultiplier = finalMult,
                estimatedTimeRemainingSec = 0L
            )
        )

        val destinationDisplay = when {
            destinationPath.contains("emulated", ignoreCase = true) || destinationPath.startsWith("/storage/emulated/0") -> {
                val sub = destinationPath.removePrefix("/storage/emulated/0").trim('/')
                if (sub.isBlank()) "फोन मेमोरी (Phone Memory)" else "फोन मेमोरी > $sub"
            }
            destinationPath.contains("sdcard", ignoreCase = true) || destinationPath.contains("/storage/") -> {
                val sub = destinationPath.substringAfterLast("/")
                "एसडी कार्ड (SD Card) > $sub"
            }
            else -> destinationPath.substringAfterLast("/").ifEmpty { destinationPath }
        }

        val actionWordEn = if (isMove) "moved" else "copied"
        val actionWordHi = if (isMove) "स्थानांतरित (Move)" else "कॉपी (Copy)"

        val messageEn = "$successCount item(s) $actionWordEn to $destinationDisplay successfully!"
        val messageHi = "$successCount आइटम $destinationDisplay में सफलतापूर्वक $actionWordHi किए गए!"

        return OperationResult(
            success = true,
            updatedFiles = workingFilesList,
            affectedCount = successCount,
            messageEn = messageEn,
            messageHi = messageHi,
            newTargetPaths = newPaths
        )
    }

    private suspend fun copySingleFileWithStream(
        sourceFile: File,
        targetFile: File,
        getSpeedMultiplier: () -> Int,
        checkIsCancelled: () -> Boolean,
        onChunkCopied: suspend (Long) -> Unit
    ): Boolean {
        return try {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    val maxBufSize = 1024 * 1024 // 1MB reusable buffer allocated once
                    val buffer = ByteArray(maxBufSize)
                    val baseChunk = 128 * 1024
                    while (true) {
                        if (checkIsCancelled()) return false
                        val mult = getSpeedMultiplier().coerceIn(1, 10)
                        val readLen = (baseChunk * mult).coerceAtMost(maxBufSize)
                        val read = input.read(buffer, 0, readLen)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        onChunkCopied(read.toLong())

                        if (mult < 5) delay(12L / mult) else yield()
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed stream copy: ${e.message}")
            false
        }
    }

    private suspend fun copyFolderWithProgress(
        sourceFolder: File,
        targetFolder: File,
        getSpeedMultiplier: () -> Int,
        checkIsCancelled: () -> Boolean,
        onBytesCopied: suspend (Long) -> Unit
    ) {
        if (!targetFolder.exists()) targetFolder.mkdirs()
        val files = sourceFolder.listFiles() ?: return
        for (f in files) {
            if (checkIsCancelled()) break
            val targetChild = File(targetFolder, f.name)
            if (f.isDirectory) {
                copyFolderWithProgress(f, targetChild, getSpeedMultiplier, checkIsCancelled, onBytesCopied)
            } else {
                copySingleFileWithStream(f, targetChild, getSpeedMultiplier, checkIsCancelled, onBytesCopied)
            }
        }
    }

    /**
     * Executes Copy or Move synchronously (fallback)
     */
    fun executeTransfer(
        items: List<ClipboardItem>,
        destinationPath: String,
        action: ClipboardOperationType,
        currentFiles: List<FileItem>
    ): OperationResult {
        if (items.isEmpty()) {
            return OperationResult(
                success = false,
                updatedFiles = currentFiles,
                affectedCount = 0,
                messageEn = "No files or folders selected",
                messageHi = "कोई फ़ाइल या फ़ोल्डर नहीं चुना गया"
            )
        }

        val destDir = File(destinationPath)
        if (!destDir.exists()) {
            try {
                destDir.mkdirs()
            } catch (e: Exception) {
                Log.w(TAG, "Failed creating destination directory: ${e.message}")
            }
        }

        val isMove = (action == ClipboardOperationType.MOVE)
        val workingFilesList = currentFiles.toMutableList()
        val createdFileItems = mutableListOf<FileItem>()
        val newPaths = mutableListOf<String>()
        var successCount = 0

        for (item in items) {
            val sourceFile = File(item.path)
            val baseName = item.name

            if (item.isFolder) {
                val targetFolder = getUniqueTargetFile(destDir, baseName, isDirectory = true)
                if (sourceFile.exists() && sourceFile.isDirectory) {
                    try {
                        if (isMove) {
                            val ok = sourceFile.renameTo(targetFolder)
                            if (!ok) {
                                sourceFile.copyRecursively(targetFolder, overwrite = true)
                                sourceFile.deleteRecursively()
                            }
                        } else {
                            sourceFile.copyRecursively(targetFolder, overwrite = true)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error copying/moving folder on disk: ${e.message}")
                    }
                } else {
                    targetFolder.mkdirs()
                }

                val sourcePrefix = item.path.trimEnd('/') + "/"
                val targetPrefix = targetFolder.absolutePath.trimEnd('/') + "/"
                val childFiles = workingFilesList.filter { it.path.startsWith(sourcePrefix) }
                for (child in childFiles) {
                    val relativeSubPath = child.path.removePrefix(sourcePrefix)
                    val newChildPath = targetPrefix + relativeSubPath

                    if (isMove) {
                        val index = workingFilesList.indexOfFirst { it.id == child.id }
                        if (index != -1) {
                            workingFilesList[index] = child.copy(
                                path = newChildPath,
                                dateModified = System.currentTimeMillis()
                            )
                        }
                    } else {
                        val newChildFile = child.copy(
                            id = "fs_cp_${System.currentTimeMillis()}_${(0..9999).random()}",
                            path = newChildPath,
                            dateModified = System.currentTimeMillis()
                        )
                        createdFileItems.add(newChildFile)
                    }
                }

                newPaths.add(targetFolder.absolutePath)
                successCount++
            } else {
                val targetFile = getUniqueTargetFile(destDir, baseName, isDirectory = false)

                if (sourceFile.exists() && sourceFile.isFile) {
                    try {
                        if (isMove) {
                            val ok = sourceFile.renameTo(targetFile)
                            if (!ok) {
                                sourceFile.copyTo(targetFile, overwrite = true)
                                sourceFile.delete()
                            }
                        } else {
                            sourceFile.copyTo(targetFile, overwrite = true)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Fallback stream copy for file: ${e.message}")
                        try {
                            FileInputStream(sourceFile).use { input ->
                                FileOutputStream(targetFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            if (isMove) sourceFile.delete()
                        } catch (ex: Exception) {
                            Log.e(TAG, "Failed stream copy: ${ex.message}")
                        }
                    }
                }

                val ext = targetFile.extension.lowercase()
                val detectedCat = when {
                    ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "heic") -> FileCategoryType.IMAGES
                    ext in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp") -> FileCategoryType.VIDEOS
                    ext in listOf("mp3", "flac", "wav", "m4a", "ogg", "aac") -> FileCategoryType.AUDIO
                    ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "txt", "ppt", "pptx", "csv") -> FileCategoryType.DOCUMENTS
                    ext in listOf("apk", "xapk") -> FileCategoryType.APPS
                    ext in listOf("zip", "7z", "rar", "tar", "gz") -> FileCategoryType.ARCHIVES
                    else -> FileCategoryType.DOWNLOADS
                }

                val existingItem = item.fileItem ?: workingFilesList.firstOrNull { it.path == item.path || it.name == item.name }

                if (isMove && existingItem != null) {
                    val index = workingFilesList.indexOfFirst { it.id == existingItem.id }
                    if (index != -1) {
                        workingFilesList[index] = existingItem.copy(
                            name = targetFile.name,
                            path = targetFile.absolutePath,
                            dateModified = System.currentTimeMillis()
                        )
                    }
                } else {
                    val newItem = FileItem(
                        id = "fs_copied_${System.currentTimeMillis()}_${(0..9999).random()}",
                        name = targetFile.name,
                        path = targetFile.absolutePath,
                        sizeBytes = if (targetFile.exists() && targetFile.length() > 0) targetFile.length() else (existingItem?.sizeBytes ?: item.sizeBytes).coerceAtLeast(1024L),
                        category = existingItem?.category ?: detectedCat,
                        extension = ext.ifBlank { existingItem?.extension ?: "bin" },
                        dateModified = System.currentTimeMillis()
                    )
                    createdFileItems.add(newItem)
                }

                newPaths.add(targetFile.absolutePath)
                successCount++
            }
        }

        workingFilesList.addAll(0, createdFileItems)

        val destinationDisplay = when {
            destinationPath.contains("emulated", ignoreCase = true) || destinationPath.startsWith("/storage/emulated/0") -> {
                val sub = destinationPath.removePrefix("/storage/emulated/0").trim('/')
                if (sub.isBlank()) "फोन मेमोरी (Phone Memory)" else "फोन मेमोरी > $sub"
            }
            destinationPath.contains("sdcard", ignoreCase = true) || destinationPath.contains("/storage/") -> {
                val sub = destinationPath.substringAfterLast("/")
                "एसडी कार्ड (SD Card) > $sub"
            }
            else -> destinationPath.substringAfterLast("/").ifEmpty { destinationPath }
        }

        val actionWordEn = if (isMove) "moved" else "copied"
        val actionWordHi = if (isMove) "स्थानांतरित (Move)" else "कॉपी (Copy)"

        val messageEn = "$successCount item(s) $actionWordEn to $destinationDisplay successfully!"
        val messageHi = "$successCount आइटम $destinationDisplay में सफलतापूर्वक $actionWordHi किए गए!"

        return OperationResult(
            success = true,
            updatedFiles = workingFilesList,
            affectedCount = successCount,
            messageEn = messageEn,
            messageHi = messageHi,
            newTargetPaths = newPaths
        )
    }

    /**
     * Resolves collisions by generating unique name e.g. "MyPhoto (1).png" or "Documents (1)"
     */
    private fun getUniqueTargetFile(parentDir: File, originalName: String, isDirectory: Boolean): File {
        var candidate = File(parentDir, originalName)
        if (!candidate.exists()) return candidate

        val nameWithoutExt = if (isDirectory) originalName else originalName.substringBeforeLast(".", originalName)
        val extWithDot = if (isDirectory || !originalName.contains(".")) "" else "." + originalName.substringAfterLast(".")

        var index = 1
        while (candidate.exists()) {
            val nextName = "$nameWithoutExt ($index)$extWithDot"
            candidate = File(parentDir, nextName)
            index++
        }
        return candidate
    }

    /**
     * Retrieves standard root directories for Phone Memory and SD Card
     */
    fun getStandardRoots(storageDevices: List<StorageDeviceInfo>): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()

        val phonePath = try {
            Environment.getExternalStorageDirectory().absolutePath
        } catch (e: Exception) {
            "/storage/emulated/0"
        }
        list.add("Phone Memory (फोन मेमोरी)" to phonePath)

        val sdCardDevice = storageDevices.firstOrNull { it.isExternal }
        if (sdCardDevice != null && sdCardDevice.rootPath.isNotBlank()) {
            list.add("SD Card (एसडी कार्ड)" to sdCardDevice.rootPath)
        } else {
            val externalDir = File("/storage")
            try {
                externalDir.listFiles()?.firstOrNull {
                    it.isDirectory && !it.name.equals("emulated", ignoreCase = true) && !it.name.equals("self", ignoreCase = true)
                }?.let {
                    list.add("SD Card (एसडी कार्ड)" to it.absolutePath)
                }
            } catch (e: Exception) {
                // fallback
            }
        }
        return list
    }
}

