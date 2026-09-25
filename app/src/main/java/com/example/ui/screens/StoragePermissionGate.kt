package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onOpenSafeFolder: () -> Unit = {},
    onOpenClean: () -> Unit = {},
    onPermissionGranted: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    // Check if permission gate was previously passed and standard permissions are currently granted.
    val isAlreadyPassed = remember {
        prefs.getBoolean("permission_gate_passed", false) && checkRuntimePermissionsGranted(context)
    }

    var isInitialGatePassed by remember { mutableStateOf(isAlreadyPassed) }

    // Track which action was requested when launching permissions
    var pendingAction by remember { mutableStateOf(PendingPermissionAction.NONE) }

    // Launcher for standard in-app Android native runtime permissions
    val runtimePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Check if storage permission was granted on-screen by the user
        val isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            results[Manifest.permission.READ_MEDIA_AUDIO] == true ||
            results[Manifest.permission.READ_MEDIA_VIDEO] == true ||
            results[Manifest.permission.READ_MEDIA_IMAGES] == true
        } else {
            results[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }

        if (isGranted) {
            prefs.edit().putBoolean("permission_gate_passed", true).apply()
            isInitialGatePassed = true
            when (pendingAction) {
                PendingPermissionAction.CLEAN -> onOpenClean()
                PendingPermissionAction.SAFE_FOLDER -> onOpenSafeFolder()
                else -> onPermissionGranted()
            }
            pendingAction = PendingPermissionAction.NONE
        } else {
            val deniedMsg = if (language == AppLanguage.HINDI) {
                "स्टोरेज की अनुमति नहीं दी गई! कृपया काम करने के लिए अनुमति दें।"
            } else {
                "Storage permission was denied! Please grant permission to proceed."
            }
            android.widget.Toast.makeText(context, deniedMsg, android.widget.Toast.LENGTH_LONG).show()
            pendingAction = PendingPermissionAction.NONE
        }
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
                        // Green Folder Badge (Interactive, triggers in-app runtime permission popup)
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFF00C853))
                                .clickable {
                                    if (checkRuntimePermissionsGranted(context)) {
                                        prefs.edit().putBoolean("permission_gate_passed", true).apply()
                                        isInitialGatePassed = true
                                        onPermissionGranted()
                                    } else {
                                        pendingAction = PendingPermissionAction.DEFAULT
                                        triggerNativeSystemPermissions(runtimePermissionsLauncher)
                                    }
                                },
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
                                "फ़ाइल्स को आपके लोकल स्टोरेज पर मौजूद फोटो, वीडियो, म्यूज़िक और दस्तावेज़ों को ढूंढने, व्यवस्थित करने और प्रबंधित करने के लिए अनुमति चाहिए।"
                            else
                                "Files needs permission to find, organize, and manage photos, videos, music, and documents on your local storage.",
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF18191C)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Feature 1: Storage Scan
                                Surface(
                                    onClick = {
                                        if (checkRuntimePermissionsGranted(context)) {
                                            prefs.edit().putBoolean("permission_gate_passed", true).apply()
                                            isInitialGatePassed = true
                                            onOpenClean()
                                        } else {
                                            pendingAction = PendingPermissionAction.CLEAN
                                            triggerNativeSystemPermissions(runtimePermissionsLauncher)
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF232429),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (language == AppLanguage.HINDI) "स्टोरेज स्कैन" else "Storage Scan",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                 ),
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (language == AppLanguage.HINDI) "जंक फाइल्स ढूंढें और स्पेस खाली करें" else "Find junk files and free up space",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = Color(0xFF9E9E9E)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = Color(0xFF757575),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Feature 2: Safe Folder
                                Surface(
                                    onClick = {
                                        if (checkRuntimePermissionsGranted(context)) {
                                            prefs.edit().putBoolean("permission_gate_passed", true).apply()
                                            isInitialGatePassed = true
                                            onOpenSafeFolder()
                                        } else {
                                            pendingAction = PendingPermissionAction.SAFE_FOLDER
                                            triggerNativeSystemPermissions(runtimePermissionsLauncher)
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF232429),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1B382B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFF00C853),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (language == AppLanguage.HINDI) "सुरक्षित फ़ोल्डर" else "Safe Folder",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                ),
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (language == AppLanguage.HINDI) "अपनी निजी फ़ाइलें छुपाएं और सुरक्षित रखें" else "Hide and protect your private files",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = Color(0xFF9E9E9E)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = Color(0xFF757575),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Green Pill Grant Button ("अनुमति दें") - Triggers standard runtime popup directly in-app
                    Button(
                        onClick = {
                            if (checkRuntimePermissionsGranted(context)) {
                                prefs.edit().putBoolean("permission_gate_passed", true).apply()
                                isInitialGatePassed = true
                                onPermissionGranted()
                            } else {
                                pendingAction = PendingPermissionAction.DEFAULT
                                triggerNativeSystemPermissions(runtimePermissionsLauncher)
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

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                icon = { Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF00C853)) },
                title = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "ऐप भाषा चुनें / Choose Language" else "Select App Language",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 540.dp)
                    ) {
                        items(AppLanguage.values().toList()) { lang ->
                            val isSelected = language == lang
                            Surface(
                                onClick = {
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF1B382B) else Color(0xFF2A2B30),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(
                                                text = lang.nativeName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (isSelected) Color(0xFF00C853) else Color.White
                                            )
                                            Text(
                                                text = lang.englishName,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color(0xFFA7F3D0) else Color(0xFF9E9E9E)
                                            )
                                        }
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            onSelectLanguage(lang)
                                            showLanguageDialog = false
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF00C853),
                                            unselectedColor = Color(0xFF9E9E9E)
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(if (language == AppLanguage.HINDI) "बंद करें" else "Close", color = Color(0xFF00C853))
                    }
                },
                containerColor = Color(0xFF1E1F23),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

private fun checkStandardPermissionsGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}

private fun checkRuntimePermissionsGranted(context: Context): Boolean {
    return checkStandardPermissionsGranted(context)
}

private fun triggerNativeSystemPermissions(
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val permsList = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permsList.add(Manifest.permission.POST_NOTIFICATIONS)
        permsList.add(Manifest.permission.READ_MEDIA_IMAGES)
        permsList.add(Manifest.permission.READ_MEDIA_VIDEO)
        permsList.add(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        permsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        permsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    try {
        launcher.launch(permsList.toTypedArray())
    } catch (e: Exception) {
        // Safe catch block
    }
}

enum class PendingPermissionAction {
    NONE,
    DEFAULT,
    CLEAN,
    SAFE_FOLDER
}
