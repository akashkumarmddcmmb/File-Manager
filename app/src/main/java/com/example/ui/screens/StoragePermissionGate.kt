package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AppLanguage

@Composable
fun StoragePermissionGate(
    hasPermission: Boolean,
    language: AppLanguage,
    onPermissionGranted: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    
    // Check if permission gate was previously passed or runtime permissions are already granted
    val isAlreadyPassed = remember {
        hasPermission || prefs.getBoolean("permission_gate_passed", false) || checkRuntimePermissionsGranted(context)
    }

    var isInitialGatePassed by remember { mutableStateOf(isAlreadyPassed) }

    // Launcher for standard initial Android native runtime permissions
    val runtimePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Save flag permanently so this screen never appears again on future app opens
        prefs.edit().putBoolean("permission_gate_passed", true).apply()
        isInitialGatePassed = true
        onPermissionGranted()
    }

    if (isInitialGatePassed) {
        content()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF121316)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Green Folder Badge
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFF00C853)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Storage Folder",
                                tint = Color.Black,
                                modifier = Modifier.size(52.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Title
                        Text(
                            text = if (language == AppLanguage.HINDI) "स्टोरेज एक्सेस आवश्यक है" else "Storage Access Required",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Description
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "फ़ाइल्स (आकाश कुमार द्वारा) को आपके स्थानीय स्टोरेज पर मौजूद फोटो, वीडियो, म्यूज़िक और दस्तावेज़ों को ढूंढने, व्यवस्थित करने और प्रबंधित करने के लिए अनुमति चाहिए।"
                            else
                                "Files (by Akash Kumar) needs permission to find, organize, and manage photos, videos, music, and documents on your local storage.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            ),
                            color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Feature Highlights Card
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F23)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Feature 1: Storage Scan
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1B382B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FormatListBulleted,
                                            contentDescription = null,
                                            tint = Color(0xFF00C853),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "स्टोरेज स्कैन" else "Storage Scan",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "जंक फाइल्स ढूंढें और 1.8GB तक स्पेस खाली करें" else "Find junk files and free up space",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                            color = Color(0xFF9E9E9E)
                                        )
                                    }
                                }

                                // Feature 2: Safe Folder
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1B382B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color(0xFF00C853),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "सुरक्षित फ़ोल्डर" else "Safe Folder",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "अपनी निजी फाइलों को 256-बिट पिन से सुरक्षित करें" else "Protect private files with PIN encryption",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                            color = Color(0xFF9E9E9E)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Green Pill Grant Button ("अनुमति दें")
                    Button(
                        onClick = {
                            prefs.edit().putBoolean("permission_gate_passed", true).apply()
                            triggerNativeSystemPermissions(runtimePermissionsLauncher) {
                                isInitialGatePassed = true
                                onPermissionGranted()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "अनुमति दें" else "Grant Permission",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun checkRuntimePermissionsGranted(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    if (prefs.getBoolean("permission_gate_passed", false)) return true

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

private fun triggerNativeSystemPermissions(
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onDone: () -> Unit
) {
    val permsList = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permsList.add(Manifest.permission.POST_NOTIFICATIONS)
        permsList.add(Manifest.permission.READ_MEDIA_IMAGES)
        permsList.add(Manifest.permission.READ_MEDIA_VIDEO)
        permsList.add(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        permsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    try {
        launcher.launch(permsList.toTypedArray())
    } catch (e: Exception) {
        onDone()
    }
}
