package com.example.storage

import android.content.ContentUris
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import com.example.model.FileCategoryType
import com.example.model.FileItem
import com.example.model.StorageDeviceInfo
import java.io.File

object StorageScanner {

    fun getRealStorageDevices(context: Context): List<StorageDeviceInfo> {
        val list = mutableListOf<StorageDeviceInfo>()
        val seenPaths = mutableSetOf<String>()

        // 1. Phone Memory / Primary Internal Storage
        try {
            val internalRoot = Environment.getExternalStorageDirectory()
            val canonicalPath = try { internalRoot.canonicalPath } catch (e: Exception) { internalRoot.absolutePath }
            seenPaths.add(canonicalPath)

            val stat = StatFs(internalRoot.path)
            val totalBytes = stat.totalBytes
            val freeBytes = stat.availableBytes
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
            val usedPct = if (totalBytes > 0) {
                ((usedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
            } else 0

            list.add(
                StorageDeviceInfo(
                    id = "internal",
                    nameEn = "Phone Memory",
                    nameHi = "फोन मेमोरी",
                    freeBytes = freeBytes,
                    totalBytes = totalBytes,
                    isExternal = false,
                    badge = "Internal",
                    usedPercent = usedPct,
                    rootPath = internalRoot.absolutePath
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Query StorageManager for physical mounted SD Cards / External Storage Volumes
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? android.os.storage.StorageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && storageManager != null) {
            try {
                val volumes = storageManager.storageVolumes
                for (volume in volumes) {
                    val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        volume.directory
                    } else {
                        null
                    }
                    if (dir != null && dir.exists() && dir.canRead()) {
                        val canonical = try { dir.canonicalPath } catch (e: Exception) { dir.absolutePath }
                        if (seenPaths.contains(canonical)) continue
                        seenPaths.add(canonical)

                        try {
                            val stat = StatFs(dir.path)
                            val totalBytes = stat.totalBytes
                            val freeBytes = stat.availableBytes
                            if (totalBytes > 0) {
                                val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
                                val usedPct = ((usedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                                val isRemovable = volume.isRemovable
                                val desc = volume.getDescription(context) ?: if (isRemovable) "SD Card" else "External Storage"

                                list.add(
                                    StorageDeviceInfo(
                                        id = "sdcard_${canonical.hashCode()}",
                                        nameEn = if (isRemovable) "SD Card ($desc)" else desc,
                                        nameHi = if (isRemovable) "एसडी कार्ड ($desc)" else desc,
                                        freeBytes = freeBytes,
                                        totalBytes = totalBytes,
                                        isExternal = isRemovable,
                                        badge = if (isRemovable) "SD Card" else "Storage",
                                        usedPercent = usedPct,
                                        rootPath = dir.absolutePath
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Scan /storage directory directly for any attached external SD card paths
        try {
            val storageDir = File("/storage")
            if (storageDir.exists() && storageDir.isDirectory) {
                storageDir.listFiles()?.forEach { file ->
                    if (file.isDirectory && file.canRead()) {
                        val nameLower = file.name.lowercase()
                        if (nameLower != "self" && nameLower != "emulated") {
                            val canonical = try { file.canonicalPath } catch (e: Exception) { file.absolutePath }
                            if (!seenPaths.contains(canonical)) {
                                seenPaths.add(canonical)
                                try {
                                    val stat = StatFs(file.path)
                                    val totalBytes = stat.totalBytes
                                    val freeBytes = stat.availableBytes
                                    if (totalBytes > 0) {
                                        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)
                                        val usedPct = ((usedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)

                                        list.add(
                                            StorageDeviceInfo(
                                                id = "sdcard_${file.name}",
                                                nameEn = "SD Card (${file.name})",
                                                nameHi = "एसडी कार्ड (${file.name})",
                                                freeBytes = freeBytes,
                                                totalBytes = totalBytes,
                                                isExternal = true,
                                                badge = "SD Card",
                                                usedPercent = usedPct,
                                                rootPath = file.absolutePath
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }

    fun scanRealStorageFiles(context: Context): List<FileItem> {
        val fileList = mutableListOf<FileItem>()
        val existingPaths = mutableSetOf<String>()

        // 1. Scan MediaStore Audio (Music, Songs)
        try {
            val audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val proj = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATE_MODIFIED
            )
            context.contentResolver.query(audioUri, proj, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathCol) ?: continue
                    if (existingPaths.contains(path)) continue
                    existingPaths.add(path)

                    val name = cursor.getString(nameCol) ?: File(path).name
                    val size = cursor.getLong(sizeCol)
                    val durMs = cursor.getLong(durCol)
                    val durSec = (durMs / 1000).toInt().coerceAtLeast(0)
                    val mins = durSec / 60
                    val secs = durSec % 60
                    val durText = String.format("%02d:%02d", mins, secs)
                    val artist = cursor.getString(artistCol) ?: "Artist"
                    val album = cursor.getString(albumCol) ?: "Album"
                    val dateMod = cursor.getLong(dateCol) * 1000L

                    fileList.add(
                        FileItem(
                            id = "ms_aud_${cursor.getLong(idCol)}",
                            name = name,
                            path = path,
                            sizeBytes = size,
                            category = FileCategoryType.AUDIO,
                            extension = name.substringAfterLast('.', "mp3").lowercase(),
                            dateModified = if (dateMod > 0) dateMod else System.currentTimeMillis(),
                            artist = artist,
                            album = album,
                            durationSeconds = if (durSec > 0) durSec else 180,
                            durationText = durText,
                            isRecent = (System.currentTimeMillis() - dateMod) < 7 * 24 * 60 * 60 * 1000L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Scan MediaStore Video
        try {
            val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val proj = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_MODIFIED
            )
            context.contentResolver.query(videoUri, proj, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathCol) ?: continue
                    if (existingPaths.contains(path)) continue
                    existingPaths.add(path)

                    val name = cursor.getString(nameCol) ?: File(path).name
                    val size = cursor.getLong(sizeCol)
                    val durMs = cursor.getLong(durCol)
                    val durSec = (durMs / 1000).toInt().coerceAtLeast(0)
                    val mins = durSec / 60
                    val secs = durSec % 60
                    val durText = String.format("%02d:%02d", mins, secs)
                    val dateMod = cursor.getLong(dateCol) * 1000L

                    fileList.add(
                        FileItem(
                            id = "ms_vid_${cursor.getLong(idCol)}",
                            name = name,
                            path = path,
                            sizeBytes = size,
                            category = FileCategoryType.VIDEOS,
                            extension = name.substringAfterLast('.', "mp4").lowercase(),
                            dateModified = if (dateMod > 0) dateMod else System.currentTimeMillis(),
                            durationSeconds = if (durSec > 0) durSec else 120,
                            durationText = durText,
                            isRecent = (System.currentTimeMillis() - dateMod) < 7 * 24 * 60 * 60 * 1000L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Scan MediaStore Images
        try {
            val imgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val proj = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_MODIFIED
            )
            context.contentResolver.query(imgUri, proj, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathCol) ?: continue
                    if (existingPaths.contains(path)) continue
                    existingPaths.add(path)

                    val name = cursor.getString(nameCol) ?: File(path).name
                    val size = cursor.getLong(sizeCol)
                    val dateMod = cursor.getLong(dateCol) * 1000L

                    fileList.add(
                        FileItem(
                            id = "ms_img_${cursor.getLong(idCol)}",
                            name = name,
                            path = path,
                            sizeBytes = size,
                            category = FileCategoryType.IMAGES,
                            extension = name.substringAfterLast('.', "jpg").lowercase(),
                            dateModified = if (dateMod > 0) dateMod else System.currentTimeMillis(),
                            isRecent = (System.currentTimeMillis() - dateMod) < 7 * 24 * 60 * 60 * 1000L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Scan MediaStore Documents & Downloads & Archives
        try {
            val filesUri = MediaStore.Files.getContentUri("external")
            val proj = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED
            )
            context.contentResolver.query(filesUri, proj, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathCol) ?: continue
                    if (existingPaths.contains(path)) continue

                    val name = cursor.getString(nameCol) ?: File(path).name
                    val ext = name.substringAfterLast('.', "").lowercase()
                    if (ext.isBlank()) continue

                    val cat = when {
                        ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "csv", "vcf") -> FileCategoryType.DOCUMENTS
                        ext in listOf("apk", "xapk") -> FileCategoryType.APPS
                        ext in listOf("zip", "7z", "tar", "gz", "bz2", "xz") -> FileCategoryType.ARCHIVES
                        ext in listOf("jpg", "jpeg", "png", "webp", "gif") -> FileCategoryType.IMAGES
                        ext in listOf("mp4", "mkv", "avi", "mov", "webm") -> FileCategoryType.VIDEOS
                        ext in listOf("mp3", "flac", "wav", "m4a", "ogg") -> FileCategoryType.AUDIO
                        else -> FileCategoryType.DOWNLOADS
                    }

                    existingPaths.add(path)
                    val size = cursor.getLong(sizeCol)
                    val dateMod = cursor.getLong(dateCol) * 1000L

                    fileList.add(
                        FileItem(
                            id = "ms_file_${cursor.getLong(idCol)}",
                            name = name,
                            path = path,
                            sizeBytes = size,
                            category = cat,
                            extension = ext,
                            dateModified = if (dateMod > 0) dateMod else System.currentTimeMillis(),
                            isRecent = (System.currentTimeMillis() - dateMod) < 7 * 24 * 60 * 60 * 1000L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 5. Scan Installed User Apps (एप्स)
        try {
            val pm = context.packageManager
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                // Non-system or updated system apps
                if ((app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    val apkFile = File(app.sourceDir)
                    if (apkFile.exists()) {
                        val appLabel = pm.getApplicationLabel(app).toString()
                        val size = apkFile.length()
                        val dateMod = apkFile.lastModified()
                        fileList.add(
                            FileItem(
                                id = "app_${app.packageName}",
                                name = "$appLabel.apk",
                                path = app.sourceDir,
                                sizeBytes = size,
                                category = FileCategoryType.APPS,
                                extension = "apk",
                                dateModified = dateMod,
                                isRecent = (System.currentTimeMillis() - dateMod) < 14 * 24 * 60 * 60 * 1000L
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 6. Direct File System Scan across Phone Memory & SD Cards
        val scannedDirs = mutableSetOf<String>()
        val rootsToScan = mutableListOf<File>()

        try {
            val internalRoot = Environment.getExternalStorageDirectory()
            if (internalRoot.exists() && internalRoot.isDirectory) {
                rootsToScan.add(internalRoot)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val storageDir = File("/storage")
            if (storageDir.exists() && storageDir.isDirectory) {
                storageDir.listFiles()?.forEach { file ->
                    if (file.isDirectory &&
                        !file.name.equals("self", ignoreCase = true) &&
                        !file.name.equals("emulated", ignoreCase = true)
                    ) {
                        rootsToScan.add(file)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        rootsToScan.distinctBy {
            try { it.canonicalPath } catch (e: Exception) { it.absolutePath }
        }.forEach { root ->
            scanDirectoryRecursively(root, 1, 5, fileList, existingPaths, scannedDirs)
        }

        return fileList
    }

    private fun scanDirectoryRecursively(
        dir: File,
        currentDepth: Int,
        maxDepth: Int,
        list: MutableList<FileItem>,
        existingPaths: MutableSet<String>,
        scannedDirs: MutableSet<String>
    ) {
        val canonicalPath = try { dir.canonicalPath } catch (e: Exception) { dir.absolutePath }
        if (scannedDirs.contains(canonicalPath)) return
        scannedDirs.add(canonicalPath)

        if (currentDepth > maxDepth || list.size >= 3000) return

        try {
            val files = dir.listFiles() ?: return
            for (file in files) {
                if (list.size >= 3000) break

                val name = file.name
                if (name.startsWith(".")) continue

                if (file.isDirectory) {
                    val lowerName = name.lowercase()
                    if (lowerName == "android" ||
                        lowerName == "cache" ||
                        lowerName == "lost.dir" ||
                        lowerName == "thumbnails" ||
                        lowerName == "temp" ||
                        lowerName == "tmp"
                    ) {
                        continue
                    }
                    scanDirectoryRecursively(file, currentDepth + 1, maxDepth, list, existingPaths, scannedDirs)
                } else if (file.isFile) {
                    val path = file.absolutePath
                    if (existingPaths.contains(path)) continue
                    existingPaths.add(path)

                    val extension = file.extension.lowercase()
                    val category = when {
                        extension in listOf("jpg", "jpeg", "png", "webp", "gif", "svg", "bmp", "heic") -> FileCategoryType.IMAGES
                        extension in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "flv", "mpeg") -> FileCategoryType.VIDEOS
                        extension in listOf("mp3", "flac", "wav", "m4a", "ogg", "aac", "wma", "mid") -> FileCategoryType.AUDIO
                        extension in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "csv", "vcf") -> FileCategoryType.DOCUMENTS
                        extension in listOf("apk", "xapk") -> FileCategoryType.APPS
                        extension in listOf("zip", "7z", "tar", "gz", "bz2", "xz") -> FileCategoryType.ARCHIVES
                        else -> FileCategoryType.DOWNLOADS
                    }

                    val size = file.length()
                    var artist = "Local Artist"
                    var album = "Local Album"
                    var durationSec = 180
                    var durationText = "03:00"

                    if (category == FileCategoryType.AUDIO) {
                        val estimatedSeconds = (size / (16 * 1024)).toInt().coerceIn(30, 600)
                        durationSec = estimatedSeconds
                        val mins = estimatedSeconds / 60
                        val secs = estimatedSeconds % 60
                        durationText = String.format("%02d:%02d", mins, secs)
                    }

                    list.add(
                        FileItem(
                            id = "fs_${file.absolutePath.hashCode()}",
                            name = name,
                            path = file.absolutePath,
                            sizeBytes = size,
                            category = category,
                            extension = extension,
                            dateModified = file.lastModified(),
                            artist = artist,
                            album = album,
                            durationSeconds = durationSec,
                            durationText = durationText,
                            isRecent = (System.currentTimeMillis() - file.lastModified()) < 7 * 24 * 60 * 60 * 1000L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
