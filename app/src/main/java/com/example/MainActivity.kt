package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.modals.*
import com.example.ui.screens.*
import com.example.ui.theme.FilesTheme
import com.example.viewmodel.FilesUiState
import com.example.viewmodel.FilesViewModel
import kotlinx.coroutines.launch

enum class AppNavScreen {
    MAIN_TABS,
    CATEGORY_FILE_LIST,
    SETTINGS,
    FEATURE_LIST
}

class MainActivity : ComponentActivity() {

    private val viewModel: FilesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            var hasPermissionGranted by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                viewModel.refreshRealStorage(context)
            }

            FilesTheme(darkTheme = true) {
                StoragePermissionGate(
                    hasPermission = hasPermissionGranted,
                    language = uiState.language,
                    onPermissionGranted = {
                        hasPermissionGranted = true
                        viewModel.refreshRealStorage(context)
                    }
                ) {
                    MainAppContent(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun MainAppContent(
    uiState: FilesUiState,
    viewModel: FilesViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf(AppNavScreen.MAIN_TABS) }
    var activeCategoryFilter by remember { mutableStateOf<FileCategoryType?>(null) }
    var activeStorageDevice by remember { mutableStateOf<StorageDeviceInfo?>(null) }

    // Dialog & Modal Visibilities
    var showSafeFolderModal by remember { mutableStateOf(false) }
    var showTrashModal by remember { mutableStateOf(false) }
    var showStorageBreakdownModal by remember { mutableStateOf(false) }
    var showLanguageModal by remember { mutableStateOf(false) }
    var showPinChangeModal by remember { mutableStateOf(false) }
    var showLegalPoliciesModal by remember { mutableStateOf(false) }
    var legalPolicyType by remember { mutableStateOf(PolicyType.PRIVACY) }

    // Media Viewer Modals
    var activeMusicFile by remember { mutableStateOf<FileItem?>(null) }
    var activeVideoFile by remember { mutableStateOf<FileItem?>(null) }
    var activePdfFile by remember { mutableStateOf<FileItem?>(null) }
    var activeImageFile by remember { mutableStateOf<FileItem?>(null) }

    // Archive Modals
    var archiveToCompressFiles by remember { mutableStateOf<List<FileItem>?>(null) }
    var archiveToExtractFile by remember { mutableStateOf<FileItem?>(null) }
    var archiveToViewFile by remember { mutableStateOf<FileItem?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GoogleDrawer(
                currentTab = uiState.currentTab,
                language = uiState.language,
                onSelectTab = { tab ->
                    viewModel.setTab(tab)
                    currentScreen = AppNavScreen.MAIN_TABS
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenClean = {
                    viewModel.setTab(MainTab.CLEAN)
                    currentScreen = AppNavScreen.MAIN_TABS
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenBrowse = {
                    viewModel.setTab(MainTab.BROWSE)
                    currentScreen = AppNavScreen.MAIN_TABS
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenShare = {
                    viewModel.setTab(MainTab.SHARE)
                    currentScreen = AppNavScreen.MAIN_TABS
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenSafeFolder = {
                    showSafeFolderModal = true
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenTrash = {
                    showTrashModal = true
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenSettings = {
                    currentScreen = AppNavScreen.SETTINGS
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenHelp = {
                    currentScreen = AppNavScreen.FEATURE_LIST
                    coroutineScope.launch { drawerState.close() }
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (currentScreen == AppNavScreen.MAIN_TABS) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        GoogleHeader(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onMenuClick = { coroutineScope.launch { drawerState.open() } },
                            isGridView = uiState.isGridView,
                            onToggleGridView = { viewModel.toggleGridView() },
                            onToggleSort = { viewModel.toggleSortOrder() },
                            onOpenFilter = {},
                            onProfileClick = { showStorageBreakdownModal = true },
                            language = uiState.language
                        )
                    }
                }
            },
            bottomBar = {
                Column {
                    // Mini Player Bar if media playing
                    if (uiState.playingAudioFile != null) {
                        MiniPlayerBar(
                            playingFile = uiState.playingAudioFile!!,
                            isPlaying = uiState.isAudioPlaying,
                            positionSeconds = uiState.audioPositionSeconds,
                            durationSeconds = uiState.audioDurationSeconds,
                            onTogglePlayPause = { viewModel.toggleAudioPlayPause() },
                            onNext = { viewModel.nextAudioTrack() },
                            onOpenFullPlayer = { activeMusicFile = uiState.playingAudioFile },
                            onClose = { viewModel.stopAudioPlayback() }
                        )
                    }

                    if (currentScreen == AppNavScreen.MAIN_TABS) {
                        BottomNavBar(
                            selectedTab = uiState.currentTab,
                            language = uiState.language,
                            onTabSelected = { viewModel.setTab(it) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppNavScreen.MAIN_TABS -> {
                        when (uiState.currentTab) {
                            MainTab.CLEAN -> {
                                val totalJunkBytes = uiState.junkItems.sumOf { it.sizeBytes }
                                CleanScreen(
                                    junkSizeText = formatFileSize(totalJunkBytes),
                                    junkCategories = uiState.junkItems,
                                    isCleaning = uiState.isCleaningInProgress,
                                    cleanProgress = 0.85f,
                                    cleanedBytes = totalJunkBytes,
                                    language = uiState.language,
                                    onStartClean = { viewModel.cleanAllJunk() },
                                    onCleanJunkItem = { viewModel.cleanJunkItem(it) }
                                )
                            }
                            MainTab.BROWSE -> {
                                val recents = uiState.files.filter { it.isRecent && !it.isInTrash && !it.isInSafeFolder }
                                BrowseScreen(
                                    recentFiles = recents,
                                    storageDevices = uiState.storageDevices,
                                    language = uiState.language,
                                    onCategoryClick = { category ->
                                        activeCategoryFilter = category
                                        activeStorageDevice = null
                                        currentScreen = AppNavScreen.CATEGORY_FILE_LIST
                                    },
                                    onDeviceClick = { device ->
                                        activeCategoryFilter = null
                                        activeStorageDevice = device
                                        currentScreen = AppNavScreen.CATEGORY_FILE_LIST
                                    },
                                    onRecentFileClick = { file ->
                                        handleOpenFile(
                                            file = file,
                                            viewModel = viewModel,
                                            onOpenMusic = { activeMusicFile = it },
                                            onOpenVideo = { activeVideoFile = it },
                                            onOpenPdf = { activePdfFile = it },
                                            onOpenImage = { activeImageFile = it },
                                            onOpenArchive = { archiveToViewFile = it }
                                        )
                                    },
                                    onOpenMusicPlayer = {
                                        val firstAudio = viewModel.getAudioFiles().firstOrNull()
                                        if (firstAudio != null) {
                                            viewModel.playAudio(firstAudio, openPlayer = true)
                                            activeMusicFile = firstAudio
                                        }
                                    },
                                    onOpenVideoPlayer = {
                                        val firstVideo = viewModel.getVideoFiles().firstOrNull()
                                        if (firstVideo != null) {
                                            viewModel.playVideo(firstVideo)
                                            activeVideoFile = firstVideo
                                        }
                                    },
                                    onOpenCompressDialog = {
                                        archiveToCompressFiles = uiState.files.filter { !it.isInTrash && !it.isInSafeFolder }.take(5)
                                    },
                                    onOpenSafeFolder = { showSafeFolderModal = true },
                                    onOpenStarred = {
                                        activeCategoryFilter = null
                                        activeStorageDevice = null
                                        currentScreen = AppNavScreen.CATEGORY_FILE_LIST
                                    },
                                    onOpenTrash = { showTrashModal = true },
                                    onOpenFeatureList = { currentScreen = AppNavScreen.FEATURE_LIST },
                                    onRefreshDevices = { viewModel.refreshRealStorage(context) },
                                    getCategoryText = { cat ->
                                        val count = viewModel.getCategoryCount(cat)
                                        "${count} ${if (uiState.language == AppLanguage.HINDI) "फाइलें" else "files"}"
                                    }
                                )
                            }
                            MainTab.SHARE -> {
                                ShareScreen(
                                    language = uiState.language,
                                    onSendFilesClick = {},
                                    onReceiveFilesClick = {}
                                )
                            }
                        }
                    }
                    AppNavScreen.CATEGORY_FILE_LIST -> {
                        CategoryFileListScreen(
                            category = activeCategoryFilter,
                            storageDevice = activeStorageDevice,
                            files = uiState.files,
                            language = uiState.language,
                            onBack = { currentScreen = AppNavScreen.MAIN_TABS },
                            onFileClick = { file ->
                                handleOpenFile(
                                    file = file,
                                    viewModel = viewModel,
                                    onOpenMusic = { activeMusicFile = it },
                                    onOpenVideo = { activeVideoFile = it },
                                    onOpenPdf = { activePdfFile = it },
                                    onOpenImage = { activeImageFile = it },
                                    onOpenArchive = { archiveToViewFile = it }
                                )
                            },
                            onToggleStar = { viewModel.toggleStarred(it) },
                            onMoveToTrash = { viewModel.moveToTrash(it) },
                            onMoveToSafeFolder = { viewModel.moveToSafeFolder(it) },
                            onCompressFiles = { filesList ->
                                archiveToCompressFiles = filesList
                            }
                        )
                    }
                    AppNavScreen.SETTINGS -> {
                        SettingsScreen(
                            currentLanguage = uiState.language,
                            onSelectLanguage = { viewModel.setLanguage(it) },
                            onChangeSafeFolderPin = { showPinChangeModal = true },
                            onOpenStorageBreakdown = { showStorageBreakdownModal = true },
                            onOpenPrivacyPolicy = {
                                legalPolicyType = PolicyType.PRIVACY
                                showLegalPoliciesModal = true
                            },
                            onOpenTerms = {
                                legalPolicyType = PolicyType.TERMS
                                showLegalPoliciesModal = true
                            },
                            onBack = { currentScreen = AppNavScreen.MAIN_TABS }
                        )
                    }
                    AppNavScreen.FEATURE_LIST -> {
                        FeatureListScreen(
                            language = uiState.language,
                            onBack = { currentScreen = AppNavScreen.MAIN_TABS }
                        )
                    }
                }
            }
        }
    }

    // Modal Dialogs Rendering
    if (showSafeFolderModal) {
        SafeFolderDialog(
            isUnlocked = uiState.isSafeFolderUnlocked,
            files = uiState.files,
            language = uiState.language,
            onVerifyPin = { viewModel.verifyPin(it) },
            onVerifyRecovery = { viewModel.verifyRecoveryAnswer(it) },
            onSaveNewPin = { viewModel.updatePin(it) },
            onRemoveFromSafeFolder = { viewModel.removeFromSafeFolder(it) },
            onDismiss = {
                showSafeFolderModal = false
                viewModel.lockSafeFolder()
            }
        )
    }

    if (showTrashModal) {
        TrashDialog(
            files = uiState.files,
            language = uiState.language,
            onRestore = { viewModel.restoreFromTrash(it) },
            onEmptyTrash = { viewModel.emptyTrash() },
            onDismiss = { showTrashModal = false }
        )
    }

    if (showStorageBreakdownModal) {
        StorageBreakdownModal(
            storageDevices = uiState.storageDevices,
            language = uiState.language,
            onDismiss = { showStorageBreakdownModal = false }
        )
    }

    if (showLanguageModal) {
        LanguageDialog(
            currentLanguage = uiState.language,
            onSelectLanguage = {
                viewModel.setLanguage(it)
                showLanguageModal = false
            },
            onDismiss = { showLanguageModal = false }
        )
    }

    if (showPinChangeModal) {
        PinChangeDialog(
            language = uiState.language,
            onSavePin = {
                viewModel.updatePin(it)
                showPinChangeModal = false
            },
            onDismiss = { showPinChangeModal = false }
        )
    }

    if (showLegalPoliciesModal) {
        LegalPoliciesModal(
            initialType = legalPolicyType,
            language = uiState.language,
            onDismiss = { showLegalPoliciesModal = false }
        )
    }

    // Media Modals
    if (activeMusicFile != null) {
        MusicPlayerModal(
            playingFile = activeMusicFile!!,
            isPlaying = uiState.isAudioPlaying,
            positionSeconds = uiState.audioPositionSeconds,
            durationSeconds = uiState.audioDurationSeconds,
            language = uiState.language,
            isShuffle = uiState.isAudioShuffle,
            repeatMode = uiState.audioRepeatMode,
            playbackSpeed = uiState.audioPlaybackSpeed,
            equalizerPreset = uiState.audioEqualizerPreset,
            activeSleepTimerMinutes = uiState.audioSleepTimerMinutes,
            onTogglePlayPause = { viewModel.toggleAudioPlayPause() },
            onPrevious = { viewModel.prevAudioTrack() },
            onNext = { viewModel.nextAudioTrack() },
            onSeek = { viewModel.seekAudio(it) },
            onToggleShuffle = { viewModel.toggleAudioShuffle() },
            onCycleRepeatMode = { viewModel.cycleAudioRepeatMode() },
            onSetSpeed = { viewModel.setAudioPlaybackSpeed(it) },
            onSetEqualizer = { viewModel.setAudioEqualizerPreset(it) },
            onSetSleepTimer = { viewModel.setAudioSleepTimer(it) },
            onToggleStar = { viewModel.toggleStarred(it) },
            onDismiss = { activeMusicFile = null }
        )
    }

    if (activeVideoFile != null) {
        VideoPlayerModal(
            playingFile = activeVideoFile!!,
            isPlaying = uiState.isVideoPlaying,
            positionSeconds = uiState.videoPositionSeconds,
            durationSeconds = uiState.videoDurationSeconds,
            language = uiState.language,
            isLocked = uiState.isVideoLocked,
            aspectRatioMode = uiState.videoAspectRatio,
            playbackSpeed = uiState.videoPlaybackSpeed,
            onTogglePlay = { viewModel.toggleVideoPlayPause() },
            onSeek = { viewModel.seekVideo(it) },
            onSeekRelative = { viewModel.seekVideoRelative(it) },
            onNextTrack = { viewModel.nextVideoTrack() },
            onPrevTrack = { viewModel.prevVideoTrack() },
            onToggleLock = { viewModel.toggleVideoLock() },
            onCycleAspectRatio = { viewModel.cycleVideoAspectRatio() },
            onSetSpeed = { viewModel.setVideoPlaybackSpeed(it) },
            onDismiss = { activeVideoFile = null }
        )
    }

    if (activePdfFile != null) {
        PdfViewerModal(
            file = activePdfFile!!,
            language = uiState.language,
            onDismiss = { activePdfFile = null },
            onToggleStar = { viewModel.toggleStarred(it) },
            onDeleteFile = { viewModel.moveToTrash(it) }
        )
    }

    if (activeImageFile != null) {
        ImageViewerModal(
            file = activeImageFile!!,
            allImageFiles = uiState.files.filter { it.category == FileCategoryType.IMAGES },
            language = uiState.language,
            onDismiss = { activeImageFile = null },
            onToggleStar = { viewModel.toggleStarred(it) },
            onDeleteFile = { viewModel.moveToTrash(it) },
            onNavigateFile = { activeImageFile = it }
        )
    }

    // Archive Dialogs
    if (archiveToCompressFiles != null) {
        ArchiveCompressModal(
            filesToCompress = archiveToCompressFiles!!,
            language = uiState.language,
            onConfirm = { name, levelInt, pass, test, splitStr ->
                val compLevel = when (levelInt) {
                    0 -> CompressionLevel.STORE
                    1, 2 -> CompressionLevel.FASTEST
                    3, 4 -> CompressionLevel.FAST
                    5, 6 -> CompressionLevel.NORMAL
                    7, 8 -> CompressionLevel.MAXIMUM
                    else -> CompressionLevel.ULTRA
                }
                val splitOpt = when (splitStr) {
                    "10MB" -> SplitVolumeOption.SPLIT_10MB
                    "50MB" -> SplitVolumeOption.SPLIT_50MB
                    else -> SplitVolumeOption.NONE
                }
                viewModel.compressFiles(
                    name = name,
                    format = ArchiveFormat.ZIP,
                    level = compLevel,
                    method = CompressionMethod.DEFLATE,
                    password = pass,
                    encryptHeader = false,
                    split = splitOpt,
                    deleteSource = false
                )
                archiveToCompressFiles = null
            },
            onDismiss = { archiveToCompressFiles = null }
        )
    }

    if (archiveToExtractFile != null) {
        ArchiveExtractModal(
            archiveFile = archiveToExtractFile!!,
            language = uiState.language,
            onConfirm = { pass, test ->
                viewModel.extractArchive(
                    destinationPath = "/storage/emulated/0/Download",
                    password = pass,
                    createSubfolder = true
                )
                archiveToExtractFile = null
            },
            onDismiss = { archiveToExtractFile = null }
        )
    }

    if (archiveToViewFile != null) {
        ArchiveViewerModal(
            archiveFile = archiveToViewFile!!,
            language = uiState.language,
            onExtract = {
                archiveToExtractFile = archiveToViewFile
                archiveToViewFile = null
            },
            onDismiss = { archiveToViewFile = null }
        )
    }

    if (uiState.showArchiveTestResultDialog && uiState.archiveTestResult != null) {
        ArchiveTestResultModal(
            archiveName = uiState.activeArchiveFile?.name ?: "Archive.zip",
            language = uiState.language,
            onDismiss = { viewModel.closeArchiveTestResult() }
        )
    }
}

private fun handleOpenFile(
    file: FileItem,
    viewModel: FilesViewModel,
    onOpenMusic: (FileItem) -> Unit,
    onOpenVideo: (FileItem) -> Unit,
    onOpenPdf: (FileItem) -> Unit,
    onOpenImage: (FileItem) -> Unit,
    onOpenArchive: (FileItem) -> Unit
) {
    when (file.category) {
        FileCategoryType.AUDIO -> {
            viewModel.playAudio(file, openPlayer = true)
            onOpenMusic(file)
        }
        FileCategoryType.VIDEOS -> {
            viewModel.playVideo(file)
            onOpenVideo(file)
        }
        FileCategoryType.IMAGES -> {
            onOpenImage(file)
        }
        FileCategoryType.DOCUMENTS -> {
            onOpenPdf(file)
        }
        FileCategoryType.ARCHIVES -> {
            onOpenArchive(file)
        }
        else -> {
            val ext = file.extension.lowercase()
            when {
                ext in listOf("mp3", "flac", "wav", "m4a", "ogg") -> {
                    viewModel.playAudio(file, openPlayer = true)
                    onOpenMusic(file)
                }
                ext in listOf("mp4", "mkv", "avi", "mov") -> {
                    viewModel.playVideo(file)
                    onOpenVideo(file)
                }
                ext in listOf("jpg", "jpeg", "png", "webp", "gif") -> {
                    onOpenImage(file)
                }
                ext in listOf("pdf", "txt", "doc", "docx", "xls") -> {
                    onOpenPdf(file)
                }
                ext in listOf("zip", "7z", "rar") -> {
                    onOpenArchive(file)
                }
                else -> {
                    onOpenPdf(file)
                }
            }
        }
    }
}
