package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.translate

@Composable
fun ShareScreen(
    language: AppLanguage,
    onSendFilesClick: () -> Unit,
    onReceiveFilesClick: () -> Unit
) {
    var showSendModal by remember { mutableStateOf(false) }
    var showReceiveModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Share Card
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B2332)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00B0FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Nearby Share",
                            tint = Color(0xFF00B0FF),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = language.translate("Nearby Share"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "बिना इंटरनेट के पास के दोस्तों को फोटो, वीडियो और फाइलें तेजी से भेजें।"
                        else if (language == AppLanguage.SPANISH)
                            "Envíe y reciba archivos rápidamente sin Internet utilizando Wi-Fi Direct y Bluetooth."
                        else if (language == AppLanguage.FRENCH)
                            "Envoyez et recevez des fichiers rapidement sans Internet grâce au Wi-Fi Direct et Bluetooth."
                        else if (language == AppLanguage.ARABIC)
                            "أرسل واستقبل الملفات بسرعة بدون إنترنت باستخدام واي فاي المباشر وبلوتوث."
                        else
                            "Send and receive files fast without internet using Wi-Fi Direct & Bluetooth.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB0BEC5),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showSendModal = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "Send")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = language.translate("Send"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Button(
                            onClick = { showReceiveModal = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Receive")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = language.translate("Receive"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Fast Wi-Fi Direct Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = null,
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = language.translate("High-Speed Wi-Fi Transfer"),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = language.translate("Speeds up to 480 Mbps"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "• मोबाइल डेटा (इन्टरनेट) की आवश्यकता नहीं है।\n• एंड-टू-एंड एन्क्रिप्टेड पीयर-टू-पीयर ट्रांसफर।\n• क्यूआर कोड (QR Code) से तुरंत पेयरिंग।"
                        else
                            "• No mobile data or internet connection required.\n• End-to-end encrypted peer-to-peer transfer.\n• Instant QR code scanning pairing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSendModal) {
        AlertDialog(
            onDismissRequest = { showSendModal = false },
            title = {
                Text(
                    text = language.translate("Searching for Nearby Devices..."),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "कृपया प्राप्तकर्ता के फोन में 'प्राप्त करें' (Receive) बटन दबाएं।"
                        else if (language == AppLanguage.SPANISH)
                            "Por favor, pídale al destinatario que toque 'Recibir' en su dispositivo."
                        else if (language == AppLanguage.FRENCH)
                            "Veuillez demander au destinataire d'appuyer sur 'Recevoir' sur son appareil."
                        else if (language == AppLanguage.ARABIC)
                            "يرجى الطلب من المستلم الضغط على 'استلام' في جهازه."
                        else
                            "Please ask the recipient to tap 'Receive' on their device.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showSendModal = false }) {
                    Text(language.translate("Cancel"))
                }
            }
        )
    }

    if (showReceiveModal) {
        AlertDialog(
            onDismissRequest = { showReceiveModal = false },
            title = {
                Text(
                    text = language.translate("Ready to Receive Files"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "QR Code",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "भेजने वाला इस क्यूआर कोड को स्कैन कर सकता है।"
                        else if (language == AppLanguage.SPANISH)
                            "El remitente puede escanear este código QR para iniciar la transferencia."
                        else if (language == AppLanguage.FRENCH)
                            "L'expéditeur peut scanner ce code QR pour lancer le transfert."
                        else if (language == AppLanguage.ARABIC)
                            "يمكن للمرسل مسح رمز الاستجابة السريعة (QR) لبدء النقل."
                        else
                            "The sender can scan this QR code to initiate transfer.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showReceiveModal = false }) {
                    Text(language.translate("Done"))
                }
            }
        )
    }
}
