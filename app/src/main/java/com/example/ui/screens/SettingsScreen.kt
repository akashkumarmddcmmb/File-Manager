package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.ui.modals.OnScreenPermissionModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onChangeSafeFolderPin: () -> Unit,
    onOpenStorageBreakdown: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    onBack: () -> Unit
) {
    var isDarkTheme by remember { mutableStateOf("system") } // "system", "light", "dark"
    var showHiddenFiles by remember { mutableStateOf(false) }
    var junkAlertEnabled by remember { mutableStateOf(true) }
    var backgroundMusicEnabled by remember { mutableStateOf(true) }
    var selectedAccent by remember { mutableStateOf("dynamic") }
    var showOnScreenPermissionModal by remember { mutableStateOf(false) }

    val performBack: () -> Unit = {
        if (showOnScreenPermissionModal) {
            showOnScreenPermissionModal = false
        } else {
            onBack()
        }
    }

    BackHandler(enabled = true) {
        performBack()
    }

    val isHindi = currentLanguage == AppLanguage.HINDI

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
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                text = if (isHindi) "सेटिंग्स" else "Settings",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "फ़ाइलें - आकाश कुमार द्वारा" else "Files - by Akash Kumar",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Green "पूर्ण" (Done) button on top right
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isHindi) "पूर्ण" else "Done",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. सामान्य (General)
            item {
                SettingsGroupCard(
                    icon = Icons.Default.Tune,
                    title = if (isHindi) "सामान्य" else "General"
                ) {
                    // Language selection row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "भाषा / App Language" else "App Language",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "हिन्दी (Hindi)" else "English",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = currentLanguage == AppLanguage.ENGLISH,
                                onClick = { onSelectLanguage(AppLanguage.ENGLISH) },
                                label = { Text("English", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = currentLanguage == AppLanguage.HINDI,
                                onClick = { onSelectLanguage(AppLanguage.HINDI) },
                                label = { Text("हिंदी", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00C853),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Theme row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "थीम" else "Theme",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "सिस्टम डिफ़ॉल्ट" else "System Default",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            FilterChip(
                                selected = isDarkTheme == "system",
                                onClick = { isDarkTheme = "system" },
                                label = { Text(if (isHindi) "सिस्टम" else "System", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00C853),
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = isDarkTheme == "light",
                                onClick = { isDarkTheme = "light" },
                                label = { Text(if (isHindi) "लाइट" else "Light", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = isDarkTheme == "dark",
                                onClick = { isDarkTheme = "dark" },
                                label = { Text(if (isHindi) "डार्क" else "Dark", fontSize = 11.sp) }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Accent Color Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isHindi) "एक्सेंट रंग (Accent Color)" else "Accent Color",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isHindi) "बटन, आइकन और नेविगेशन रंग बदलें" else "Customize buttons and icon colors",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1B382B)
                            ) {
                                Text(
                                    text = if (isHindi) "सक्रिय" else "Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00C853),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Material You dynamic pill
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAccent = "dynamic" },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selectedAccent == "dynamic") Color(0xFF1B382B) else MaterialTheme.colorScheme.surface,
                            border = if (selectedAccent == "dynamic") ButtonDefaults.outlinedButtonBorder else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00C853)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isHindi) "सिस्टम डायनामिक रंग (Material You)" else "System Dynamic Color (Material You)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isHindi) "एंड्रॉयड वॉलपेपर और ऑसम पैलेट के साथ ऑटो-सिंक" else "Auto-sync with Android wallpaper",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Color swatches row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ColorSwatchItem("Emerald", Color(0xFF00C853), selectedAccent == "emerald") { selectedAccent = "emerald" }
                            ColorSwatchItem("Ocean", Color(0xFF0288D1), selectedAccent == "ocean") { selectedAccent = "ocean" }
                            ColorSwatchItem("Royal", Color(0xFF5E35B1), selectedAccent == "royal") { selectedAccent = "royal" }
                            ColorSwatchItem("Sunset", Color(0xFFE64A19), selectedAccent == "sunset") { selectedAccent = "sunset" }
                            ColorSwatchItem("Gold", Color(0xFFFBC02D), selectedAccent == "gold") { selectedAccent = "gold" }
                            ColorSwatchItem("Arctic", Color(0xFF00BCD4), selectedAccent == "arctic") { selectedAccent = "arctic" }
                            ColorSwatchCustomItem("Custom", selectedAccent == "custom") { selectedAccent = "custom" }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Show hidden files switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "छिपी हुई फाइलें दिखाएं" else "Show hidden files",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "डॉट (.) से शुरू होने वाली फाइलें दिखाएं (जैसे .nomedia और कैश)" else "Show files starting with dot (.)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showHiddenFiles,
                            onCheckedChange = { showHiddenFiles = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00C853))
                        )
                    }
                }
            }

            // 2. स्टोरेज और क्लीनर मैनेजमेंट (Storage & Cleaner Management)
            item {
                SettingsGroupCard(
                    icon = Icons.Default.Storage,
                    title = if (isHindi) "स्टोरेज और क्लीनर मैनेजमेंट" else "Storage & Cleaner Management"
                ) {
                    // Storage Details row
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenStorageBreakdown),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1A73E8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "स्टोरेज विवरण" else "Storage Details",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "19 GB of 225 GB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Junk Cleaner Alert Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "जंक क्लीनर अलर्ट" else "Junk cleaner alert",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "जंक फाइलें 500 MB से अधिक होने पर सूचित करें" else "Notify when junk files exceed 500 MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = junkAlertEnabled,
                            onCheckedChange = { junkAlertEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFF9AB00))
                        )
                    }
                }
            }

            // 3. सुरक्षित फ़ोल्डर सुरक्षा (Safe Folder Security)
            item {
                SettingsGroupCard(
                    icon = Icons.Default.Shield,
                    title = if (isHindi) "सुरक्षित फ़ोल्डर सुरक्षा" else "Safe Folder Security"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "PIN बदलें" else "Change PIN",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Button(
                            onClick = onChangeSafeFolderPin,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853), contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(if (isHindi) "बदलें" else "Change", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "तुरंत लॉक करें" else "Lock immediately",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                            Text(
                                text = if (isHindi) "तुरंत" else "Immediately",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Help, contentDescription = null, tint = Color(0xFFE64A19), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isHindi) "सुरक्षा प्रश्न" else "Security Question",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isHindi) "पासवर्ड रीसेट करने के लिए सुरक्षा प्रश्न सेट करें" else "Set security question for password recovery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(
                            onClick = onChangeSafeFolderPin,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64A19), contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(if (isHindi) "सेट अप" else "Set Up", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. क्विक शेयर (Quick Share)
            item {
                SettingsGroupCard(
                    icon = Icons.Default.Share,
                    title = if (isHindi) "क्विक शेयर" else "Quick Share"
                ) {
                    Column {
                        Text(
                            text = if (isHindi) "डिवाइस का नाम" else "Device Name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Akash's Android Device",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isHindi) "दृश्यता" else "Visibility",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1B382B)) {
                                    Text(
                                        text = if (isHindi) "सभी के लिए" else "Everyone",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00C853),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "दिया हुआ" else "Given",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 5. मीडिया और ऑडियो प्लेबैक (Media & Audio Playback)
            item {
                SettingsGroupCard(
                    icon = Icons.Default.MusicNote,
                    title = if (isHindi) "मीडिया और ऑडियो प्लेबैक" else "Media & Audio Playback"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "बैकग्राउंड म्यूजिक प्लेबैक" else "Background music playback",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "ऐप बंद करने के बाद भी MP3 ऑडियो चलाना जारी रखें" else "Continue playing audio even when app is closed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = backgroundMusicEnabled,
                            onCheckedChange = { backgroundMusicEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF7C4DFF))
                        )
                    }
                }
            }

            // 6. लो बैटरी खपत मोड (Low Battery Mode)
            item {
                SettingsGroupCard(
                    icon = Icons.Default.BatteryChargingFull,
                    title = if (isHindi) "लो बैटरी खपत मोड" else "Low Battery Consumption Mode"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "अल्ट्रा बैटरी सेवर सक्रिय" else "Ultra Battery Saver",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "डार्क ओएलईडी पृष्ठभूमि, न्यूनतम स्टोरेज स्कैनिंग और इष्टतम बैटरी सहनशीलता के लिए कम सीपीयू प्रभाव" else "Dark OLED theme & low CPU consumption",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF1B382B)) {
                            Text(
                                text = if (isHindi) "सक्रिय" else "Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00C853),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 7. डेमो और नमूना फाइलें (Demo & Sample Files)
            item {
                SettingsGroupCard(
                    icon = Icons.Default.Science,
                    title = if (isHindi) "डेमो और नमूना फाइलें" else "Demo & Sample Files"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isHindi) "वास्तविक डिज़ाइन (क्लीन मोड)" else "Clean Mode Design",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1B382B)) {
                                    Text(
                                        text = if (isHindi) "वास्तविक" else "Real",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00C853),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isHindi) "कोई डमी फ़ाइल नहीं, केवल वास्तविक स्टोरेज फाइलें लोड करें" else "Only load real local storage files",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8), contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(if (isHindi) "नमूना लोड करें" else "Load Samples", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "ऐप कैश साफ़ करें" else "Clear App Cache",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "अस्थायी कैश मेमोरी और थंबनेल इंडेक्स साफ़ करें" else "Clear temporary cache memory",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD93025)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(if (isHindi) "साफ़ करें" else "Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "डिफ़ॉल्ट सेटिंग्स रीसेट करें" else "Reset to Default",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "सभी सेटिंग्स को डिफ़ॉल्ट मानों पर सेट करें" else "Restore all settings to default",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {},
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 8. ऐप और कानूनी जानकारी (App & Legal Info)
            item {
                SettingsGroupCard(
                    icon = Icons.Default.Info,
                    title = if (isHindi) "ऐप और कानूनी जानकारी" else "App & Legal Information"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isHindi) "संस्करण" else "Version", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "फ़ाइलें - आकाश कुमार द्वारा v2.4.0 (ऑफिशियल)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = if (isHindi) "डेवलपर" else "Developer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "आकाश कुमार", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Contact / Support", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "akashkumarmddcmmb@gmail.com", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF1A73E8), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isHindi) "डेवलपर को प्रतिक्रिया भेजें" else "Send Developer Feedback",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Button(
                                    onClick = {},
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8), contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                ) {
                                    Text("Open Portal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = onOpenPrivacyPolicy) {
                                Icon(Icons.Default.PrivacyTip, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isHindi) "गोपनीयता नीति" else "Privacy Policy", fontSize = 12.sp)
                            }
                            TextButton(onClick = onOpenTerms) {
                                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isHindi) "सेवा की शर्तें" else "Terms of Service", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ColorSwatchItem(
    name: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) Modifier.border(2.dp, Color.White, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ColorSwatchCustomItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C323D))
                .then(
                    if (selected) Modifier.border(2.dp, Color.White, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsGroupCard(
    icon: ImageVector? = null,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}
