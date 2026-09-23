package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var isDarkTheme by remember { mutableStateOf(true) }
    var showHiddenFiles by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showOnScreenPermissionModal by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        when {
            showLanguagePicker -> showLanguagePicker = false
            showOnScreenPermissionModal -> showOnScreenPermissionModal = false
            else -> onBack()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "सेटिंग्स (Settings)" else "Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
            // General Preferences
            item {
                SettingsGroupCard(
                    title = if (currentLanguage == AppLanguage.HINDI) "सामान्य (General)" else "General Preferences"
                ) {
                    SettingsRow(
                        title = if (currentLanguage == AppLanguage.HINDI) "भाषा चुनें (Language)" else "App Language",
                        subtitle = if (currentLanguage == AppLanguage.HINDI) "हिंदी (Hindi)" else "English",
                        icon = Icons.Default.Language,
                        onClick = { showLanguagePicker = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    SettingsRowWithSwitch(
                        title = if (currentLanguage == AppLanguage.HINDI) "डार्क थीम (Dark Mode)" else "Dark Theme",
                        subtitle = if (currentLanguage == AppLanguage.HINDI) "आंखों के लिए आरामदायक डार्क इंटरफेस" else "Dark UI for reduced eye strain",
                        icon = Icons.Default.DarkMode,
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    SettingsRowWithSwitch(
                        title = if (currentLanguage == AppLanguage.HINDI) "छिपी हुई फाइलें दिखाएं (Hidden files)" else "Show Hidden Files",
                        subtitle = if (currentLanguage == AppLanguage.HINDI) ". से शुरू होने वाली फाइलें दिखाएं" else "Display files starting with dot (.)",
                        icon = Icons.Default.Visibility,
                        checked = showHiddenFiles,
                        onCheckedChange = { showHiddenFiles = it }
                    )
                }
            }

            // Permissions & Security
            item {
                SettingsGroupCard(
                    title = if (currentLanguage == AppLanguage.HINDI) "सुरक्षा व ऑनस्रीन अनुमतियां" else "Permissions & Security"
                ) {
                    SettingsRow(
                        title = if (currentLanguage == AppLanguage.HINDI) "ऑनस्रीन अनुमति कंट्रोल" else "On-Screen Permission Center",
                        subtitle = if (currentLanguage == AppLanguage.HINDI) "बिना किसी बाहरी विंडो के सभी अनुमतियां ऑन/ऑफ करें" else "Manage all permissions directly on-screen",
                        icon = Icons.Default.Security,
                        onClick = { showOnScreenPermissionModal = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    SettingsRow(
                        title = if (currentLanguage == AppLanguage.HINDI) "सुरक्षित फ़ोल्डर पासवर्ड बदलें" else "Change Safe Folder PIN",
                        subtitle = if (currentLanguage == AppLanguage.HINDI) "4-8 अक्षरों का पासवर्ड बदलें" else "Update 4-8 digit passcode",
                        icon = Icons.Default.Lock,
                        onClick = onChangeSafeFolderPin
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    SettingsRow(
                        title = if (currentLanguage == AppLanguage.HINDI) "स्टोरेज विश्लेषण देखें" else "Storage Breakdown",
                        subtitle = if (currentLanguage == AppLanguage.HINDI) "इंटरनल व एसडी कार्ड उपयोग देखें" else "Analyze storage breakdown",
                        icon = Icons.Default.PieChart,
                        onClick = onOpenStorageBreakdown
                    )
                }
            }

            // Legal & Policies
            item {
                SettingsGroupCard(
                    title = if (currentLanguage == AppLanguage.HINDI) "कानूनी व नीतियां" else "Legal & Privacy Policies"
                ) {
                    SettingsRow(
                        title = if (currentLanguage == AppLanguage.HINDI) "गोपनीयता नीति (Privacy Policy)" else "Privacy Policy",
                        subtitle = if (currentLanguage == AppLanguage.HINDI) "100% स्थानीय डेटा सुरक्षा नीति" else "100% local data processing policy",
                        icon = Icons.Default.PrivacyTip,
                        onClick = onOpenPrivacyPolicy
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    SettingsRow(
                        title = if (currentLanguage == AppLanguage.HINDI) "सेवा की शर्तें (Terms & Conditions)" else "Terms & Conditions",
                        subtitle = if (currentLanguage == AppLanguage.HINDI) "उपयोग के नियम व शर्तें" else "App usage terms and guidelines",
                        icon = Icons.Default.Gavel,
                        onClick = onOpenTerms
                    )
                }
            }

            // Developer Info Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A73E8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AK",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Files by Akash Kumar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Version 2.4.0 (Build 2026)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Developer Contact: akashkumarmddcmmb@gmail.com\nCreated with Kotlin & Jetpack Compose",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showOnScreenPermissionModal) {
        OnScreenPermissionModal(
            language = currentLanguage,
            onDismiss = { showOnScreenPermissionModal = false },
            onPermissionsUpdated = {}
        )
    }

    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = {
                Text(
                    text = "Select App Language / भाषा चुनें",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectLanguage(AppLanguage.HINDI)
                                showLanguagePicker = false
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "हिंदी (Hindi)",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (currentLanguage == AppLanguage.HINDI) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectLanguage(AppLanguage.ENGLISH)
                                showLanguagePicker = false
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (currentLanguage == AppLanguage.ENGLISH) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguagePicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsRowWithSwitch(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
