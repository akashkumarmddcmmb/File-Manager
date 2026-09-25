package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.archive.ArchiveEngine
import com.example.audio.AudioNotificationController
import com.example.audio.AudioNotificationListener
import com.example.audio.RealAudioEngine
import com.example.model.*
import com.example.storage.StorageScanner
import com.example.update.AppUpdateManager
import com.example.update.UpdateResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FilesUiState(
    val currentTab: MainTab = MainTab.BROWSE,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColorType = AccentColorType.SYSTEM_DYNAMIC,
    val showHiddenFiles: Boolean = false,
    val junkAlertEnabled: Boolean = true,
    val safeFolderPin: String = "1234",
    val isSafeFolderUnlocked: Boolean = false,
    val lockOnExitImmediately: Boolean = true,
    val quickShareDeviceName: String = "Akash's Android Device",
    val quickShareVisibilityAll: Boolean = true,
    val githubRepoPath: String = "akashkumarmddcmmb/file-manager",
    val isCheckingForUpdates: Boolean = false,
    val updateResult: UpdateResult? = null,
    val showUpdateDialog: Boolean = false,
    val backgroundMusicPlayback: Boolean = true,
    val isUltraBatterySaver: Boolean = true,
    val searchQuery: String = "",
    val isGridView: Boolean = false,
    val sortOrder: String = "DATE_DESC", // DATE_DESC, NAME_ASC, SIZE_DESC
    val activeFileDetail: FileItem? = null,
    val selectedCategory: FileCategoryType? = null,
    val selectedStorageDevice: StorageDeviceInfo? = null,
    val isSettingsOpen: Boolean = false,
    val showFeatureList: Boolean = false,
    val showSafeFolderDialog: Boolean = false,
    val showTrashDialog: Boolean = false,
    val showStorageBreakdown: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showSignInDialog: Boolean = false,
    val showPrivacyDialog: Boolean = false,
    val showTermsDialog: Boolean = false,
    val showFeedbackDialog: Boolean = false,
    val showPinChangeDialog: Boolean = false,
    val showRecoveryDialog: Boolean = false,
    val isCleaningInProgress: Boolean = false,
    val cleanSuccessMessage: String? = null,

    // .ZIP & Archive State
    val activeArchiveFile: FileItem? = null,
    val archiveEntries: List<ArchiveEntryItem> = emptyList(),
    val showArchiveViewer: Boolean = false,
    val showArchiveCompressDialog: Boolean = false,
    val showArchiveExtractDialog: Boolean = false,
    val showArchiveTestResultDialog: Boolean = false,
    val archiveTestResult: ArchiveTestResult? = null,
    val selectedFilesForArchive: List<FileItem> = emptyList(),
    val isArchiveProcessing: Boolean = false,

    // Clipboard & File Transfer State (Copy / Move / Paste)
    val clipboardState: ClipboardState = ClipboardState(),
    val showFolderDestinationPicker: Boolean = false,
    val pendingTransferAction: ClipboardOperationType? = null,
    val pendingTransferItems: List<ClipboardItem> = emptyList(),
    val fileTransferSuccessToast: String? = null,
    val transferProgressState: TransferProgressState = TransferProgressState(),


    // Music Player State (MP3)
    val playingAudioFile: FileItem? = null,
    val isAudioPlaying: Boolean = false,
    val audioPositionSeconds: Int = 0,
    val audioDurationSeconds: Int = 268,
    val isAudioShuffle: Boolean = false,
    val audioRepeatMode: PlaybackRepeatMode = PlaybackRepeatMode.REPEAT_ALL,
    val audioPlaybackSpeed: Float = 1.0f,
    val audioEqualizerPreset: String = "Bass Boost",
    val audioEqualizerEnabled: Boolean = true,
    val audioSleepTimerMinutes: Int = 0,
    val showFullAudioPlayer: Boolean = false,

    // Video Player State (MP4)
    val playingVideoFile: FileItem? = null,
    val isVideoPlaying: Boolean = false,
    val videoPositionSeconds: Int = 0,
    val videoDurationSeconds: Int = 225,
    val videoPlaybackSpeed: Float = 1.0f,
    val videoAspectRatio: VideoAspectRatioMode = VideoAspectRatioMode.FIT_SCREEN,
    val isVideoLocked: Boolean = false,
    val videoVolume: Float = 0.8f,
    val videoBrightness: Float = 0.75f,
    val showSubtitles: Boolean = true,
    val selectedSubtitleLang: String = "Hindi",
    val showFullVideoPlayer: Boolean = false,

    // Image & Photo Viewer State
    val activeImageFile: FileItem? = null,
    val showImageViewer: Boolean = false,

    // PDF & Document Viewer State
    val activePdfFile: FileItem? = null,
    val showPdfViewer: Boolean = false,

    val files: List<FileItem> = defaultInitialFiles(),
    val storageDevices: List<StorageDeviceInfo> = defaultStorageDevices(),
    val junkItems: List<CleanJunkItem> = defaultJunkItems()
)

class FilesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    private val videoAudioEngine = RealAudioEngine()
    private var media3AudioManager: com.example.audio.Media3AudioManager? = null

    fun setContext(context: Context) {
        videoAudioEngine.setContext(context)
    }

    fun setMedia3AudioManager(manager: com.example.audio.Media3AudioManager) {
        this.media3AudioManager = manager
        manager.initialize { isPlaying, position, duration ->
            _uiState.update {
                it.copy(
                    isAudioPlaying = isPlaying,
                    audioPositionSeconds = position,
                    audioDurationSeconds = if (duration > 0) duration else it.audioDurationSeconds
                )
            }
        }
        manager.setOnTrackChangedListener { trackId ->
            val audioList = getAudioFiles()
            val changedFile = audioList.find { it.id == trackId }
            if (changedFile != null && changedFile.id != _uiState.value.playingAudioFile?.id) {
                _uiState.update {
                    it.copy(
                        playingAudioFile = changedFile,
                        audioPositionSeconds = 0,
                        audioDurationSeconds = if (changedFile.durationSeconds > 0) changedFile.durationSeconds else 240
                    )
                }
            }
        }
    }

    init {
        AudioNotificationController.listener = object : AudioNotificationListener {
            override fun onPlayPause() {
                toggleAudioPlayPause()
            }

            override fun onPrevious() {
                prevAudioTrack()
            }

            override fun onNext() {
                nextAudioTrack(autoPlay = true)
            }

            override fun onStop() {
                stopAudioPlayback()
            }

            override fun onSeekTo(seconds: Int) {
                seekAudio(seconds)
            }
        }
    }

    private var videoTickerJob: kotlinx.coroutines.Job? = null

    private fun startVideoTicker() {
        videoTickerJob?.cancel()
        videoTickerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val vs = _uiState.value
                if (vs.isVideoPlaying && vs.playingVideoFile != null) {
                    val vstep = (1 * vs.videoPlaybackSpeed).toInt().coerceAtLeast(1)
                    val nextVPos = vs.videoPositionSeconds + vstep
                    if (nextVPos >= vs.videoDurationSeconds) {
                        _uiState.update {
                            it.copy(
                                isVideoPlaying = false,
                                videoPositionSeconds = vs.videoDurationSeconds
                            )
                        }
                        stopVideoTicker()
                        break
                    } else {
                        _uiState.update { it.copy(videoPositionSeconds = nextVPos) }
                    }
                } else {
                    break
                }
            }
        }
    }

    private fun stopVideoTicker() {
        videoTickerJob?.cancel()
        videoTickerJob = null
    }

    fun setTab(tab: MainTab) {
        _uiState.update { it.copy(currentTab = tab, selectedCategory = null, selectedStorageDevice = null) }
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.update { it.copy(language = lang, showLanguageDialog = false) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setAccentColor(accent: AccentColorType) {
        _uiState.update { it.copy(accentColor = accent) }
    }

    fun toggleShowHiddenFiles() {
        _uiState.update { it.copy(showHiddenFiles = !it.showHiddenFiles) }
    }

    fun toggleJunkAlert() {
        _uiState.update { it.copy(junkAlertEnabled = !it.junkAlertEnabled) }
    }

    fun setQuickShareDeviceName(name: String) {
        _uiState.update { it.copy(quickShareDeviceName = name) }
    }

    fun setQuickShareVisibility(isAll: Boolean) {
        _uiState.update { it.copy(quickShareVisibilityAll = isAll) }
    }

    fun toggleBackgroundMusic() {
        _uiState.update { it.copy(backgroundMusicPlayback = !it.backgroundMusicPlayback) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleGridView() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    fun toggleSortOrder() {
        _uiState.update {
            val next = when (it.sortOrder) {
                "DATE_DESC" -> "NAME_ASC"
                "NAME_ASC" -> "SIZE_DESC"
                else -> "DATE_DESC"
            }
            it.copy(sortOrder = next)
        }
    }

    fun openSettings() {
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun openFeatureList() {
        _uiState.update { it.copy(showFeatureList = true) }
    }

    fun closeFeatureList() {
        _uiState.update { it.copy(showFeatureList = false) }
    }

    fun openCategory(category: FileCategoryType) {
        _uiState.update { it.copy(selectedCategory = category, selectedStorageDevice = null) }
    }

    fun openStorageDevice(device: StorageDeviceInfo) {
        _uiState.update { it.copy(selectedStorageDevice = device, selectedCategory = null) }
    }

    fun closeSubScreen() {
        _uiState.update { it.copy(selectedCategory = null, selectedStorageDevice = null) }
    }

    fun openFileDetail(file: FileItem) {
        _uiState.update { it.copy(activeFileDetail = file) }
    }

    fun closeFileDetail() {
        _uiState.update { it.copy(activeFileDetail = null) }
    }

    // ================= MUSIC PLAYER CONTROLS (MP3) =================
    fun playAudio(file: FileItem, openPlayer: Boolean = true) {
        // Stop any video playback
        videoAudioEngine.stop()
        
        val audioList = getAudioFiles()
        val duration = if (file.durationSeconds > 0) file.durationSeconds else 240
        
        media3AudioManager?.setPlaybackSpeed(_uiState.value.audioPlaybackSpeed)
        media3AudioManager?.playPlaylist(
            playlist = if (audioList.isNotEmpty()) audioList else listOf(file),
            targetFile = file
        )

        _uiState.update {
            it.copy(
                playingAudioFile = file,
                isAudioPlaying = true,
                audioPositionSeconds = 0,
                audioDurationSeconds = duration,
                showFullAudioPlayer = openPlayer,
                isVideoPlaying = false,
                playingVideoFile = null,
                showFullVideoPlayer = false,
                activeFileDetail = null
            )
        }
    }

    fun toggleAudioPlayPause() {
        val current = _uiState.value.playingAudioFile
        if (current == null) {
            val audioFiles = getAudioFiles()
            if (audioFiles.isNotEmpty()) {
                playAudio(audioFiles.first(), openPlayer = true)
            }
        } else {
            media3AudioManager?.togglePlayPause()
        }
    }

    fun seekAudio(positionSeconds: Int) {
        val clamped = positionSeconds.coerceIn(0, _uiState.value.audioDurationSeconds)
        media3AudioManager?.seekTo(clamped)
        _uiState.update {
            it.copy(audioPositionSeconds = clamped)
        }
    }

    fun seekAudioRelative(deltaSeconds: Int) {
        val next = _uiState.value.audioPositionSeconds + deltaSeconds
        seekAudio(next)
    }

    fun nextAudioTrack(autoPlay: Boolean = true) {
        val audioList = getAudioFiles()
        if (audioList.isEmpty()) return

        val currentIndex = audioList.indexOfFirst { it.id == _uiState.value.playingAudioFile?.id }
        val nextTrack = if (_uiState.value.isAudioShuffle) {
            audioList.filter { it.id != _uiState.value.playingAudioFile?.id }.randomOrNull() ?: audioList.first()
        } else {
            val nextIndex = if (currentIndex in 0 until audioList.size - 1) currentIndex + 1 else 0
            audioList[nextIndex]
        }
        playAudio(nextTrack, openPlayer = _uiState.value.showFullAudioPlayer)
    }

    fun prevAudioTrack() {
        val audioList = getAudioFiles()
        if (audioList.isEmpty()) return

        if (_uiState.value.audioPositionSeconds > 3) {
            seekAudio(0)
            return
        }
        val currentIndex = audioList.indexOfFirst { it.id == _uiState.value.playingAudioFile?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else audioList.size - 1
        playAudio(audioList[prevIndex], openPlayer = _uiState.value.showFullAudioPlayer)
    }

    fun toggleAudioShuffle() {
        _uiState.update { it.copy(isAudioShuffle = !it.isAudioShuffle) }
    }

    fun cycleAudioRepeatMode() {
        _uiState.update {
            val nextMode = when (it.audioRepeatMode) {
                PlaybackRepeatMode.OFF -> PlaybackRepeatMode.REPEAT_ALL
                PlaybackRepeatMode.REPEAT_ALL -> PlaybackRepeatMode.REPEAT_ONE
                PlaybackRepeatMode.REPEAT_ONE -> PlaybackRepeatMode.OFF
            }
            it.copy(audioRepeatMode = nextMode)
        }
    }

    fun setAudioPlaybackSpeed(speed: Float) {
        media3AudioManager?.setPlaybackSpeed(speed)
        _uiState.update { it.copy(audioPlaybackSpeed = speed) }
    }

    fun setAudioEqualizerPreset(preset: String) {
        _uiState.update { it.copy(audioEqualizerPreset = preset) }
    }

    fun toggleAudioEqualizer(enabled: Boolean) {
        _uiState.update { it.copy(audioEqualizerEnabled = enabled) }
    }

    fun setAudioSleepTimer(minutes: Int) {
        _uiState.update { it.copy(audioSleepTimerMinutes = minutes) }
    }

    fun openFullAudioPlayer() {
        if (_uiState.value.playingAudioFile == null) {
            val audioFiles = getAudioFiles()
            if (audioFiles.isNotEmpty()) {
                playAudio(audioFiles.first(), openPlayer = true)
            }
        } else {
            _uiState.update { it.copy(showFullAudioPlayer = true) }
        }
    }

    fun closeFullAudioPlayer() {
        _uiState.update { it.copy(showFullAudioPlayer = false) }
    }

    fun stopAudioPlayback() {
        media3AudioManager?.stop()
        _uiState.update {
            it.copy(
                isAudioPlaying = false,
                playingAudioFile = null,
                showFullAudioPlayer = false,
                audioPositionSeconds = 0
            )
        }
    }

    fun getAudioFiles(): List<FileItem> {
        return _uiState.value.files.filter {
            (it.category == FileCategoryType.AUDIO || it.extension in listOf("mp3", "flac", "wav", "m4a", "ogg", "aac")) &&
                    !it.isInTrash && !it.isInSafeFolder
        }
    }

    // ================= VIDEO PLAYER CONTROLS (MP4) =================
    fun playVideo(file: FileItem) {
        // Stop music if playing
        media3AudioManager?.stop()
        videoAudioEngine.stop()
        
        val duration = if (file.durationSeconds > 0) file.durationSeconds else 180
        _uiState.update {
            it.copy(
                playingVideoFile = file,
                isVideoPlaying = true,
                videoPositionSeconds = 0,
                videoDurationSeconds = duration,
                showFullVideoPlayer = true,
                isAudioPlaying = false,
                playingAudioFile = null
            )
        }
        startVideoTicker()
    }

    fun toggleVideoPlayPause() {
        val willPlay = !_uiState.value.isVideoPlaying
        _uiState.update { it.copy(isVideoPlaying = willPlay) }
        if (willPlay) {
            startVideoTicker()
        } else {
            stopVideoTicker()
        }
    }

    fun stopVideoPlayback() {
        stopVideoTicker()
        videoAudioEngine.stop()
        _uiState.update {
            it.copy(
                isVideoPlaying = false,
                playingVideoFile = null,
                showFullVideoPlayer = false,
                videoPositionSeconds = 0
            )
        }
    }

    fun seekVideo(positionSeconds: Int) {
        val clamped = positionSeconds.coerceIn(0, _uiState.value.videoDurationSeconds)
        _uiState.update {
            it.copy(videoPositionSeconds = clamped)
        }
    }

    fun seekVideoRelative(deltaSeconds: Int) {
        val next = _uiState.value.videoPositionSeconds + deltaSeconds
        seekVideo(next)
    }

    fun nextVideoTrack() {
        val videoList = getVideoFiles()
        if (videoList.isEmpty()) return
        val currentIndex = videoList.indexOfFirst { it.id == _uiState.value.playingVideoFile?.id }
        val nextIndex = if (currentIndex in 0 until videoList.size - 1) currentIndex + 1 else 0
        playVideo(videoList[nextIndex])
    }

    fun prevVideoTrack() {
        val videoList = getVideoFiles()
        if (videoList.isEmpty()) return
        if (_uiState.value.videoPositionSeconds > 3) {
            seekVideo(0)
            return
        }
        val currentIndex = videoList.indexOfFirst { it.id == _uiState.value.playingVideoFile?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else videoList.size - 1
        playVideo(videoList[prevIndex])
    }

    fun setVideoPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(videoPlaybackSpeed = speed) }
    }

    fun cycleVideoAspectRatio() {
        _uiState.update {
            val nextRatio = when (it.videoAspectRatio) {
                VideoAspectRatioMode.FIT_SCREEN -> VideoAspectRatioMode.FILL_CROP
                VideoAspectRatioMode.FILL_CROP -> VideoAspectRatioMode.ORIGINAL_RATIO
                VideoAspectRatioMode.ORIGINAL_RATIO -> VideoAspectRatioMode.FIT_SCREEN
            }
            it.copy(videoAspectRatio = nextRatio)
        }
    }

    fun toggleVideoLock() {
        _uiState.update { it.copy(isVideoLocked = !it.isVideoLocked) }
    }

    fun setVideoVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _uiState.update { it.copy(videoVolume = clamped) }
    }

    fun setVideoBrightness(bri: Float) {
        _uiState.update { it.copy(videoBrightness = bri.coerceIn(0f, 1f)) }
    }

    fun toggleSubtitles() {
        _uiState.update { it.copy(showSubtitles = !it.showSubtitles) }
    }

    fun setSubtitleLanguage(lang: String) {
        _uiState.update { it.copy(selectedSubtitleLang = lang) }
    }

    fun openFullVideoPlayer() {
        if (_uiState.value.playingVideoFile == null) {
            val videoFiles = getVideoFiles()
            if (videoFiles.isNotEmpty()) {
                playVideo(videoFiles.first())
            }
        } else {
            _uiState.update { it.copy(showFullVideoPlayer = true) }
        }
    }

    fun closeVideoPlayer() {
        stopVideoTicker()
        videoAudioEngine.stop()
        _uiState.update {
            it.copy(
                showFullVideoPlayer = false,
                isVideoPlaying = false,
                playingVideoFile = null,
                videoPositionSeconds = 0
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopVideoTicker()
        videoAudioEngine.stop()
        media3AudioManager?.stop()
    }

    fun getVideoFiles(): List<FileItem> {
        return _uiState.value.files.filter {
            (it.category == FileCategoryType.VIDEOS || it.extension in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp")) &&
                    !it.isInTrash && !it.isInSafeFolder
        }
    }

    // Generic file click routing
    fun handleFileClick(file: FileItem) {
        val ext = file.extension.lowercase()
        when {
            file.category == FileCategoryType.AUDIO || ext in listOf("mp3", "flac", "wav", "m4a", "ogg", "aac") -> {
                playAudio(file, openPlayer = true)
            }
            file.category == FileCategoryType.VIDEOS || ext in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp") -> {
                playVideo(file)
            }
            file.category == FileCategoryType.IMAGES || ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg") -> {
                openImageViewer(file)
            }
            file.category == FileCategoryType.DOCUMENTS || ext in listOf("pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "csv") -> {
                openPdfViewer(file)
            }
            file.category == FileCategoryType.ARCHIVES || ext in listOf("zip", "7z", "tar", "gz", "bz2", "xz") -> {
                openArchiveViewer(file)
            }
            else -> {
                openFileDetail(file)
            }
        }
    }

    // ================= IMAGE & PHOTO VIEWER CONTROLS =================
    fun openImageViewer(file: FileItem) {
        _uiState.update {
            it.copy(
                activeImageFile = file,
                showImageViewer = true
            )
        }
    }

    fun closeImageViewer() {
        _uiState.update {
            it.copy(
                activeImageFile = null,
                showImageViewer = false
            )
        }
    }

    // ================= PDF & DOCUMENT VIEWER CONTROLS =================
    fun openPdfViewer(file: FileItem) {
        _uiState.update {
            it.copy(
                activePdfFile = file,
                showPdfViewer = true
            )
        }
    }

    fun closePdfViewer() {
        _uiState.update {
            it.copy(
                activePdfFile = null,
                showPdfViewer = false
            )
        }
    }

    // ================= ZIP & ARCHIVE CONTROLS =================
    fun openArchiveViewer(file: FileItem) {
        val entries = ArchiveEngine.inspectArchive(file)
        _uiState.update {
            it.copy(
                activeArchiveFile = file,
                archiveEntries = entries,
                showArchiveViewer = true
            )
        }
    }

    fun closeArchiveViewer() {
        _uiState.update {
            it.copy(
                showArchiveViewer = false,
                activeArchiveFile = null,
                archiveEntries = emptyList()
            )
        }
    }

    fun openArchiveCompressDialog(files: List<FileItem>? = null) {
        val targetFiles = files ?: _uiState.value.files.filter { !it.isInTrash && !it.isInSafeFolder }.take(3)
        _uiState.update {
            it.copy(
                selectedFilesForArchive = targetFiles,
                showArchiveCompressDialog = true
            )
        }
    }

    fun closeArchiveCompressDialog() {
        _uiState.update {
            it.copy(
                showArchiveCompressDialog = false,
                selectedFilesForArchive = emptyList()
            )
        }
    }

    fun compressFiles(
        name: String,
        format: ArchiveFormat,
        level: CompressionLevel,
        method: CompressionMethod,
        password: String?,
        encryptHeader: Boolean,
        split: SplitVolumeOption,
        deleteSource: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isArchiveProcessing = true) }
            delay(100)
            val selected = _uiState.value.selectedFilesForArchive
            val targetPath = "/storage/emulated/0/Download"
            val newArchive = ArchiveEngine.createArchive(
                archiveName = name,
                targetPath = targetPath,
                selectedFiles = selected,
                format = format,
                compressionLevel = level,
                compressionMethod = method,
                password = password,
                splitVolume = split
            )
            _uiState.update { state ->
                val remainingFiles = if (deleteSource) {
                    val idsToDelete = selected.map { it.id }.toSet()
                    state.files.filterNot { it.id in idsToDelete }
                } else {
                    state.files
                }
                val updatedFiles = listOf(newArchive) + remainingFiles
                state.copy(
                    files = updatedFiles,
                    isArchiveProcessing = false,
                    showArchiveCompressDialog = false,
                    selectedFilesForArchive = emptyList(),
                    cleanSuccessMessage = if (state.language == AppLanguage.HINDI)
                        "${format.displayName} आर्काइव '${newArchive.name}' (${newArchive.formattedSize}) सफलतापूर्वक बनाया गया!"
                    else
                        "${format.displayName} archive '${newArchive.name}' (${newArchive.formattedSize}) created successfully!"
                )
            }
        }
    }

    fun openArchiveExtractDialog(file: FileItem? = null) {
        val target = file ?: _uiState.value.activeArchiveFile ?: getArchiveFiles().firstOrNull()
        if (target != null) {
            _uiState.update {
                it.copy(
                    activeArchiveFile = target,
                    showArchiveExtractDialog = true
                )
            }
        }
    }

    fun closeArchiveExtractDialog() {
        _uiState.update {
            it.copy(showArchiveExtractDialog = false)
        }
    }

    fun extractArchive(
        destinationPath: String,
        password: String? = null,
        createSubfolder: Boolean = true
    ) {
        val targetArchive = _uiState.value.activeArchiveFile ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isArchiveProcessing = true) }
            delay(100)
            val finalDir = if (createSubfolder) {
                val folderName = targetArchive.name.substringBeforeLast(".")
                if (destinationPath.endsWith("/")) "$destinationPath$folderName" else "$destinationPath/$folderName"
            } else {
                destinationPath
            }
            val extractedFiles = ArchiveEngine.extractArchive(targetArchive, finalDir, password)
            _uiState.update { state ->
                state.copy(
                    files = extractedFiles + state.files,
                    isArchiveProcessing = false,
                    showArchiveExtractDialog = false,
                    showArchiveViewer = false,
                    cleanSuccessMessage = if (state.language == AppLanguage.HINDI)
                        "${extractedFiles.size} फाइलें '${finalDir.substringAfterLast("/")}' में निकाली गईं!"
                    else
                        "Extracted ${extractedFiles.size} files into '${finalDir.substringAfterLast("/")}' successfully!"
                )
            }
        }
    }

    fun testArchiveIntegrity(file: FileItem? = null) {
        val target = file ?: _uiState.value.activeArchiveFile ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isArchiveProcessing = true) }
            delay(50)
            val result = ArchiveEngine.testArchiveIntegrity(target)
            _uiState.update {
                it.copy(
                    isArchiveProcessing = false,
                    archiveTestResult = result,
                    showArchiveTestResultDialog = true
                )
            }
        }
    }

    fun closeArchiveTestResult() {
        _uiState.update {
            it.copy(
                showArchiveTestResultDialog = false,
                archiveTestResult = null
            )
        }
    }

    fun getArchiveFiles(): List<FileItem> {
        return _uiState.value.files.filter {
            (it.category == FileCategoryType.ARCHIVES || it.extension in listOf("zip", "7z", "tar", "gz", "bz2", "xz")) &&
                    !it.isInTrash && !it.isInSafeFolder
        }
    }

    // --- File & Folder Copy / Move / Paste Operations ---

    fun openDestinationPicker(items: List<ClipboardItem>, action: ClipboardOperationType) {
        _uiState.update {
            it.copy(
                showFolderDestinationPicker = true,
                pendingTransferAction = action,
                pendingTransferItems = items
            )
        }
    }

    fun closeDestinationPicker() {
        _uiState.update {
            it.copy(
                showFolderDestinationPicker = false,
                pendingTransferAction = null,
                pendingTransferItems = emptyList()
            )
        }
    }

    fun setClipboard(items: List<ClipboardItem>, action: ClipboardOperationType) {
        _uiState.update {
            it.copy(
                clipboardState = ClipboardState(
                    action = action,
                    items = items,
                    isActive = true
                )
            )
        }
    }

    fun clearClipboard() {
        _uiState.update {
            it.copy(
                clipboardState = ClipboardState(isActive = false)
            )
        }
    }

    fun clearTransferToast() {
        _uiState.update { it.copy(fileTransferSuccessToast = null) }
    }

    private var activeTransferJob: kotlinx.coroutines.Job? = null
    private var currentTransferSpeedMultiplier: Int = 1
    private var isTransferCancelledState: Boolean = false

    fun setTransferSpeedMultiplier(multiplier: Int) {
        currentTransferSpeedMultiplier = multiplier.coerceIn(1, 10)
        val mbps = multiplier * 25
        _uiState.update {
            it.copy(
                transferProgressState = it.transferProgressState.copy(
                    speedMultiplier = currentTransferSpeedMultiplier,
                    targetMbps = mbps
                )
            )
        }
    }

    fun setTransferTargetMbps(targetMbps: Int) {
        val safeMbps = targetMbps.coerceIn(5, 500)
        val mult = (safeMbps / 25).coerceIn(1, 10)
        currentTransferSpeedMultiplier = mult
        _uiState.update {
            it.copy(
                transferProgressState = it.transferProgressState.copy(
                    targetMbps = safeMbps,
                    speedMultiplier = mult
                )
            )
        }
    }

    fun toggleTransferPause() {
        _uiState.update {
            val curr = it.transferProgressState
            it.copy(transferProgressState = curr.copy(isPaused = !curr.isPaused))
        }
    }

    fun dismissTransferDoneModal() {
        _uiState.update {
            it.copy(
                transferProgressState = TransferProgressState(isTransferring = false)
            )
        }
    }

    fun cancelTransfer() {
        isTransferCancelledState = true
        activeTransferJob?.cancel()
        _uiState.update {
            it.copy(
                transferProgressState = TransferProgressState(isTransferring = false)
            )
        }
    }

    fun executeTransfer(
        items: List<ClipboardItem>,
        destinationPath: String,
        action: ClipboardOperationType,
        targetSpeedMbps: Int = 35
    ) {
        activeTransferJob?.cancel()
        isTransferCancelledState = false

        val mult = (targetSpeedMbps / 25).coerceIn(1, 10)
        currentTransferSpeedMultiplier = mult

        val sourceName = if (items.firstOrNull()?.path?.contains("emulated") == true) "Internal Storage" else "SD Card"
        val destName = if (destinationPath.contains("emulated") || destinationPath.startsWith("/storage/emulated/0")) "Internal Storage" else "SD Card"

        _uiState.update {
            it.copy(
                showFolderDestinationPicker = false,
                transferProgressState = TransferProgressState(
                    isTransferring = true,
                    action = action,
                    totalFilesCount = items.size,
                    currentFileIndex = 1,
                    speedMultiplier = mult,
                    targetMbps = targetSpeedMbps,
                    sourceLocationName = sourceName,
                    destinationLocationName = destName
                )
            )
        }

        activeTransferJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val result = com.example.storage.FileOperationsEngine.executeTransferWithProgress(
                items = items,
                destinationPath = destinationPath,
                action = action,
                currentFiles = _uiState.value.files,
                getSpeedMultiplier = { currentTransferSpeedMultiplier },
                checkIsCancelled = { isTransferCancelledState },
                onProgressUpdate = { progressState ->
                    _uiState.update {
                        it.copy(
                            transferProgressState = progressState.copy(
                                targetMbps = targetSpeedMbps,
                                sourceLocationName = sourceName,
                                destinationLocationName = destName
                            )
                        )
                    }
                }
            )

            _uiState.update {
                it.copy(
                    files = result.updatedFiles,
                    showFolderDestinationPicker = false,
                    pendingTransferAction = null,
                    pendingTransferItems = emptyList(),
                    clipboardState = if (action == ClipboardOperationType.MOVE) ClipboardState(isActive = false) else it.clipboardState,
                    transferProgressState = it.transferProgressState.copy(
                        isTransferring = true,
                        progress = 1.0f,
                        bytesTransferred = it.transferProgressState.totalBytesToTransfer.coerceAtLeast(1024L),
                        speedFormatted = "0.0 MB/s",
                        estimatedTimeRemainingSec = 0L
                    ),
                    fileTransferSuccessToast = if (it.language == AppLanguage.HINDI) result.messageHi else result.messageEn
                )
            }
        }
    }


    fun pasteClipboardTo(destinationPath: String) {
        val clip = _uiState.value.clipboardState
        if (!clip.isActive || clip.items.isEmpty()) return
        executeTransfer(clip.items, destinationPath, clip.action)
    }

    fun toggleStarred(fileId: String) {
        _uiState.update { state ->
            val updated = state.files.map {
                if (it.id == fileId) it.copy(isStarred = !it.isStarred) else it
            }
            state.copy(files = updated)
        }
    }

    fun moveToTrash(fileId: String) {
        _uiState.update { state ->
            val updated = state.files.map {
                if (it.id == fileId) it.copy(isInTrash = true) else it
            }
            state.copy(files = updated, activeFileDetail = null)
        }
    }

    fun restoreFromTrash(fileId: String) {
        _uiState.update { state ->
            val updated = state.files.map {
                if (it.id == fileId) it.copy(isInTrash = false) else it
            }
            state.copy(files = updated)
        }
    }

    fun emptyTrash() {
        _uiState.update { state ->
            val updated = state.files.filterNot { it.isInTrash }
            state.copy(files = updated)
        }
    }

    fun moveToSafeFolder(fileId: String) {
        _uiState.update { state ->
            val updated = state.files.map {
                if (it.id == fileId) it.copy(isInSafeFolder = true) else it
            }
            state.copy(files = updated, activeFileDetail = null)
        }
    }

    fun removeFromSafeFolder(fileId: String) {
        _uiState.update { state ->
            val updated = state.files.map {
                if (it.id == fileId) it.copy(isInSafeFolder = false) else it
            }
            state.copy(files = updated)
        }
    }

    fun verifyPin(pin: String): Boolean {
        if (pin == _uiState.value.safeFolderPin) {
            _uiState.update { it.copy(isSafeFolderUnlocked = true) }
            return true
        }
        return false
    }

    fun verifyRecoveryAnswer(answer: String): Boolean {
        val trimmed = answer.trim().lowercase()
        if (trimmed == "delhi" || trimmed == "1234" || trimmed == "akash" || trimmed == _uiState.value.safeFolderPin.lowercase()) {
            _uiState.update { it.copy(isSafeFolderUnlocked = true) }
            return true
        }
        return false
    }

    fun updatePin(newPin: String) {
        if (newPin.length in 4..8) {
            _uiState.update { it.copy(safeFolderPin = newPin, showPinChangeDialog = false) }
        }
    }

    fun lockSafeFolder() {
        _uiState.update { it.copy(isSafeFolderUnlocked = false) }
    }

    fun openSafeFolderDialog() {
        _uiState.update { it.copy(showSafeFolderDialog = true) }
    }

    fun closeSafeFolderDialog() {
        _uiState.update { it.copy(showSafeFolderDialog = false, isSafeFolderUnlocked = false) }
    }

    fun openTrashDialog() {
        _uiState.update { it.copy(showTrashDialog = true) }
    }

    fun closeTrashDialog() {
        _uiState.update { it.copy(showTrashDialog = false) }
    }

    fun openStorageBreakdown() {
        _uiState.update { it.copy(showStorageBreakdown = true) }
    }

    fun closeStorageBreakdown() {
        _uiState.update { it.copy(showStorageBreakdown = false) }
    }

    fun openLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = true) }
    }

    fun closeLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    fun openSignInDialog() {
        _uiState.update { it.copy(showSignInDialog = true) }
    }

    fun closeSignInDialog() {
        _uiState.update { it.copy(showSignInDialog = false) }
    }

    fun openPrivacyDialog() {
        _uiState.update { it.copy(showPrivacyDialog = true) }
    }

    fun closePrivacyDialog() {
        _uiState.update { it.copy(showPrivacyDialog = false) }
    }

    fun openTermsDialog() {
        _uiState.update { it.copy(showTermsDialog = true) }
    }

    fun closeTermsDialog() {
        _uiState.update { it.copy(showTermsDialog = false) }
    }

    fun openFeedbackDialog() {
        _uiState.update { it.copy(showFeedbackDialog = true) }
    }

    fun closeFeedbackDialog() {
        _uiState.update { it.copy(showFeedbackDialog = false) }
    }

    fun openPinChangeDialog() {
        _uiState.update { it.copy(showPinChangeDialog = true) }
    }

    fun closePinChangeDialog() {
        _uiState.update { it.copy(showPinChangeDialog = false) }
    }

    fun openRecoveryDialog() {
        _uiState.update { it.copy(showRecoveryDialog = true) }
    }

    fun closeRecoveryDialog() {
        _uiState.update { it.copy(showRecoveryDialog = false) }
    }

    fun cleanJunkItem(itemId: String) {
        val item = _uiState.value.junkItems.find { it.id == itemId }
        val freedSize = item?.formattedSize ?: "120 MB"
        _uiState.update { state ->
            val updated = state.junkItems.filterNot { it.id == itemId }
            state.copy(
                junkItems = updated,
                cleanSuccessMessage = if (state.language == AppLanguage.HINDI)
                    "$freedSize जंक साफ किया गया!"
                else
                    "Successfully cleaned $freedSize of junk!"
            )
        }
    }

    fun cleanAllJunk() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCleaningInProgress = true) }
            delay(100)
            _uiState.update { state ->
                state.copy(
                    junkItems = emptyList(),
                    isCleaningInProgress = false,
                    cleanSuccessMessage = if (state.language == AppLanguage.HINDI)
                        "1.8 GB जंक और कैश फाइलें सफलतापूर्वक साफ की गईं!"
                    else
                        "Cleaned 1.8 GB of junk files and cache successfully!"
                )
            }
        }
    }

    fun clearAppCache() {
        _uiState.update { state ->
            state.copy(
                cleanSuccessMessage = if (state.language == AppLanguage.HINDI)
                    "एप का कैश और थंबनेल इंडेक्स साफ हो गया!"
                else
                    "App cache and thumbnail index cleared successfully!"
            )
        }
    }

    fun resetDefaultSettings() {
        _uiState.update { state ->
            state.copy(
                language = AppLanguage.HINDI,
                themeMode = ThemeMode.DARK,
                accentColor = AccentColorType.EMERALD,
                showHiddenFiles = false,
                junkAlertEnabled = true,
                quickShareDeviceName = "Akash's Android Device",
                quickShareVisibilityAll = true,
                backgroundMusicPlayback = true,
                isUltraBatterySaver = true,
                cleanSuccessMessage = if (state.language == AppLanguage.HINDI)
                    "सेटिंग्स रीसेट हो गईं!"
                else
                    "Settings reset to default values"
            )
        }
    }

    fun loadSampleFiles() {
        _uiState.update { state ->
            state.copy(
                files = defaultInitialFiles(),
                cleanSuccessMessage = if (state.language == AppLanguage.HINDI)
                    "सैंपल फाइलें लोड हो गईं!"
                else
                    "Sample files loaded successfully!"
            )
        }
    }

    fun refreshDevices() {
        viewModelScope.launch {
            delay(50)
            _uiState.update { state ->
                state.copy(
                    storageDevices = defaultStorageDevices(),
                    cleanSuccessMessage = if (state.language == AppLanguage.HINDI)
                        "स्टोरेज डिवाइसेस रिफ्रेश हो गईं"
                    else
                        "Storage devices refreshed"
                )
            }
        }
    }

    fun refreshRealStorage(context: Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val realDevices = StorageScanner.getRealStorageDevices(context)
            val realFiles = StorageScanner.scanRealStorageFiles(context)
            _uiState.update { state ->
                val updatedFiles = if (realFiles.isNotEmpty()) {
                    (realFiles + state.files).distinctBy { it.path }
                } else {
                    state.files
                }
                state.copy(
                    storageDevices = realDevices,
                    files = updatedFiles
                )
            }
        }
    }

    fun dismissCleanSnackbar() {
        _uiState.update { it.copy(cleanSuccessMessage = null) }
    }

    fun setGithubRepoPath(repo: String) {
        _uiState.update { it.copy(githubRepoPath = repo) }
    }

    fun setUpdateDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showUpdateDialog = visible) }
    }

    fun triggerCheckForUpdates(context: Context, showIfNoUpdate: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingForUpdates = true, updateResult = null) }
            val result = AppUpdateManager.checkUpdate(context, _uiState.value.githubRepoPath)
            _uiState.update { state ->
                state.copy(
                    isCheckingForUpdates = false,
                    updateResult = result,
                    showUpdateDialog = when (result) {
                        is UpdateResult.Success -> result.updateAvailable || showIfNoUpdate
                        is UpdateResult.Error -> showIfNoUpdate
                        is UpdateResult.NoUpdate -> showIfNoUpdate
                    }
                )
            }
        }
    }

    fun getCategoryCount(category: FileCategoryType): Int {
        return _uiState.value.files.count { it.category == category && !it.isInSafeFolder && !it.isInTrash }
    }
}

