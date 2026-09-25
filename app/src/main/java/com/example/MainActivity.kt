package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.modals.*
import com.example.ui.screens.*
import com.example.ui.theme.FilesTheme
import com.example.update.UpdateResult
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
            val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }
            val initialPassed = remember { prefs.getBoolean("permission_gate_passed", false) }

            var hasPermissionGranted by remember { mutableStateOf(initialPassed) }

            LaunchedEffect(Unit) {
                viewModel.setContext(context.applicationContext)
                val audioManager = com.example.audio.Media3AudioManager(context.applicationContext)
                viewModel.setMedia3AudioManager(audioManager)
                viewModel.refreshRealStorage(context.applicationContext)
                // Silent update check on startup (only shows dialog if update is available)
                viewModel.triggerCheckForUpdates(context.applicationContext, showIfNoUpdate = false)
            }

            FilesTheme(darkTheme = true) {
                MainAppContent(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun MainAppContent(
    uiState: FilesUiState,
    viewModel: FilesViewModel
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE) }
    val initialPassed = remember { prefs.getBoolean("permission_gate_passed", false) }
    var hasPermissionGranted by remember { mutableStateOf(initialPassed) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(AppNavScreen.MAIN_TABS) }
    val screenHistory = remember { mutableStateListOf(AppNavScreen.MAIN_TABS) }
    val tabHistory = remember { mutableStateListOf(MainTab.BROWSE) }

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

    StoragePermissionGate(
        hasPermission = hasPermissionGranted,
        language = uiState.language,
        onSelectLanguage = { viewModel.setLanguage(it) },
        onOpenSafeFolder = {
            hasPermissionGranted = true
            showSafeFolderModal = true
            viewModel.refreshRealStorage(context)
        },
        onOpenClean = {
            hasPermissionGranted = true
            viewModel.setTab(MainTab.CLEAN)
            viewModel.refreshRealStorage(context)
        },
        onPermissionGranted = {
            hasPermissionGranted = true
            viewModel.refreshRealStorage(context)
        }
    ) {

    // Media Viewer Modals
    var activeMusicFile by remember { mutableStateOf<FileItem?>(null) }
    var activeVideoFile by remember { mutableStateOf<FileItem?>(null) }
    var activePdfFile by remember { mutableStateOf<FileItem?>(null) }
    var activeImageFile by remember { mutableStateOf<FileItem?>(null) }

    // Archive Modals
    var archiveToCompressFiles by remember { mutableStateOf<List<FileItem>?>(null) }
    var archiveToExtractFile by remember { mutableStateOf<FileItem?>(null) }
    var archiveToViewFile by remember { mutableStateOf<FileItem?>(null) }

    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    fun navigateToScreen(screen: AppNavScreen) {
        if (currentScreen != screen) {
            if (screen == AppNavScreen.MAIN_TABS) {
                screenHistory.clear()
                screenHistory.add(AppNavScreen.MAIN_TABS)
            } else {
                screenHistory.add(screen)
            }
            currentScreen = screen
        }
    }

    fun navigateToTab(tab: MainTab) {
        if (uiState.currentTab != tab) {
            if (tab == MainTab.BROWSE) {
                tabHistory.clear()
                tabHistory.add(MainTab.BROWSE)
            } else {
                tabHistory.add(tab)
            }
            viewModel.setTab(tab)
        }
    }

    fun handleBackNavigation(): Boolean {
        when {
            // 1. Drawer open -> close drawer
            drawerState.isOpen -> {
                coroutineScope.launch { drawerState.close() }
                return true
            }
            // 2. Active Archive Modals -> close archive modal
            archiveToViewFile != null -> {
                archiveToViewFile = null
                return true
            }
            archiveToExtractFile != null -> {
                archiveToExtractFile = null
                return true
            }
            archiveToCompressFiles != null -> {
                archiveToCompressFiles = null
                return true
            }
            // 3. Active Media Modals -> close media viewer
            activeImageFile != null -> {
                activeImageFile = null
                return true
            }
            activePdfFile != null -> {
                activePdfFile = null
                return true
            }
            activeVideoFile != null || uiState.showFullVideoPlayer -> {
                viewModel.stopVideoPlayback()
                viewModel.closeVideoPlayer()
                activeVideoFile = null
                return true
            }
            activeMusicFile != null || uiState.showFullAudioPlayer -> {
                viewModel.closeFullAudioPlayer()
                activeMusicFile = null
                return true
            }
            // 4. Active System & Settings Modals -> close modal
            showLegalPoliciesModal -> {
                showLegalPoliciesModal = false
                return true
            }
            showPinChangeModal -> {
                showPinChangeModal = false
                return true
            }
            showLanguageModal -> {
                showLanguageModal = false
                return true
            }
            showStorageBreakdownModal -> {
                showStorageBreakdownModal = false
                return true
            }
            showTrashModal -> {
                showTrashModal = false
                return true
            }
            showSafeFolderModal -> {
                showSafeFolderModal = false
                viewModel.lockSafeFolder()
                return true
            }
            // 5. Multi-selection active -> clear archive selection
            uiState.selectedFilesForArchive.isNotEmpty() -> {
                viewModel.closeArchiveCompressDialog()
                return true
            }
            // 6. Search query active in global header -> clear search
            uiState.searchQuery.isNotBlank() -> {
                viewModel.setSearchQuery("")
                return true
            }
            // 7. Sub-screens history (e.g., Settings, Feature list, Category File list) -> step back in screen stack
            screenHistory.size > 1 -> {
                screenHistory.removeAt(screenHistory.lastIndex)
                val prevScreen = screenHistory.lastOrNull() ?: AppNavScreen.MAIN_TABS
                currentScreen = prevScreen
                if (prevScreen != AppNavScreen.CATEGORY_FILE_LIST) {
                    activeCategoryFilter = null
                    activeStorageDevice = null
                }
                return true
            }
            currentScreen != AppNavScreen.MAIN_TABS -> {
                activeCategoryFilter = null
                activeStorageDevice = null
                currentScreen = AppNavScreen.MAIN_TABS
                screenHistory.clear()
                screenHistory.add(AppNavScreen.MAIN_TABS)
                return true
            }
            // 8. Tab history (e.g., Clean/Share back to Browse) -> step back in tab stack
            tabHistory.size > 1 -> {
                tabHistory.removeAt(tabHistory.lastIndex)
                val prevTab = tabHistory.lastOrNull() ?: MainTab.BROWSE
                viewModel.setTab(prevTab)
                return true
            }
            uiState.currentTab != MainTab.BROWSE -> {
                tabHistory.clear()
                tabHistory.add(MainTab.BROWSE)
                viewModel.setTab(MainTab.BROWSE)
                return true
            }
            // 9. Root level: Double-back to exit prevention
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime < 2000) {
                    (context as? Activity)?.finish()
                } else {
                    lastBackPressTime = now
                    val exitMsg = if (uiState.language == AppLanguage.HINDI) {
                        "ऐप बंद करने के लिए दोबारा बैक दबाएं"
                    } else {
                        "Press back again to exit"
                    }
                    Toast.makeText(context, exitMsg, Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
    }

    val isAnyOverlayActive = drawerState.isOpen ||
            archiveToViewFile != null ||
            archiveToExtractFile != null ||
            archiveToCompressFiles != null ||
            activeImageFile != null ||
            activePdfFile != null ||
            activeVideoFile != null ||
            uiState.showFullVideoPlayer ||
            activeMusicFile != null ||
            uiState.showFullAudioPlayer ||
            showLegalPoliciesModal ||
            showPinChangeModal ||
            showLanguageModal ||
            showStorageBreakdownModal ||
            showTrashModal ||
            showSafeFolderModal ||
            uiState.selectedFilesForArchive.isNotEmpty()

    BackHandler(enabled = true) {
        handleBackNavigation()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawer(
                currentTab = uiState.currentTab,
                language = uiState.language,
                onSelectTab = { tab ->
                    navigateToTab(tab)
                    navigateToScreen(AppNavScreen.MAIN_TABS)
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenClean = {
                    navigateToTab(MainTab.CLEAN)
                    navigateToScreen(AppNavScreen.MAIN_TABS)
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenBrowse = {
                    navigateToTab(MainTab.BROWSE)
                    navigateToScreen(AppNavScreen.MAIN_TABS)
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenShare = {
                    navigateToTab(MainTab.SHARE)
                    navigateToScreen(AppNavScreen.MAIN_TABS)
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
                    navigateToScreen(AppNavScreen.SETTINGS)
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenHelp = {
                    navigateToScreen(AppNavScreen.FEATURE_LIST)
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (currentScreen == AppNavScreen.MAIN_TABS) {
                    Column(modifier = Modifier.statusBarsPadding()) {
                        AppHeader(
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
                            onTabSelected = { navigateToTab(it) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (currentScreen == AppNavScreen.MAIN_TABS) innerPadding.calculateTopPadding() else 0.dp,
                        bottom = innerPadding.calculateBottomPadding()
                    )
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
                                        navigateToScreen(AppNavScreen.CATEGORY_FILE_LIST)
                                    },
                                    onDeviceClick = { device ->
                                        activeCategoryFilter = null
                                        activeStorageDevice = device
                                        navigateToScreen(AppNavScreen.CATEGORY_FILE_LIST)
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
                                        navigateToScreen(AppNavScreen.CATEGORY_FILE_LIST)
                                    },
                                    onOpenTrash = { showTrashModal = true },
                                    onOpenFeatureList = { navigateToScreen(AppNavScreen.FEATURE_LIST) },
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
                            onBack = { handleBackNavigation() },
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
                            },
                            onCopyItems = { items ->
                                viewModel.openDestinationPicker(items, ClipboardOperationType.COPY)
                            },
                            onMoveItems = { items ->
                                viewModel.openDestinationPicker(items, ClipboardOperationType.MOVE)
                            },
                            isOverlayActive = isAnyOverlayActive
                        )
                    }
                    AppNavScreen.SETTINGS -> {
                        val context = androidx.compose.ui.platform.LocalContext.current
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
                            isCheckingForUpdates = uiState.isCheckingForUpdates,
                            githubRepoPath = uiState.githubRepoPath,
                            onGithubRepoPathChange = { viewModel.setGithubRepoPath(it) },
                            onCheckForUpdates = { viewModel.triggerCheckForUpdates(context, showIfNoUpdate = true) },
                            onBack = { handleBackNavigation() }
                        )
                    }
                    AppNavScreen.FEATURE_LIST -> {
                        FeatureListScreen(
                            language = uiState.language,
                            onBack = { handleBackNavigation() },
                            onOpenSettings = { navigateToScreen(AppNavScreen.SETTINGS) },
                            onOpenBrowse = {
                                navigateToTab(MainTab.BROWSE)
                                navigateToScreen(AppNavScreen.MAIN_TABS)
                            },
                            onOpenClean = {
                                navigateToTab(MainTab.CLEAN)
                                navigateToScreen(AppNavScreen.MAIN_TABS)
                            },
                            onOpenShare = {
                                navigateToTab(MainTab.SHARE)
                                navigateToScreen(AppNavScreen.MAIN_TABS)
                            },
                            onOpenSafeFolder = { showSafeFolderModal = true },
                            onOpenTrash = { showTrashModal = true },
                            onOpenStorageBreakdown = { showStorageBreakdownModal = true },
                            onOpenLanguageDialog = { showLanguageModal = true }
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

    if (uiState.showUpdateDialog && uiState.updateResult != null) {
        val language = uiState.language
        val isHindi = language == AppLanguage.HINDI
        val result = uiState.updateResult

        AlertDialog(
            onDismissRequest = { viewModel.setUpdateDialogVisible(false) },
            icon = {
                Icon(
                    imageVector = when (result) {
                        is UpdateResult.Success -> {
                            if (result.updateAvailable) Icons.Default.CloudDownload else Icons.Default.Check
                        }
                        is UpdateResult.Error -> Icons.Default.Info
                        is UpdateResult.NoUpdate -> Icons.Default.Check
                    },
                    contentDescription = null,
                    tint = when (result) {
                        is UpdateResult.Success -> {
                            if (result.updateAvailable) Color(0xFF00C853) else Color(0xFF1973E8)
                        }
                        is UpdateResult.Error -> Color(0xFFD93025)
                        is UpdateResult.NoUpdate -> Color(0xFF1973E8)
                    },
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = when (result) {
                        is UpdateResult.Success -> {
                            if (result.updateAvailable) {
                                if (isHindi) "नया अपडेट उपलब्ध है! 🎉" else "New Update Available! 🎉"
                            } else {
                                if (isHindi) "आप नवीनतम संस्करण पर हैं!" else "You are on the Latest Version!"
                            }
                        }
                        is UpdateResult.Error -> {
                            if (isHindi) "अपडेट जांच विफल रही" else "Update Check Failed"
                        }
                        is UpdateResult.NoUpdate -> {
                            if (isHindi) "कोई अपडेट उपलब्ध नहीं है" else "No Updates Available"
                        }
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (result) {
                        is UpdateResult.Success -> {
                            if (result.updateAvailable) {
                                Text(
                                    text = if (isHindi) 
                                        "एक नया संस्करण (${result.latestVersion}) डाउनलोड के लिए तैयार है। आपका वर्तमान संस्करण ${result.currentVersion} है।"
                                        else 
                                        "A new version (${result.latestVersion}) is available for download. Your current version is ${result.currentVersion}.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHindi) "बदलाव (Changelog):" else "Release Changelog:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF00C853)
                                )
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 140.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 1.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Text(
                                            text = result.changelog,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = if (isHindi) 
                                        "बधाई हो! आप पहले से ही नवीनतम आधिकारिक संस्करण (${result.currentVersion}) का उपयोग कर रहे हैं।"
                                        else 
                                        "Congratulations! You are already running the latest official version (${result.currentVersion}) of File Manager.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        is UpdateResult.Error -> {
                            Text(
                                text = if (isHindi) 
                                    "अपडेट की जांच करते समय एक त्रुटि हुई: ${result.message}\n\nकृपया सुनिश्चित करें कि आपका रिपॉजिटरी पाथ सही है और आपके पास इंटरनेट कनेक्शन है।"
                                    else 
                                    "An error occurred while checking for updates: ${result.message}\n\nPlease verify that your repository path is correct and you have an active network connection.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is UpdateResult.NoUpdate -> {
                            Text(
                                text = if (isHindi) 
                                    "आपके डिवाइस पर वर्तमान में इंस्टॉल किया गया संस्करण नवीनतम उपलब्ध संस्करण है।"
                                    else 
                                    "The version currently installed on your device is the latest available version.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.setUpdateDialogVisible(false) }) {
                        Text(
                            text = if (isHindi) "बाद में" else "Later",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (result is UpdateResult.Success && result.updateAvailable) {
                        Spacer(modifier = Modifier.width(8.dp))
                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        Button(
                            onClick = {
                                viewModel.setUpdateDialogVisible(false)
                                try {
                                    uriHandler.openUri(result.downloadUrl)
                                } catch (e: Exception) {
                                    uriHandler.openUri(result.releasePageUrl)
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00C853),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = if (isHindi) "अभी अपडेट करें" else "Update Now",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Media Modals
    val currentMusic = uiState.playingAudioFile ?: activeMusicFile
    if (currentMusic != null && (uiState.showFullAudioPlayer || activeMusicFile != null)) {
        MusicPlayerModal(
            playingFile = currentMusic,
            isPlaying = uiState.isAudioPlaying,
            positionSeconds = uiState.audioPositionSeconds,
            durationSeconds = uiState.audioDurationSeconds,
            language = uiState.language,
            isShuffle = uiState.isAudioShuffle,
            repeatMode = uiState.audioRepeatMode,
            playbackSpeed = uiState.audioPlaybackSpeed,
            equalizerPreset = uiState.audioEqualizerPreset,
            equalizerEnabled = uiState.audioEqualizerEnabled,
            activeSleepTimerMinutes = uiState.audioSleepTimerMinutes,
            onTogglePlayPause = { viewModel.toggleAudioPlayPause() },
            onPrevious = { viewModel.prevAudioTrack() },
            onNext = { viewModel.nextAudioTrack() },
            onSeek = { viewModel.seekAudio(it) },
            onToggleShuffle = { viewModel.toggleAudioShuffle() },
            onCycleRepeatMode = { viewModel.cycleAudioRepeatMode() },
            onSetSpeed = { viewModel.setAudioPlaybackSpeed(it) },
            onSetEqualizer = { viewModel.setAudioEqualizerPreset(it) },
            onToggleEqualizer = { viewModel.toggleAudioEqualizer(it) },
            onSetSleepTimer = { viewModel.setAudioSleepTimer(it) },
            onToggleStar = { viewModel.toggleStarred(it) },
            onDismiss = {
                viewModel.closeFullAudioPlayer()
                activeMusicFile = null
            }
        )
    }

    val currentVideo = uiState.playingVideoFile ?: activeVideoFile
    if (currentVideo != null && (uiState.showFullVideoPlayer || activeVideoFile != null)) {
        VideoPlayerModal(
            playingFile = currentVideo,
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
            onDismiss = {
                viewModel.stopVideoPlayback()
                viewModel.closeVideoPlayer()
                activeVideoFile = null
            }
        )
    }

    if (activePdfFile != null) {
        val pdfList = uiState.files.filter { 
            it.category == FileCategoryType.DOCUMENTS || it.extension.lowercase() in listOf("pdf", "doc", "docx", "txt") 
        }
        PdfViewerModal(
            file = activePdfFile!!,
            allPdfFiles = if (pdfList.isNotEmpty()) pdfList else listOf(activePdfFile!!),
            language = uiState.language,
            onDismiss = { activePdfFile = null },
            onToggleStar = { viewModel.toggleStarred(it) },
            onDeleteFile = { viewModel.moveToTrash(it) },
            onNavigateFile = { activePdfFile = it }
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
            onConfirm = { name, format, level, method, pass, encryptHeader, splitOpt, deleteSource ->
                viewModel.compressFiles(
                    name = name,
                    format = format,
                    level = level,
                    method = method,
                    password = pass,
                    encryptHeader = encryptHeader,
                    split = splitOpt,
                    deleteSource = deleteSource
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
            onConfirm = { destPath, pass, createSubfolder ->
                viewModel.openArchiveExtractDialog(archiveToExtractFile)
                viewModel.extractArchive(
                    destinationPath = destPath,
                    password = pass,
                    createSubfolder = createSubfolder
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
            onTestIntegrity = {
                viewModel.testArchiveIntegrity(archiveToViewFile)
            },
            onDismiss = { archiveToViewFile = null }
        )
    }

    if (uiState.showArchiveTestResultDialog && uiState.archiveTestResult != null) {
        ArchiveTestResultModal(
            archiveName = uiState.activeArchiveFile?.name ?: "Archive.7z",
            result = uiState.archiveTestResult!!,
            language = uiState.language,
            onDismiss = { viewModel.closeArchiveTestResult() }
        )
    }

    // Folder Destination Picker Modal (Copy / Move File Operations)
    if (uiState.showFolderDestinationPicker && uiState.pendingTransferAction != null) {
        com.example.ui.modals.FolderPickerModal(
            action = uiState.pendingTransferAction!!,
            itemsToProcess = uiState.pendingTransferItems,
            storageDevices = uiState.storageDevices,
            language = uiState.language,
            onConfirmDestination = { destPath, targetMbps ->
                viewModel.executeTransfer(
                    items = uiState.pendingTransferItems,
                    destinationPath = destPath,
                    action = uiState.pendingTransferAction!!,
                    targetSpeedMbps = targetMbps
                )
            },
            onDismiss = { viewModel.closeDestinationPicker() }
        )
    }

    // Real-Time File Transfer & Speed Booster Progress Modal
    if (uiState.transferProgressState.isTransferring) {
        com.example.ui.modals.FileTransferProgressModal(
            progressState = uiState.transferProgressState,
            language = uiState.language,
            onSpeedMbpsChange = { mbps ->
                viewModel.setTransferTargetMbps(mbps)
            },
            onPauseToggle = {
                viewModel.toggleTransferPause()
            },
            onCancelTransfer = {
                viewModel.cancelTransfer()
            },
            onDismissDone = {
                viewModel.dismissTransferDoneModal()
            }
        )
    }

    // Transfer Toast Effect

    LaunchedEffect(uiState.fileTransferSuccessToast) {
        uiState.fileTransferSuccessToast?.let { toastMsg ->
            android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearTransferToast()
        }
    }
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
                ext in listOf("zip", "7z", "tar", "gz", "bz2", "xz") -> {
                    onOpenArchive(file)
                }
                else -> {
                    onOpenPdf(file)
                }
            }
        }
    }
}
