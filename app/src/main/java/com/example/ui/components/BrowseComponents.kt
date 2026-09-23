package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.FileCategoryType
import com.example.model.FileItem
import com.example.model.StorageDeviceInfo

@Composable
fun RecentFileCard(
    file: FileItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column {
            // Media Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF14171C)),
                contentAlignment = Alignment.Center
            ) {
                val ext = file.extension.lowercase()
                val isImage = file.category == FileCategoryType.IMAGES || ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
                val isVideo = file.category == FileCategoryType.VIDEOS || ext in listOf("mp4", "mkv", "avi", "mov", "webm")
                val localFile = java.io.File(file.path)
                val imageModel = if (localFile.exists() && localFile.canRead() && localFile.length() > 0) {
                    localFile
                } else {
                    when {
                        isImage -> "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=400&q=80"
                        isVideo -> "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&q=80"
                        else -> null
                    }
                }

                if (imageModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = file.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Category/Type Badge at top left
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = when (file.category) {
                        FileCategoryType.VIDEOS -> Color(0xFFD93025)
                        FileCategoryType.IMAGES -> Color(0xFF00C853)
                        FileCategoryType.AUDIO -> Color(0xFFF9AB00)
                        else -> Color(0xFF1A73E8)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (file.category) {
                                FileCategoryType.VIDEOS -> Icons.Default.Videocam
                                FileCategoryType.IMAGES -> Icons.Default.Image
                                FileCategoryType.AUDIO -> Icons.Default.Audiotrack
                                else -> Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = file.extension.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Center Play Icon for Video / Music or Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (file.category == FileCategoryType.VIDEOS) Icons.Filled.PlayArrow else Icons.Filled.Visibility,
                        contentDescription = "Preview",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Info below
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = file.formattedSize,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryGridItem(
    category: FileCategoryType,
    title: String,
    sizeText: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val (iconBgColor, iconColor) = when (category) {
        FileCategoryType.DOWNLOADS -> if (isDark) Color(0xFF004A77) to Color(0xFFA8C7FA) else Color(0xFFD3E3FD) to Color(0xFF0B57D0)
        FileCategoryType.IMAGES -> if (isDark) Color(0xFF0F5223) to Color(0xFF81C995) else Color(0xFFC4EED0) to Color(0xFF1E8E3E)
        FileCategoryType.VIDEOS -> if (isDark) Color(0xFF5C1D1D) to Color(0xFFF28B82) else Color(0xFFFAD2CF) to Color(0xFFD93025)
        FileCategoryType.AUDIO -> if (isDark) Color(0xFF4D3800) to Color(0xFFFDD663) else Color(0xFFFEEFC3) to Color(0xFFE37400)
        FileCategoryType.DOCUMENTS -> if (isDark) Color(0xFF004A77) to Color(0xFFA8C7FA) else Color(0xFFD3E3FD) to Color(0xFF0B57D0)
        FileCategoryType.APPS -> if (isDark) Color(0xFF491C75) to Color(0xFFC58AF9) else Color(0xFFE8D0FB) to Color(0xFF9334E6)
        FileCategoryType.ARCHIVES -> if (isDark) Color(0xFF004D40) to Color(0xFF80CBC4) else Color(0xFFB2DFDB) to Color(0xFF00796B)
        FileCategoryType.LARGE_FILES -> if (isDark) Color(0xFF283593) to Color(0xFF9FA8DA) else Color(0xFFC5CAE9) to Color(0xFF3949AB)
    }

    val icon = when (category) {
        FileCategoryType.DOWNLOADS -> Icons.Filled.Download
        FileCategoryType.IMAGES -> Icons.Filled.Image
        FileCategoryType.VIDEOS -> Icons.Filled.VideoLibrary
        FileCategoryType.AUDIO -> Icons.Filled.Audiotrack
        FileCategoryType.DOCUMENTS -> Icons.Filled.Description
        FileCategoryType.APPS -> Icons.Filled.Apps
        FileCategoryType.ARCHIVES -> Icons.Filled.FolderZip
        FileCategoryType.LARGE_FILES -> Icons.Filled.DataSaverOn
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sizeText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CollectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StorageDeviceCard(
    device: StorageDeviceInfo,
    languageTitle: String,
    isHindi: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (device.isExternal) Color(0xFF381F4E) else Color(0xFF1E2D4A)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (device.isExternal) Icons.Filled.SdCard else Icons.Filled.Smartphone,
                    contentDescription = device.nameEn,
                    tint = if (device.isExternal) Color(0xFFD1B7FF) else Color(0xFF8AB4F8),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = languageTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (device.badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF532468)
                        ) {
                            Text(
                                text = device.badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE9C8FF),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                val spaceText = if (isHindi) {
                    "${device.totalFormatted} में से ${device.freeFormatted} खाली"
                } else {
                    "${device.freeFormatted} free of ${device.totalFormatted}"
                }
                Text(
                    text = spaceText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${device.usedPercent}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (device.isExternal) Color(0xFFD1B7FF) else Color(0xFF75F9A7)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { device.usedPercent / 100f },
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (device.isExternal) Color(0xFFD1B7FF) else Color(0xFF00C853),
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}