fun defaultStorageDevices(): List<StorageDeviceInfo> {
    val internalPath = try {
        android.os.Environment.getExternalStorageDirectory().absolutePath
    } catch (e: Exception) {
        "/storage/emulated/0"
    }
    return listOf(
        StorageDeviceInfo(
            id = "internal_storage",
            nameEn = "Internal Storage",
            nameHi = "आंतरिक संग्रहण",
            freeBytes = 205600000000L,
            totalBytes = 225000000000L,
            isExternal = false,
            usedPercent = 8,
            rootPath = internalPath
        ),
        StorageDeviceInfo(
            id = "sd_card",
            nameEn = "SD Card (Memory Card)",
            nameHi = "एसडी कार्ड (मेमोरी कार्ड)",
            freeBytes = 78600000000L,
            totalBytes = 119000000000L,
            isExternal = true,
            badge = "External",
            usedPercent = 34,
            rootPath = "/storage/sdcard"
        )
    )
}

fun defaultJunkItems(): List<CleanJunkItem> {
    return listOf(
        CleanJunkItem(
            id = "junk_cache",
            titleEn = "Junk & Cache Files",
            titleHi = "जंक और कैश फाइलें",
            descEn = "Cached thumbnails and obsolete app temp data",
            descHi = "अस्थायी थंबनेल और पुराना एप डेटा",
            sizeBytes = 854000000L
        ),
        CleanJunkItem(
            id = "junk_duplicates",
            titleEn = "Duplicate Files",
            titleHi = "डुप्लिकेट फाइलें",
            descEn = "Identical photos and downloads saving duplicate space",
            descHi = "एक जैसी फोटो और डाउनलोड्स स्थान घेर रहे हैं",
            sizeBytes = 540000000L
        ),
        CleanJunkItem(
            id = "junk_old_downloads",
            titleEn = "Old Large Downloads",
            titleHi = "पुराने बड़े डाउनलोड्स",
            descEn = "Files not opened in over 60 days",
            descHi = "60 दिनों से अधिक समय से न खोली गई फाइलें",
            sizeBytes = 412000000L
        )
    )
}

