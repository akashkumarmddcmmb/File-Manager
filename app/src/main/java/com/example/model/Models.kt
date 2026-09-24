package com.example.model

import androidx.compose.ui.graphics.Color

enum class MainTab {
    CLEAN,
    BROWSE,
    SHARE
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AccentColorType(
    val titleEn: String,
    val titleHi: String,
    val primary: Color,
    val container: Color,
    val onContainer: Color,
    val isDynamic: Boolean = false
) {
    SYSTEM_DYNAMIC("Dynamic (System)", "डायनामिक (Material You)", Color(0xFF1A73E8), Color(0xFF1E2D4A), Color(0xFF8AB4F8), true),
    EMERALD("Emerald", "Emerald", Color(0xFF00C853), Color(0xFF1E3A2B), Color(0xFF75F9A7)),
    OCEAN("Ocean", "Ocean", Color(0xFF1A73E8), Color(0xFF192A45), Color(0xFF8AB4F8)),
    ROYAL("Royal", "Royal", Color(0xFF7C4DFF), Color(0xFF2A1C4E), Color(0xFFD1B7FF)),
    SUNSET("Sunset", "Sunset", Color(0xFFFF5722), Color(0xFF421E14), Color(0xFFFFAB91)),
    GOLD("Gold", "Gold", Color(0xFFFFB300), Color(0xFF423214), Color(0xFFFFE082)),
    ARCTIC("Arctic", "Arctic", Color(0xFF00B0FF), Color(0xFF10364A), Color(0xFF80D8FF)),
    CUSTOM("Custom", "कस्टम", Color(0xFF10B981), Color(0xFF133829), Color(0xFFA7F3D0))
}

enum class AppLanguage {
    HINDI,
    ENGLISH
}

enum class FileCategoryType(
    val titleEn: String,
    val titleHi: String,
    val iconColor: Color,
    val iconBgColor: Color
) {
    DOWNLOADS("Downloads", "डाउनलोड", Color(0xFF1A73E8), Color(0xFF1E2D4A)),
    IMAGES("Images", "इमेजेस", Color(0xFF00C853), Color(0xFF1B3B2B)),
    VIDEOS("Videos", "वीडियोस", Color(0xFFD93025), Color(0xFF4A1F1D)),
    AUDIO("Audio", "ऑडियो", Color(0xFFF9AB00), Color(0xFF473618)),
    DOCUMENTS("Documents & Other", "दस्तावेज़", Color(0xFF1A73E8), Color(0xFF1E2D4A)),
    APPS("Apps & APK", "एप्स / APK", Color(0xFF00C853), Color(0xFF1B3B2B)),
    ARCHIVES("Archives", "आर्काइव्स", Color(0xFF9334E6), Color(0xFF381F4E)),
    LARGE_FILES("Large files", "बड़ी फाइलें", Color(0xFFEA8600), Color(0xFF452A14))
}

enum class PlaybackRepeatMode {
    OFF,
    REPEAT_ALL,
    REPEAT_ONE
}

enum class VideoAspectRatioMode {
    FIT_SCREEN,
    FILL_CROP,
    ORIGINAL_RATIO
}

data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val category: FileCategoryType,
    val extension: String,
    val dateModified: Long = System.currentTimeMillis(),
    val isStarred: Boolean = false,
    val isInSafeFolder: Boolean = false,
    val isInTrash: Boolean = false,
    val isRecent: Boolean = false,
    val isHidden: Boolean = false,
    val durationText: String? = null,
    val durationSeconds: Int = 180,
    val artist: String? = null,
    val album: String? = null
) {
    val formattedSize: String
        get() = formatFileSize(sizeBytes)
}

data class StorageDeviceInfo(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val freeBytes: Long,
    val totalBytes: Long,
    val isExternal: Boolean = false,
    val badge: String? = null,
    val usedPercent: Int = 8,
    val rootPath: String = ""
) {
    val freeFormatted: String
        get() = formatFileSize(freeBytes)
    val totalFormatted: String
        get() = formatFileSize(totalBytes)
}

