package com.example.ui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.model.FileItem

@Composable
fun ArchiveExtractModal(
    archiveFile: FileItem,
    language: AppLanguage,
    onConfirm: (destinationPath: String, password: String?, createSubfolder: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var destinationPath by remember { mutableStateOf("/storage/emulated/0/Download") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var createSubfolder by remember { mutableStateOf(true) }
    var testBeforeExtract by remember { mutableStateOf(true) }
    var overwriteMode by remember { mutableStateOf("OVERWRITE_ALL") }

    val folderName = remember(archiveFile) { archiveFile.name.substringBeforeLast(".") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B3B2B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "आर्काइव निकालें (Extract)" else "Extract Archive (${archiveFile.extension.uppercase()})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${archiveFile.name} (${archiveFile.formattedSize})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Destination Folder
                Text(
                    text = if (language == AppLanguage.HINDI) "निकालने का स्थान (Destination)" else "Extract Destination",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "/storage/emulated/0/Download" to "Downloads",
                        "/storage/emulated/0/Documents" to "Documents",
                        "/storage/emulated/0" to "Internal"
                    ).forEach { (path, label) ->
                        val isSelected = destinationPath == path
                        FilterChip(
                            selected = isSelected,
                            onClick = { destinationPath = path },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Checkbox: Create dedicated subfolder
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { createSubfolder = !createSubfolder },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = createSubfolder,
                        onCheckedChange = { createSubfolder = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "अलग फ़ोल्डर '$folderName/' में निकालें"
                        else
                            "Extract into '$folderName/'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Password Protection Input (for encrypted 7z / zip)
                Text(
                    text = if (language == AppLanguage.HINDI) "पासवर्ड (यदि एन्क्रिप्टेड है):" else "Password (if encrypted):",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    singleLine = true,
                    placeholder = { Text(if (language == AppLanguage.HINDI) "पासवर्ड खाली छोड़ सकते हैं" else "Leave blank if not protected") },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Checkbox: Test integrity before extracting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { testBeforeExtract = !testBeforeExtract },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = testBeforeExtract,
                        onCheckedChange = { testBeforeExtract = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "निकालने से पहले CRC32 टेस्ट करें" else "Verify CRC32 integrity before extract",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(destinationPath, passwordInput.ifBlank { null }, createSubfolder)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderZip,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "यहाँ निकालें" else "Extract Here",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