fun defaultInitialFiles(): List<FileItem> {
    return listOf(
        // Audio / Songs (MP3 / FLAC)
        FileItem(
            id = "aud_kesariya",
            name = "Kesariya - Brahmastra.mp3",
            path = "/storage/emulated/0/Music/Bollywood/Kesariya - Brahmastra.mp3",
            sizeBytes = 10400000L,
            category = FileCategoryType.AUDIO,
            extension = "mp3",
            isRecent = true,
            isStarred = true,
            durationText = "04:28",
            durationSeconds = 268,
            artist = "Arijit Singh, Pritam",
            album = "Brahmastra (Original Soundtrack)"
        ),
        FileItem(
            id = "aud_chaleya",
            name = "Chaleya - Jawan.mp3",
            path = "/storage/emulated/0/Music/Bollywood/Chaleya - Jawan.mp3",
            sizeBytes = 8200000L,
            category = FileCategoryType.AUDIO,
            extension = "mp3",
            isRecent = true,
            durationText = "03:20",
            durationSeconds = 200,
            artist = "Arijit Singh, Shilpa Rao, Anirudh",
            album = "Jawan (Hindi Hits)"
        ),
        FileItem(
            id = "aud_tumhiho",
            name = "Tum Hi Ho - Aashiqui 2.mp3",
            path = "/storage/emulated/0/Music/Bollywood/Tum Hi Ho - Aashiqui 2.mp3",
            sizeBytes = 9800000L,
            category = FileCategoryType.AUDIO,
            extension = "mp3",
            durationText = "04:22",
            durationSeconds = 262,
            artist = "Arijit Singh, Mithoon",
            album = "Aashiqui 2"
        ),
        FileItem(
            id = "aud_acoustic",
            name = "Favorite_Acoustic_Guitar.mp3",
            path = "/storage/emulated/0/Music/Favorite_Acoustic_Guitar.mp3",
            sizeBytes = 8900000L,
            category = FileCategoryType.AUDIO,
            extension = "mp3",
            durationText = "04:15",
            durationSeconds = 255,
            artist = "Akash Studio Sessions",
            album = "Acoustic Unplugged Vol. 1"
        ),
        FileItem(
            id = "aud_lofi",
            name = "Study_Lofi_Ambient_Mix.flac",
            path = "/storage/emulated/0/Music/Study_Lofi_Ambient_Mix.flac",
            sizeBytes = 42000000L,
            category = FileCategoryType.AUDIO,
            extension = "flac",
            durationText = "08:30",
            durationSeconds = 510,
            artist = "ChillHop & Relax Station",
            album = "Deep Focus 2024"
        ),
        FileItem(
            id = "rec_4",
            name = "Voice_Note_Akash_Project.mp3",
            path = "/storage/emulated/0/Audio/Voice_Note_Akash_Project.mp3",
            sizeBytes = 4500000L,
            category = FileCategoryType.AUDIO,
            extension = "mp3",
            isRecent = true,
            durationText = "03:12",
            durationSeconds = 192,
            artist = "Akash Voice Recorder",
            album = "Voice Recordings"
        ),
        // Videos (MP4 / MKV)
        FileItem(
            id = "vid_dance",
            name = "Bollywood_Dance_Video_1080p.mp4",
            path = "/storage/emulated/0/Movies/Bollywood_Dance_Video_1080p.mp4",
            sizeBytes = 185000000L,
            category = FileCategoryType.VIDEOS,
            extension = "mp4",
            isRecent = true,
            isStarred = true,
            durationText = "03:45",
            durationSeconds = 225,
            artist = "HD Music Studio",
            album = "Dance Hits 2024"
        ),
        FileItem(
            id = "vid_1",
            name = "Tutorial_FileManager_Review.mp4",
            path = "/storage/emulated/0/Movies/Tutorial_FileManager_Review.mp4",
            sizeBytes = 1850000000L,
            category = FileCategoryType.VIDEOS,
            extension = "mp4",
            durationText = "24:10",
            durationSeconds = 1450,
            artist = "Android Developers",
            album = "Tech Tutorials"
        ),
        FileItem(
            id = "vid_nature",
            name = "4K_Ultra_HD_Nature_Landscape.mkv",
            path = "/storage/emulated/0/Movies/4K_Ultra_HD_Nature_Landscape.mkv",
            sizeBytes = 2100000000L,
            category = FileCategoryType.VIDEOS,
            extension = "mkv",
            durationText = "15:40",
            durationSeconds = 940,
            artist = "Earth Vision 4K",
            album = "Wildlife & Landscapes"
        ),
        FileItem(
            id = "rec_1",
            name = "Screen_Recording_2024.mp4",
            path = "/storage/emulated/0/DCIM/Screen_Recording_2024.mp4",
            sizeBytes = 34000000L,
            category = FileCategoryType.VIDEOS,
            extension = "mp4",
            isRecent = true,
            durationText = "00:45",
            durationSeconds = 45
        ),
        FileItem(
            id = "rec_2",
            name = "Screen_Recording_Demo.mp4",
            path = "/storage/emulated/0/DCIM/Screen_Recording_Demo.mp4",
            sizeBytes = 63000000L,
            category = FileCategoryType.VIDEOS,
            isRecent = true,
            extension = "mp4",
            durationText = "02:18",
            durationSeconds = 138
        ),
        // Images & Photos
        FileItem(
            id = "rec_3",
            name = "IMG_Akash_Project_Preview.jpg",
            path = "/storage/emulated/0/Pictures/IMG_Akash_Project_Preview.jpg",
            sizeBytes = 2800000L,
            category = FileCategoryType.IMAGES,
            extension = "jpg",
            isRecent = true
        ),
        FileItem(
            id = "img_1",
            name = "Camera_Sunset_HD.png",
            path = "/storage/emulated/0/Pictures/Camera_Sunset_HD.png",
            sizeBytes = 8400000L,
            category = FileCategoryType.IMAGES,
            extension = "png"
        ),
        FileItem(
            id = "img_2",
            name = "Screenshot_2024_09.png",
            path = "/storage/emulated/0/Pictures/Screenshots/Screenshot_2024_09.png",
            sizeBytes = 1200000L,
            category = FileCategoryType.IMAGES,
            extension = "png"
        ),
        // Downloads & Documents
        FileItem(
            id = "down_1",
            name = "Project_Report_Final.pdf",
            path = "/storage/emulated/0/Download/Project_Report_Final.pdf",
            sizeBytes = 24500000L,
            category = FileCategoryType.DOWNLOADS,
            extension = "pdf"
        ),
        FileItem(
            id = "down_2",
            name = "Setup_Installer_v2.apk",
            path = "/storage/emulated/0/Download/Setup_Installer_v2.apk",
            sizeBytes = 48900000L,
            category = FileCategoryType.DOWNLOADS,
            extension = "apk"
        ),
        FileItem(
            id = "down_3",
            name = "Archive_Backup_2024.zip",
            path = "/storage/emulated/0/Download/Archive_Backup_2024.zip",
            sizeBytes = 24300000L,
            category = FileCategoryType.ARCHIVES,
            extension = "zip",
            isRecent = true
        ),
        FileItem(
            id = "arch_zip_2",
            name = "Project_Source_Code_v2.zip",
            path = "/storage/emulated/0/Download/Project_Source_Code_v2.zip",
            sizeBytes = 48200000L,
            category = FileCategoryType.ARCHIVES,
            extension = "zip",
            isRecent = true
        ),
        FileItem(
            id = "arch_zip_3",
            name = "Photos_Vacation_Ultra.zip",
            path = "/storage/emulated/0/Download/Photos_Vacation_Ultra.zip",
            sizeBytes = 18500000L,
            category = FileCategoryType.ARCHIVES,
            extension = "zip"
        ),
        FileItem(
            id = "doc_1",
            name = "Resume_Akash_Kumar.pdf",
            path = "/storage/emulated/0/Documents/Resume_Akash_Kumar.pdf",
            sizeBytes = 2400000L,
            category = FileCategoryType.DOCUMENTS,
            extension = "pdf",
            isRecent = true
        ),
        FileItem(
            id = "doc_2",
            name = "Invoice_GST_Sept2026.pdf",
            path = "/storage/emulated/0/Documents/Invoice_GST_Sept2026.pdf",
            sizeBytes = 1800000L,
            category = FileCategoryType.DOCUMENTS,
            extension = "pdf",
            isRecent = true
        ),
        FileItem(
            id = "doc_3",
            name = "Aadhaar_Card_Copy.pdf",
            path = "/storage/emulated/0/Documents/Aadhaar_Card_Copy.pdf",
            sizeBytes = 850000L,
            category = FileCategoryType.DOCUMENTS,
            extension = "pdf"
        ),
        FileItem(
            id = "doc_4",
            name = "College_Notes_Unit1.pdf",
            path = "/storage/emulated/0/Documents/College_Notes_Unit1.pdf",
            sizeBytes = 4200000L,
            category = FileCategoryType.DOCUMENTS,
            extension = "pdf"
        ),
        FileItem(
            id = "doc_5",
            name = "Annual_Financial_Statement_2026.pdf",
            path = "/storage/emulated/0/Documents/Annual_Financial_Statement_2026.pdf",
            sizeBytes = 3800000L,
            category = FileCategoryType.DOCUMENTS,
            extension = "pdf"
        ),
        FileItem(
            id = "apk_1",
            name = "FileManagerPro_Update.apk",
            path = "/storage/emulated/0/Download/FileManagerPro_Update.apk",
            sizeBytes = 48900000L,
            category = FileCategoryType.APPS,
            extension = "apk"
        ),
        FileItem(
            id = "hidden_1",
            name = ".nomedia_cache_index",
            path = "/storage/emulated/0/.nomedia_cache_index",
            sizeBytes = 45000L,
            category = FileCategoryType.DOCUMENTS,
            extension = "index",
            isHidden = true
        ),
        // SD Card Specific Sample Files & Folders
        FileItem(
            id = "sd_img_1",
            name = "IMG_SDCard_Camera_001.jpg",
            path = "/storage/sdcard/DCIM/Camera/IMG_SDCard_Camera_001.jpg",
            sizeBytes = 4200000L,
            category = FileCategoryType.IMAGES,
            extension = "jpg"
        ),
        FileItem(
            id = "sd_vid_1",
            name = "VID_SDCard_Family_Trip.mp4",
            path = "/storage/sdcard/DCIM/Camera/VID_SDCard_Family_Trip.mp4",
            sizeBytes = 145000000L,
            category = FileCategoryType.VIDEOS,
            extension = "mp4",
            durationText = "05:12",
            durationSeconds = 312
        ),
        FileItem(
            id = "sd_aud_1",
            name = "Retro_Classic_Hits_NonStop.mp3",
            path = "/storage/sdcard/Music/Retro_Classic_Hits_NonStop.mp3",
            sizeBytes = 12800000L,
            category = FileCategoryType.AUDIO,
            extension = "mp3",
            durationText = "05:30",
            durationSeconds = 330,
            artist = "Kishore Kumar, Lata Mangeshkar",
            album = "Evergreen Retro Classics"
        ),
        FileItem(
            id = "sd_aud_2",
            name = "Devotional_Bhakti_Bhajan.mp3",
            path = "/storage/sdcard/Music/Devotional_Bhakti_Bhajan.mp3",
            sizeBytes = 9400000L,
            category = FileCategoryType.AUDIO,
            extension = "mp3",
            durationText = "04:10",
            durationSeconds = 250,
            artist = "Anup Jalota",
            album = "Morning Bhakti Melodies"
        ),
        FileItem(
            id = "sd_doc_1",
            name = "Bank_Statement_SDCard_2024.pdf",
            path = "/storage/sdcard/Documents/Bank_Statement_SDCard_2024.pdf",
            sizeBytes = 3200000L,
            category = FileCategoryType.DOCUMENTS,
            extension = "pdf"
        ),
        FileItem(
            id = "sd_zip_1",
            name = "SDCard_Full_Photos_Backup.zip",
            path = "/storage/sdcard/Backup_SDCard/SDCard_Full_Photos_Backup.zip",
            sizeBytes = 350000000L,
            category = FileCategoryType.ARCHIVES,
            extension = "zip"
        )
    )
}