data class CleanJunkItem(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val descEn: String,
    val descHi: String,
    val sizeBytes: Long,
    val categoryType: String = "JUNK"
) {
    val formattedSize: String
        get() = formatFileSize(sizeBytes)
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val tb = gb / 1024.0
    return when {
        tb >= 1.0 -> String.format("%.1f TB", tb)
        gb >= 1.0 -> String.format("%.1f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}

enum class ArchiveFormat(val extension: String, val displayName: String, val supportsEncryption: Boolean) {
    SEVEN_ZIP("7z", "7-Zip (.7z)", true),
    ZIP("zip", ".ZIP (.zip)", true),
    TAR("tar", "TAR (.tar)", false),
    TAR_GZ("tar.gz", "GZip (.tar.gz)", false),
    TAR_BZ2("tar.bz2", "BZip2 (.tar.bz2)", false),
    XZ("xz", "XZ (.xz)", false)
}

enum class CompressionLevel(val levelName: String, val levelNumber: Int, val descriptionEn: String, val descriptionHi: String) {
    STORE("0 - Store (No Compression)", 0, "Fastest, no CPU overhead", "बिना कंप्रेशन (सबसे तेज)"),
    FASTEST("1 - Fastest", 1, "Quick compression, moderate size", "तेज कंप्रेशन"),
    FAST("3 - Fast", 3, "Good speed with decent compression", "सामान्य गति"),
    NORMAL("5 - Normal (Recommended)", 5, "Balanced speed and ratio", "संतुलित अनुपात (अनुशंसित)"),
    MAXIMUM("7 - Maximum", 7, "High compression ratio", "उच्च कंप्रेशन"),
    ULTRA("9 - Ultra", 9, "Highest ratio, uses more RAM", "अल्ट्रा कंप्रेशन (अधिकतम)")
}

enum class CompressionMethod(val displayName: String) {
    LZMA2("LZMA2 (Ultra High Ratio)"),
    LZMA("LZMA (High Ratio)"),
    DEFLATE("Deflate (Standard .ZIP)"),
    DEFLATE64("Deflate64 (Enhanced)"),
    BZIP2("BZip2 (High Compression)"),
    PPMD("PPMd (Best for text/docs)")
}

enum class DictionarySize(val displayName: String) {
    SIZE_16MB("16 MB"),
    SIZE_32MB("32 MB (Recommended)"),
    SIZE_64MB("64 MB (Ultra)"),
    SIZE_128MB("128 MB (Extreme)")
}

enum class SolidBlockSize(val displayName: String) {
    NON_SOLID("Non-solid (Fast random access)"),
    SOLID_2GB("2 GB Solid Block"),
    SOLID_4GB("4 GB Solid Block"),
    SOLID_ALL("Solid (Maximum compression)")
}

enum class SplitVolumeOption(val displayName: String, val bytes: Long) {
    NONE("No splitting (Single file)", 0L),
    SPLIT_10MB("10 MB (Email attachment)", 10 * 1024 * 1024L),
    SPLIT_50MB("50 MB (Social apps)", 50 * 1024 * 1024L),
    SPLIT_100MB("100 MB (Web upload)", 100 * 1024 * 1024L),
    SPLIT_700MB("700 MB (CD-ROM)", 700 * 1024 * 1024L),
    SPLIT_4GB("4092 MB (FAT32 / DVD)", 4092L * 1024 * 1024L)
}

data class ArchiveEntryItem(
    val id: String,
    val name: String,
    val pathInArchive: String,
    val uncompressedBytes: Long,
    val compressedBytes: Long,
    val isDirectory: Boolean = false,
    val dateModified: Long = System.currentTimeMillis(),
    val crc32Hex: String = "A4F89C12",
    val isEncrypted: Boolean = false,
    val compressionRatioPercent: Int = 45
) {
    val uncompressedFormatted: String
        get() = formatFileSize(uncompressedBytes)
    val compressedFormatted: String
        get() = formatFileSize(compressedBytes)
}

data class ArchiveTestResult(
    val isValid: Boolean,
    val filesTested: Int,
    val errorsCount: Int,
    val totalUncompressedBytes: Long,
    val durationMs: Long,
    val details: List<String>
)

data class FolderDisplayItem(
    val name: String,
    val path: String,
    val itemCount: Int = 0,
    val dateModified: Long = System.currentTimeMillis()
)

enum class ClipboardOperationType {
    COPY,
    MOVE
}

data class ClipboardItem(
    val path: String,
    val name: String,
    val isFolder: Boolean,
    val sizeBytes: Long = 0L,
    val fileItem: FileItem? = null
)

data class ClipboardState(
    val action: ClipboardOperationType = ClipboardOperationType.COPY,
    val items: List<ClipboardItem> = emptyList(),
    val isActive: Boolean = false
) {
    val totalBytes: Long
        get() = items.sumOf { it.sizeBytes }
    val formattedTotalSize: String
        get() = formatFileSize(totalBytes)
}

data class TransferProgressState(
    val isTransferring: Boolean = false,
    val action: ClipboardOperationType = ClipboardOperationType.COPY,
    val currentFileName: String = "",
    val currentFileIndex: Int = 0,
    val totalFilesCount: Int = 0,
    val bytesTransferred: Long = 0L,
    val totalBytesToTransfer: Long = 0L,
    val progress: Float = 0f,
    val speedBytesPerSec: Long = 0L,
    val speedFormatted: String = "0.0 MB/s",
    val speedMultiplier: Int = 1, // 1x, 2x, 5x, 10x Turbo
    val estimatedTimeRemainingSec: Long = 0L,
    val isCancelled: Boolean = false
)

