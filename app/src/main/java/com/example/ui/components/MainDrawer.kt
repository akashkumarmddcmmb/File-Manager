package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.MainTab
import com.example.model.getSafeFolderText
import com.example.model.getSettingsText
import com.example.model.getTabTitle
import com.example.model.getTrashText

@Composable
fun MainDrawer(
    currentTab: MainTab,
    language: AppLanguage,
    onSelectTab: (MainTab) -> Unit,
    onOpenClean: () -> Unit,
    onOpenBrowse: () -> Unit,
    onOpenShare: () -> Unit,
    onOpenSafeFolder: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    MainDrawerContent(
        currentTab = currentTab,
        language = language,
        onTabSelected = onSelectTab,
        onCloseDrawer = onCloseDrawer,
        onOpenSafeFolder = onOpenSafeFolder,
        onOpenTrash = onOpenTrash,
        onOpenStorageBreakdown = {},
        onOpenFeatureList = onOpenHelp,
        onOpenLanguageDialog = {},
        onOpenSettings = onOpenSettings,
        onOpenPrivacy = {},
        onOpenTerms = {},
        onOpenFeedback = {},
        onOpenSignIn = {}
    )
}

@Composable
fun MainDrawerContent(
    currentTab: MainTab,
    language: AppLanguage,
    onTabSelected: (MainTab) -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenMusicPlayer: () -> Unit = {},
    onOpenVideoPlayer: () -> Unit = {},
    onOpenZipManager: () -> Unit = {},
    onOpenSafeFolder: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenStorageBreakdown: () -> Unit,
    onOpenFeatureList: () -> Unit,
    onOpenLanguageDialog: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenFeedback: () -> Unit,
    onOpenSignIn: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            // App Branding Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppBrandLogo(modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "फाइल मैनेजर" else "File Manager",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "आकाश कुमार द्वारा" else "by Akash Kumar",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Version v2.4.0 (Official)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onCloseDrawer) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Drawer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Sign-in Banner Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable {
                        onCloseDrawer()
                        onOpenSignIn()
                    },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Sign In",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "साइन इन करें" else "Sign In",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) "बैकअप अकाउंट" else "Backup Account",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Main Tab Navigation Items
            DrawerNavItem(
                icon = Icons.Outlined.AutoFixHigh,
                label = language.getTabTitle(MainTab.CLEAN),
                selected = currentTab == MainTab.CLEAN,
                onClick = {
                    onTabSelected(MainTab.CLEAN)
                    onCloseDrawer()
                }
            )
            DrawerNavItem(
                icon = Icons.Filled.Folder,
                label = language.getTabTitle(MainTab.BROWSE),
                selected = currentTab == MainTab.BROWSE,
                onClick = {
                    onTabSelected(MainTab.BROWSE)
                    onCloseDrawer()
                }
            )
            DrawerNavItem(
                icon = Icons.Outlined.Share,
                label = language.getTabTitle(MainTab.SHARE),
                selected = currentTab == MainTab.SHARE,
                onClick = {
                    onTabSelected(MainTab.SHARE)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Collection Section
            Text(
                text = if (language == AppLanguage.HINDI) "संग्रह" else "Collections",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            DrawerActionItem(
                icon = Icons.Filled.Shield,
                iconTint = Color(0xFF00C853),
                label = language.getSafeFolderText(),
                onClick = {
                    onCloseDrawer()
                    onOpenSafeFolder()
                }
            )

            DrawerActionItem(
                icon = Icons.Filled.Delete,
                iconTint = Color(0xFF00C853),
                label = language.getTrashText(),
                onClick = {
                    onCloseDrawer()
                    onOpenTrash()
                }
            )

            DrawerActionItem(
                icon = Icons.Filled.Storage,
                iconTint = Color(0xFF00C853),
                label = if (language == AppLanguage.HINDI) "स्टोरेज विवरण" else "Storage Breakdown",
                badge = "19 GB / 225 GB",
                onClick = {
                    onCloseDrawer()
                    onOpenStorageBreakdown()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // Feature List Spotlight Item
            DrawerActionItem(
                icon = Icons.Outlined.ChecklistRtl,
                iconTint = Color(0xFF00C853),
                label = if (language == AppLanguage.HINDI) "सुविधा निर्देशिका" else "Feature Directory",
                subtitle = if (language == AppLanguage.HINDI) "सभी अनगिनत सुविधाओं और थीम विकल्पों की पूरी सूची" else "Complete feature capabilities & theme options",
                badge = if (language == AppLanguage.HINDI) "सभी" else "All",
                onClick = {
                    onCloseDrawer()
                    onOpenFeatureList()
                }
            )

            // Settings & Utilities
            DrawerActionItem(
                icon = Icons.Outlined.Translate,
                label = if (language == AppLanguage.HINDI) "भाषा" else "Language",
                badge = if (language == AppLanguage.HINDI) "हिन्दी" else "English",
                onClick = {
                    onCloseDrawer()
                    onOpenLanguageDialog()
                }
            )

            DrawerActionItem(
                icon = Icons.Outlined.Settings,
                label = if (language == AppLanguage.HINDI) "सेटिंग्स" else "Settings",
                onClick = {
                    onCloseDrawer()
                    onOpenSettings()
                }
            )

            DrawerActionItem(
                icon = Icons.Outlined.VerifiedUser,
                label = if (language == AppLanguage.HINDI) "गोपनीयता नीति" else "Privacy Policy",
                onClick = {
                    onCloseDrawer()
                    onOpenPrivacy()
                }
            )

            DrawerActionItem(
                icon = Icons.Outlined.Description,
                label = if (language == AppLanguage.HINDI) "सेवा की शर्तें" else "Terms of Service",
                onClick = {
                    onCloseDrawer()
                    onOpenTerms()
                }
            )

            DrawerActionItem(
                icon = Icons.Outlined.HelpOutline,
                label = if (language == AppLanguage.HINDI) "सहायता और प्रतिक्रिया" else "Help & Feedback",
                subtitle = if (language == AppLanguage.HINDI) "डेवलपर आकाश कुमार को प्रतिक्रिया भेजें" else "Send feedback to developer Akash Kumar",
                onClick = {
                    onCloseDrawer()
                    onOpenFeedback()
                }
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Text(
                text = if (language == AppLanguage.HINDI) "आकाश कुमार - V2.4.0 (ऑफिशियल संस्करण)" else "Akash Kumar - V2.4.0 (Official Edition)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
fun AppBrandLogo(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp))
                    .background(Color(0xFF00C853)) // Beautiful green accent primary style
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(bottomStart = 4.dp))
                    .background(Color(0xFF00E676))
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(topEnd = 4.dp))
                    .background(Color(0xFF69F0AE))
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(bottomEnd = 4.dp))
                    .background(Color(0xFFB9F6CA))
            )
        }
    }
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) Color(0xFF13A263) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DrawerActionItem(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    badge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
