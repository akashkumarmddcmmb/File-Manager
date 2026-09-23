package com.example.ui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnScreenPermissionModal(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onPermissionsUpdated: () -> Unit
) {
    var storageAccess by remember { mutableStateOf(true) }
    var mediaAccess by remember { mutableStateOf(true) }
    var audioAccess by remember { mutableStateOf(true) }
    var notificationAccess by remember { mutableStateOf(true) }
    var cameraAccess by remember { mutableStateOf(true) }
    var installApkAccess by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "ऑनस्रीन अनुमति कंट्रोल" else "On-Screen Permission Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) "बिना किसी बाहरी विंडो के ऑन-स्क्रीन एक्सेस दें" else "Direct in-app access without external popups",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "सभी अनुमतियां सीधे इसी स्क्रीन पर ऑन/ऑफ की जा सकती हैं। कोई दूसरा पॉप-अप या विंडो नहीं खुलेगा।"
                        else
                            "All permissions can be toggled on-screen directly. No secondary popup window or external app settings required.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Permissions list with inline switches
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    PermissionSwitchRow(
                        title = if (language == AppLanguage.HINDI) "ऑनस्रीन स्टोरेज व ऑल फाइल्स एक्सेस" else "All Files Storage Access",
                        subtitle = if (language == AppLanguage.HINDI) "इंटरनल व एसडी कार्ड की सभी फाइलें पढ़ें व प्रबंधित करें" else "Read and manage all files on storage",
                        icon = Icons.Default.Folder,
                        checked = storageAccess,
                        onCheckedChange = {
                            storageAccess = it
                            onPermissionsUpdated()
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    PermissionSwitchRow(
                        title = if (language == AppLanguage.HINDI) "मीडिया व फोटो/वीडियो एक्सेस" else "Photos & Videos Gallery Access",
                        subtitle = if (language == AppLanguage.HINDI) "गैलरी के चित्र और वीडियो सीधे ऑन-स्क्रीन देखें" else "View gallery images and videos directly",
                        icon = Icons.Default.Image,
                        checked = mediaAccess,
                        onCheckedChange = { mediaAccess = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    PermissionSwitchRow(
                        title = if (language == AppLanguage.HINDI) "ऑडियो व म्यूजिक प्लेबैक" else "Audio & Music Player Access",
                        subtitle = if (language == AppLanguage.HINDI) "गाने व ऑडियो फाइलें ऑन-स्क्रीन प्ले करें" else "Play music and audio files on-screen",
                        icon = Icons.Default.Audiotrack,
                        checked = audioAccess,
                        onCheckedChange = { audioAccess = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    PermissionSwitchRow(
                        title = if (language == AppLanguage.HINDI) "ऑनस्रीन नोटिफिकेशन" else "App Notifications",
                        subtitle = if (language == AppLanguage.HINDI) "क्लीनअप व ट्रांसफर अलर्ट ऑन-स्क्रीन प्राप्त करें" else "Receive cleanup and transfer alerts",
                        icon = Icons.Default.Notifications,
                        checked = notificationAccess,
                        onCheckedChange = { notificationAccess = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    PermissionSwitchRow(
                        title = if (language == AppLanguage.HINDI) "कैमरा व दस्तावेज़ स्कैनर" else "Camera & Document Scan",
                        subtitle = if (language == AppLanguage.HINDI) "क्यूआर कोड व दस्तावेज़ स्कैनिंग" else "QR code and document scanning",
                        icon = Icons.Default.CameraAlt,
                        checked = cameraAccess,
                        onCheckedChange = { cameraAccess = it }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    PermissionSwitchRow(
                        title = if (language == AppLanguage.HINDI) "एपीके (APK) इंस्टॉल अनुमति" else "APK App Installer Permission",
                        subtitle = if (language == AppLanguage.HINDI) "डाउनलोड की गई ऐप्स सीधे इंस्टॉल करें" else "Install downloaded apps directly",
                        icon = Icons.Default.Android,
                        checked = installApkAccess,
                        onCheckedChange = { installApkAccess = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onPermissionsUpdated()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "अनुमतियां सुरक्षित करें (Done)" else "Save & Apply On-Screen",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PermissionSwitchRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (checked) Color(0xFF00C853).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) Color(0xFF00C853) else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
