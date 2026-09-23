package com.example.archive

import com.example.model.*

object ArchiveEngine {
    fun inspectArchive(file: FileItem): List<ArchiveEntryItem> {
        return listOf(
            ArchiveEntryItem(
                id = "ae_1",
                name = "report_document_2026.pdf",
                pathInArchive = "report_document_2026.pdf",
                uncompressedBytes = 1258291L, // 1.2 MB
                compressedBytes = 524288L, // 512 KB
                isEncrypted = file.name.contains("secure", ignoreCase = true)
            ),
            ArchiveEntryItem(
                id = "ae_2",
                name = "avatar_profile_image.png",
                pathInArchive = "images/avatar_profile_image.png",
                uncompressedBytes = 460800L, // 450 KB
                compressedBytes = 409600L, // 400 KB
                isEncrypted = file.name.contains("secure", ignoreCase = true)
            ),
            ArchiveEntryItem(
                id = "ae_3",
                name = "audio_voice_memo_raw.wav",
                pathInArchive = "audio/audio_voice_memo_raw.wav",
                uncompressedBytes = 8808038L, // 8.4 MB
                compressedBytes = 3145728L, // 3.0 MB
                isEncrypted = file.name.contains("secure", ignoreCase = true)
            )
        )
    }

    fun createArchive(
        archiveName: String,
        targetPath: String,
        selectedFiles: List<FileItem>,
        format: ArchiveFormat,
        compressionLevel: CompressionLevel,
        compressionMethod: CompressionMethod,
        password: String?,
        splitVolume: SplitVolumeOption
    ): FileItem {
        val totalInputSize = selectedFiles.sumOf { it.sizeBytes }
        // Simulate compression savings
        val ratio = when (compressionLevel) {
            CompressionLevel.STORE -> 1.0
            CompressionLevel.FASTEST -> 0.75
            CompressionLevel.FAST -> 0.65
            CompressionLevel.NORMAL -> 0.55
            CompressionLevel.MAXIMUM -> 0.45
            CompressionLevel.ULTRA -> 0.35
        }
        val outputSize = (totalInputSize * ratio).toLong()
        val extension = format.extension
        val cleanName = if (archiveName.endsWith(".$extension")) archiveName else "$archiveName.$extension"

        return FileItem(
            id = "zip_${System.currentTimeMillis()}",
            name = cleanName,
            path = if (targetPath.endsWith("/")) "$targetPath$cleanName" else "$targetPath/$cleanName",
            sizeBytes = outputSize,
            category = FileCategoryType.ARCHIVES,
            extension = extension,
            dateModified = System.currentTimeMillis()
        )
    }

    fun extractArchive(
        archiveFile: FileItem,
        destinationPath: String,
        password: String? = null
    ): List<FileItem> {
        // Return a list of newly extracted files
        return listOf(
            FileItem(
                id = "ext_1_${System.currentTimeMillis()}",
                name = "report_document_2026.pdf",
                path = "$destinationPath/report_document_2026.pdf",
                sizeBytes = 1258291L,
                category = FileCategoryType.DOCUMENTS,
                extension = "pdf"
            ),
            FileItem(
                id = "ext_2_${System.currentTimeMillis()}",
                name = "avatar_profile_image.png",
                path = "$destinationPath/avatar_profile_image.png",
                sizeBytes = 460800L,
                category = FileCategoryType.IMAGES,
                extension = "png"
            ),
            FileItem(
                id = "ext_3_${System.currentTimeMillis()}",
                name = "audio_voice_memo_raw.wav",
                path = "$destinationPath/audio_voice_memo_raw.wav",
                sizeBytes = 8808038L,
                category = FileCategoryType.AUDIO,
                extension = "wav"
            )
        )
    }

    fun testArchiveIntegrity(archiveFile: FileItem): ArchiveTestResult {
        return ArchiveTestResult(
            isValid = true,
            filesTested = 3,
            errorsCount = 0,
            totalUncompressedBytes = 10527129L,
            durationMs = 240,
            details = listOf(
                "Verifying report_document_2026.pdf: CRC32 Match [OK]",
                "Verifying avatar_profile_image.png: CRC32 Match [OK]",
                "Verifying audio_voice_memo_raw.wav: CRC32 Match [OK]"
            )
        )
    }
}
