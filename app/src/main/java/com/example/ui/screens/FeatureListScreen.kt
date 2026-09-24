package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage

enum class FeatureCategoryKey {
    ALL,
    FILE_BROWSER,
    THEME,
    SECURITY,
    JUNK_CLEANER,
    SHARE,
    SETTINGS
}

data class FeatureItemData(
    val id: String,
    val category: FeatureCategoryKey,
    val titleHi: String,
    val titleEn: String,
    val tagHi: String,
    val tagEn: String,
    val descHi: String,
    val descEn: String,
    val icon: ImageVector,
    val iconBgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureListScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit = onBack,
    onOpenBrowse: () -> Unit = onBack,
    onOpenClean: () -> Unit = onBack,
    onOpenShare: () -> Unit = onBack,
    onOpenSafeFolder: () -> Unit = onBack,
    onOpenTrash: () -> Unit = onBack,
    onOpenStorageBreakdown: () -> Unit = onBack,
    onOpenLanguageDialog: () -> Unit = onBack,
    onOpenZipManager: () -> Unit = onBack
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FeatureCategoryKey.ALL) }

    val performBack: () -> Unit = {
        when {
            searchQuery.isNotBlank() -> {
                searchQuery = ""
            }
            selectedCategory != FeatureCategoryKey.ALL -> {
                selectedCategory = FeatureCategoryKey.ALL
            }
            else -> {
                onBack()
            }
        }
    }

    BackHandler(enabled = true) {
        performBack()
    }

    val isHindi = language == AppLanguage.HINDI

    val allFeatures = remember {
        listOf(
            FeatureItemData(
                id = "theme_dynamic",
                category = FeatureCategoryKey.THEME,
                titleHi = "सिस्टम थीम और डायनेमिक रंग",
                titleEn = "System Theme & Dynamic Color",
                tagHi = "सक्रिय",
                tagEn = "Active",
                descHi = "स्वचालित रूप से आपके डिवाइस के सिस्टम डार्क/लाइट थीम और वॉलपेपर पैलेट के साथ सिंक होता है।",
                descEn = "Automatically syncs with your device system dark/light theme and dynamic wallpaper palette.",
                icon = Icons.Default.Palette,
                iconBgColor = Color(0xFF1A73E8)
            ),
            FeatureItemData(
                id = "category_browser",
                category = FeatureCategoryKey.FILE_BROWSER,
                titleHi = "वर्गीकृत फाइल ब्राउज़र",
                titleEn = "Categorized File Browser",
                tagHi = "8 श्रेणियां",
                tagEn = "8 Categories",
                descHi = "डाउनलोड, इमेज, वीडियो, ऑडियो, दस्तावेज़, ऐप्स और बड़ी फाइलों के लिए त्वरित ब्राउज़िंग उपलब्ध है।",
                descEn = "Quick browsing for downloads, images, videos, audio, documents, apps, and large files.",
                icon = Icons.Filled.Folder,
                iconBgColor = Color(0xFF00C853)
            ),
            FeatureItemData(
                id = "recent_carousel",
                category = FeatureCategoryKey.FILE_BROWSER,
                titleHi = "हाल की फाइलें हिंडोला",
                titleEn = "Recent Files Carousel",
                tagHi = "लाइव पूर्वावलोकन",
                tagEn = "Live Preview",
                descHi = "वीडियो की लंबाई, आकार संकेतक और त्वरित पूर्वावलोकन के साथ क्षैतिज हिंडोला दृश्य।",
                descEn = "Horizontal carousel view with video duration, file size indicator, and quick preview.",
                icon = Icons.Filled.VideoLibrary,
                iconBgColor = Color(0xFFD93025)
            ),
            FeatureItemData(
                id = "safe_folder",
                category = FeatureCategoryKey.SECURITY,
                titleHi = "सुरक्षित फ़ोल्डर (PIN लॉक)",
                titleEn = "Safe Folder (PIN Lock)",
                tagHi = "पिन द्वारा सुरक्षित",
                tagEn = "PIN Protected",
                descHi = "4-अंकीय पिन लॉक और ऐप बंद होने पर त्वरित लॉक के साथ व्यक्तिगत फाइलों को सुरक्षित रखें।",
                descEn = "Keep personal files private behind 4-digit PIN lock and auto-lock on app minimize.",
                icon = Icons.Filled.Shield,
                iconBgColor = Color(0xFF00C853)
            ),
            FeatureItemData(
                id = "junk_cleaner",
                category = FeatureCategoryKey.JUNK_CLEANER,
                titleHi = "जंक और कैश क्लीनर",
                titleEn = "Junk & Cache Cleaner",
                tagHi = "1-टैप क्लीनर",
                tagEn = "1-Tap Clean",
                descHi = "कैश फ़ाइलें, डुप्लिकेट आइटम और अप्रयुक्त डाउनलोड हटाकर स्टोरेज स्पेस साफ़ करें।",
                descEn = "Free up storage space by clearing cache files, duplicate items, and unused downloads.",
                icon = Icons.Filled.AutoFixHigh,
                iconBgColor = Color(0xFFF9AB00)
            ),
            FeatureItemData(
                id = "trash_recovery",
                category = FeatureCategoryKey.JUNK_CLEANER,
                titleHi = "ट्रैश और 30-दिवसीय पुनर्प्राप्ति",
                titleEn = "Trash & 30-Day Recovery",
                tagHi = "पुनर्प्राप्ति योग्य",
                tagEn = "Recoverable",
                descHi = "हटाया गया फ़ाइलें 30 दिनों तक सुरक्षित रखा जाता है, जिन्हें एक टैप से पुनर्प्राप्त किया जा सकता है।",
                descEn = "Deleted files stay safely in trash for 30 days and can be restored with a single tap.",
                icon = Icons.Filled.Delete,
                iconBgColor = Color(0xFFD93025)
            ),
            FeatureItemData(
                id = "quick_share",
                category = FeatureCategoryKey.SHARE,
                titleHi = "क्विक शेयर (आस-पास शेयर)",
                titleEn = "Quick Share (Nearby Share)",
                tagHi = "ऑफ़लाइन शेयर",
                tagEn = "Offline Share",
                descHi = "बिना इंटरनेट के हाई-स्पीड फ़ाइल ट्रांसफर का उपयोग करके आस-पास के उपकरणों के साथ कोई फ़ाइल साझा करें।",
                descEn = "Share any file with nearby devices using high-speed transfer without internet connection.",
                icon = Icons.Filled.Share,
                iconBgColor = Color(0xFF1A73E8)
            ),
            FeatureItemData(
                id = "storage_sd",
                category = FeatureCategoryKey.FILE_BROWSER,
                titleHi = "आंतरिक और एसडी कार्ड स्टोरेज",
                titleEn = "Internal & SD Card Storage",
                tagHi = "वास्तविक समय मीटर",
                tagEn = "Realtime Meter",
                descHi = "समर्पित एसडी कार्ड पहचान के साथ वास्तविक समय स्टोरेज ब्रेकडाउन प्राप्त करें।",
                descEn = "Get real-time storage breakdown with dedicated SD card detection and partition metering.",
                icon = Icons.Filled.Storage,
                iconBgColor = Color(0xFF1A73E8)
            ),
            FeatureItemData(
                id = "music_player",
                category = FeatureCategoryKey.FILE_BROWSER,
                titleHi = "म्यूजिक और MP3 प्लेयर",
                titleEn = "Music & MP3 Player",
                tagHi = "ध्वनि",
                tagEn = "Audio Engine",
                descHi = "घूमती हुई विनाइल एनीमेशन, वेवफॉर्म इक्वलाइज़र, स्लीप टाइमर और मिनी प्लेयर के साथ हाई-रेस प्लेयर।",
                descEn = "Hi-res player with rotating vinyl animation, equalizer waveform, sleep timer, and mini player.",
                icon = Icons.Filled.Audiotrack,
                iconBgColor = Color(0xFFE91E63)
            ),
            FeatureItemData(
                id = "video_player",
                category = FeatureCategoryKey.FILE_BROWSER,
                titleHi = "वीडियो प्लेयर (1080p FHD)",
                titleEn = "Video Player (1080p FHD)",
                tagHi = "1080p FHD",
                tagEn = "1080p FHD",
                descHi = "जेस्चर ब्राइटनेस/वॉल्यूम कंट्रोल, एस्पेक्ट रेशियो और उपशीर्षक के साथ इमर्सिव फुल-स्क्रीन एचडी प्लेयर।",
                descEn = "Immersive full-screen HD player with gesture brightness/volume, aspect ratio, and subtitles.",
                icon = Icons.Filled.Videocam,
                iconBgColor = Color(0xFFE64A19)
            ),
            FeatureItemData(
                id = "multi_language",
                category = FeatureCategoryKey.SETTINGS,
                titleHi = "बहुभाषी समर्थन (हिंदी और अंग्रेजी)",
                titleEn = "Multi-Language Support",
                tagHi = "2 भाषाएं",
                tagEn = "2 Languages",
                descHi = "हिंदी और अंग्रेजी स्थानीयकृत यूजर इंटरफेस के बीच आसानी से स्विच करें।",
                descEn = "Seamlessly toggle between Hindi and English fully localized user interface.",
                icon = Icons.Filled.Translate,
                iconBgColor = Color(0xFF7C4DFF)
            ),
            FeatureItemData(
                id = "zip_manager",
                category = FeatureCategoryKey.FILE_BROWSER,
                titleHi = ".ZIP आर्काइव मैनेजर",
                titleEn = ".ZIP Archive Manager",
                tagHi = "तैयार",
                tagEn = "Ready",
                descHi = "पासवर्ड सुरक्षा, 0-9 अल्ट्रा कम्प्रेशन स्तर, और मल्टी-वॉल्यूम स्प्लिट्स के साथ .zip बनाएं और निकालें।",
                descEn = "Create and extract .zip archives with password encryption, 0-9 compression, and split volumes.",
                icon = Icons.Filled.FolderZip,
                iconBgColor = Color(0xFF00897B)
            ),
            FeatureItemData(
                id = "battery_saving",
                category = FeatureCategoryKey.SETTINGS,
                titleHi = "बैकग्राउंड मीडिया और बैटरी सेवर",
                titleEn = "Background Media & Battery Saver",
                tagHi = "ऊर्जा अनुकूलित",
                tagEn = "Energy Optimized",
                descHi = "बैकग्राउंड ऑडियो स्ट्रीमिंग समर्थन और ओएलईडी बैटरी-बचत मोड।",
                descEn = "Background audio playback support and dark OLED battery-saving optimization.",
                icon = Icons.Filled.BatteryChargingFull,
                iconBgColor = Color(0xFF00C853)
            )
        )
    }

    val filteredFeatures = remember(searchQuery, selectedCategory, allFeatures) {
        allFeatures.filter { item ->
            val matchesCategory = when (selectedCategory) {
                FeatureCategoryKey.ALL -> true
                else -> item.category == selectedCategory
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                item.titleHi.contains(searchQuery, ignoreCase = true) ||
                item.titleEn.contains(searchQuery, ignoreCase = true) ||
                item.descHi.contains(searchQuery, ignoreCase = true) ||
                item.descEn.contains(searchQuery, ignoreCase = true) ||
                item.tagHi.contains(searchQuery, ignoreCase = true) ||
                item.tagEn.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = performBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = if (isHindi) "सुविधा निर्देशिका" else "Feature Directory",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "सभी अनगिनत सुविधाओं और क्षमताओं का पूरा विवरण" else "Complete directory of features & capabilities",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Green "13 Items" Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF00C853)
                    ) {
                        Text(
                            text = "${filteredFeatures.size} Items",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    placeholder = {
                        Text(
                            text = if (isHindi) "सुविधाएं और उपकरण खोजें..." else "Search features and tools...",
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    singleLine = true
                )
            }

            // 2. Filter Category Pills (LazyRow)
            item {
                val categoryFilters = listOf(
                    FeatureCategoryKey.ALL to if (isHindi) "सभी" else "All",
                    FeatureCategoryKey.FILE_BROWSER to if (isHindi) "फाइल ब्राउज़र" else "File Browser",
                    FeatureCategoryKey.THEME to if (isHindi) "थीम" else "Theme",
                    FeatureCategoryKey.SECURITY to if (isHindi) "सुरक्षा" else "Security",
                    FeatureCategoryKey.JUNK_CLEANER to if (isHindi) "जंक क्लीनर" else "Junk Cleaner",
                    FeatureCategoryKey.SHARE to if (isHindi) "शेयर" else "Share",
                    FeatureCategoryKey.SETTINGS to if (isHindi) "सेटिंग्स" else "Settings"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryFilters) { (catKey, label) ->
                        val isSelected = selectedCategory == catKey
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF00C853) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = catKey }
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 3. Featured Hero Card at Top (सिस्टम डायनेमिक थीम)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB9F6CA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFF00701A),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "सिस्टम डायनेमिक थीम (Material You)" else "System Dynamic Theme (Material You)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isHindi)
                                    "आपके एंड्रॉइड वॉलपेपर और सिस्टम डार्क/लाइट थीम के साथ स्वचालित रूप से सिंक होता है।"
                                else
                                    "Automatically syncs with your Android wallpaper and system dark/light theme.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onOpenSettings,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00C853),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isHindi) "बदलें" else "Change",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. Feature Cards List (13 items)
            items(filteredFeatures) { item ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (item.id) {
                                "theme_dynamic", "battery_saving" -> onOpenSettings()
                                "category_browser", "recent_carousel" -> onOpenBrowse()
                                "safe_folder" -> onOpenSafeFolder()
                                "junk_cleaner" -> onOpenClean()
                                "trash_recovery" -> onOpenTrash()
                                "quick_share" -> onOpenShare()
                                "storage_sd" -> onOpenStorageBreakdown()
                                "music_player", "video_player" -> onOpenBrowse()
                                "multi_language" -> onOpenLanguageDialog()
                                "zip_manager" -> onOpenZipManager()
                                else -> onOpenBrowse()
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(item.iconBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) item.titleHi else item.titleEn,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1B382B)
                                ) {
                                    Text(
                                        text = if (isHindi) item.tagHi else item.tagEn,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00C853),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (isHindi) "खोलें >" else "Open >",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00C853),
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isHindi) item.descHi else item.descEn,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
