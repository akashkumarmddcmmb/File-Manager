package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

data class FeatureItem(
    val titleEn: String,
    val titleHi: String,
    val descEn: String,
    val descHi: String,
    val icon: ImageVector,
    val iconBgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureListScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler(enabled = true) {
        onBack()
    }
    val features = listOf(
        FeatureItem(
            titleEn = "Full Local File System Scanner",
            titleHi = "पूर्ण स्थानीय फ़ाइल सिस्टम स्कैनर",
            descEn = "Deep recursive scanning for internal storage and external SD cards without internet dependency.",
            descHi = "बिना इंटरनेट के आंतरिक संग्रहण और एसडी कार्ड के लिए गहरा फ़ाइल स्कैनर।",
            icon = Icons.Default.FolderOpen,
            iconBgColor = Color(0xFF1A73E8)
        ),
        FeatureItem(
            titleEn = "High Fidelity Music Player & Equalizer",
            titleHi = "हाई फिडेलिटी म्यूज़िक प्लेयर और इक्वालाइज़र",
            descEn = "Embedded audio engine with background notification controls, equalizer presets, and sleep timer.",
            descHi = "बैकग्राउंड प्लेबैक, साउंड इक्वालाइज़र और स्लीप टाइमर के साथ पूर्ण ऑडियो प्लेयर।",
            icon = Icons.Default.MusicNote,
            iconBgColor = Color(0xFF00C853)
        ),
        FeatureItem(
            titleEn = "FHD Video Player with Aspect Ratio",
            titleHi = "एफएचडी वीडियो प्लेयर और एस्पेक्ट रेश्यो",
            descEn = "Smooth video playback supporting gesture controls, subtitle toggles, speed adjustment, and lock mode.",
            descHi = "जेस्चर नियंत्रण, सबटाइटल, स्पीड नियंत्रण और स्क्रीन लॉक के साथ एचडी वीडियो प्लेयर।",
            icon = Icons.Default.Videocam,
            iconBgColor = Color(0xFFD93025)
        ),
        FeatureItem(
            titleEn = "PDF & Document Viewer",
            titleHi = "पीडीएफ और दस्तावेज़ दर्शक",
            descEn = "Full-featured PDF reader supporting Night Reading Mode, font scaling, page jumping, and text search.",
            descHi = "नाइट रीडिंग मोड, टेक्स्ट साइज और सर्च सुविधाओं के साथ दस्तावेज़ रीडर।",
            icon = Icons.Default.Description,
            iconBgColor = Color(0xFFF9AB00)
        ),
        FeatureItem(
            titleEn = "ZIP Archive Compression & Extraction",
            titleHi = "जिप आर्काइव संपीड़न और निष्कर्षण",
            descEn = "Create and extract ZIP archives with custom compression levels, password encryption, and multi-part split options.",
            descHi = "पासवर्ड सुरक्षा और कम्प्रेशन स्तर के साथ जिप फाइल बनाएं और निकालें।",
            icon = Icons.Default.FolderZip,
            iconBgColor = Color(0xFF7C4DFF)
        ),
        FeatureItem(
            titleEn = "Protected Safe Folder",
            titleHi = "सुरक्षित गुप्त फ़ोल्डर",
            descEn = "Hide sensitive photos and documents behind a custom 4-8 digit password and security question recovery.",
            descHi = "4-8 अंकों के पासवर्ड और रिकवरी प्रश्न द्वारा निजी फाइलों को छिपाएं।",
            icon = Icons.Default.Shield,
            iconBgColor = Color(0xFF00C853)
        ),
        FeatureItem(
            titleEn = "Smart Junk File Cleaner",
            titleHi = "स्मार्ट जंक फाइल क्लिनर",
            descEn = "One-tap cleanup for app cache, temporary files, APK leftovers, and empty folders.",
            descHi = "ऐप कैश, अस्थाई फाइलों और खाली फ़ोल्डरों को एक टैप में साफ करें।",
            icon = Icons.Default.CleaningServices,
            iconBgColor = Color(0xFF1A73E8)
        ),
        FeatureItem(
            titleEn = "High-Speed Wi-Fi P2P File Transfer",
            titleHi = "हाई-स्पीड वाई-फाई पी2पी फाइल शेयरिंग",
            descEn = "Nearby file sharing between Android devices at gigabit speeds with QR code pairing.",
            descHi = "क्यूआर कोड पेयरिंग के माध्यम से बिना इंटरनेट फाइलों का तीव्र आदान-प्रदान।",
            icon = Icons.Default.SwapHoriz,
            iconBgColor = Color(0xFF00B0FF)
        )
    )

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
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "ऐप की विशेषताएं (Features)" else "App Capabilities & Features",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) "आकाश कुमार द्वारा विकसित" else "Developed by Akash Kumar",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.primary
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(features) { feature ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(feature.iconBgColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = null,
                                tint = feature.iconBgColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.HINDI) feature.titleHi else feature.titleEn,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) feature.descHi else feature.descEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Files by Akash Kumar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "यह एक पूर्णतः क्रियाशील और ऑप्टिमाइज्ड फाइल मैनेजर एप्लीकेशन है।"
                            else
                                "Fully functional, performant, and feature-rich Android file management application.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
