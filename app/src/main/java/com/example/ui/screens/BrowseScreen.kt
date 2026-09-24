package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CategoryGridItem
import com.example.ui.components.CollectionCard
import com.example.ui.components.RecentFileCard
import com.example.ui.components.StorageDeviceCard

@Composable
fun BrowseScreen(
    recentFiles: List<FileItem>,
    storageDevices: List<StorageDeviceInfo>,
    language: AppLanguage,
    onCategoryClick: (FileCategoryType) -> Unit,
    onDeviceClick: (StorageDeviceInfo) -> Unit,
    onRecentFileClick: (FileItem) -> Unit,
    onOpenMusicPlayer: () -> Unit,
    onOpenVideoPlayer: () -> Unit,
    onOpenCompressDialog: () -> Unit,
    onOpenSafeFolder: () -> Unit,
    onOpenStarred: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenFeatureList: () -> Unit,
    onRefreshDevices: () -> Unit,
    getCategoryText: (FileCategoryType) -> String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Recent Files Section
        if (recentFiles.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "हाल ही की फाइलें" else "Recent files",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) "सभी देखें >" else "See all >",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.clickable {
                                onCategoryClick(FileCategoryType.VIDEOS)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 8.dp)
                    ) {
                        items(recentFiles, key = { it.id }) { file ->
                            RecentFileCard(
                                file = file,
                                onClick = { onRecentFileClick(file) }
                            )
                        }
                    }
                }
            }
        }

        // 2. Categories Section
        item {
            Column {
                Text(
                    text = if (language == AppLanguage.HINDI) "श्रेणियां" else "Categories",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                val categories = listOf(
                    FileCategoryType.DOWNLOADS to FileCategoryType.IMAGES,
                    FileCategoryType.VIDEOS to FileCategoryType.AUDIO,
                    FileCategoryType.DOCUMENTS to FileCategoryType.APPS
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.forEach { (cat1, cat2) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryGridItem(
                                    category = cat1,
                                    title = if (language == AppLanguage.HINDI) cat1.titleHi else cat1.titleEn,
                                    sizeText = getCategoryText(cat1),
                                    onClick = { onCategoryClick(cat1) }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryGridItem(
                                    category = cat2,
                                    title = if (language == AppLanguage.HINDI) cat2.titleHi else cat2.titleEn,
                                    sizeText = getCategoryText(cat2),
                                    onClick = { onCategoryClick(cat2) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Collections Section
        item {
            Column {
                Text(
                    text = if (language == AppLanguage.HINDI) "संग्रह" else "Collections",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CollectionCard(
                        title = if (language == AppLanguage.HINDI) "पसंदीदा फाइलें" else "Starred files",
                        subtitle = if (language == AppLanguage.HINDI) "0 आइटम" else "0 items",
                        icon = Icons.Filled.Star,
                        iconColor = Color(0xFFF9AB00),
                        iconBgColor = Color(0xFF473618),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenStarred
                    )
                    CollectionCard(
                        title = if (language == AppLanguage.HINDI) "सुरक्षित फ़ोल्डर" else "Safe folder",
                        subtitle = "Protected PIN",
                        icon = Icons.Filled.Shield,
                        iconColor = Color(0xFF00C853),
                        iconBgColor = Color(0xFF1B3B2B),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSafeFolder
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                CollectionCard(
                    title = if (language == AppLanguage.HINDI) "ट्रैश (कचरा)" else "Trash",
                    subtitle = if (language == AppLanguage.HINDI) "0 आइटम" else "0 items",
                    icon = Icons.Filled.Delete,
                    iconColor = Color(0xFF9AA0A6),
                    iconBgColor = Color(0xFF2C323D),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenTrash
                )
            }
        }

        // 4. Storage Devices Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "स्टोरेज डिवाइसेस" else "Storage devices",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier.clickable(onClick = onRefreshDevices),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF4FC3F7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "रिफ्रेश करें" else "Refresh devices",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4FC3F7)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    storageDevices.forEach { device ->
                        StorageDeviceCard(
                            device = device,
                            languageTitle = if (language == AppLanguage.HINDI) device.nameHi else device.nameEn,
                            isHindi = (language == AppLanguage.HINDI),
                            onClick = { onDeviceClick(device) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
