package com.example.ui.modals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage

enum class PolicyType {
    PRIVACY,
    TERMS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalPoliciesModal(
    initialType: PolicyType = PolicyType.PRIVACY,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedType == PolicyType.PRIVACY) {
                                    if (language == AppLanguage.HINDI) "गोपनीयता नीति (Privacy Policy)" else "Privacy Policy"
                                } else {
                                    if (language == AppLanguage.HINDI) "सेवा की शर्तें (Terms & Conditions)" else "Terms & Conditions"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI) "आकाश कुमार द्वारा फाइल्स" else "Files by Akash Kumar",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = if (selectedType == PolicyType.PRIVACY) Icons.Default.PrivacyTip else Icons.Default.Gavel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp)),
                            color = if (selectedType == PolicyType.PRIVACY) MaterialTheme.colorScheme.primary else Color.Transparent,
                            onClick = { selectedType = PolicyType.PRIVACY }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "प्राइवेसी पॉलिसी" else "Privacy Policy",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (selectedType == PolicyType.PRIVACY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp)),
                            color = if (selectedType == PolicyType.TERMS) MaterialTheme.colorScheme.primary else Color.Transparent,
                            onClick = { selectedType = PolicyType.TERMS }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "नियम व शर्तें" else "Terms & Conditions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (selectedType == PolicyType.TERMS) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedType == PolicyType.PRIVACY) {
                        PrivacyPolicyContent(language)
                    } else {
                        TermsAndConditionsContent(language)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "मैं सहमत हूं और स्वीकार करता हूं" else "I Agree & Accept",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent(language: AppLanguage) {
    if (language == AppLanguage.HINDI) {
        PolicySectionCard(
            title = "1. 100% स्थानीय डेटा सुरक्षा (Local Data Guarantee)",
            icon = Icons.Default.Security
        ) {
            Text(
                text = "'फाइल्स बाय आकाश कुमार' (Files by Akash Kumar) आपकी गोपनीयता (Privacy) का पूरा ध्यान रखता है।\n\nआपकी सभी फाइलें, फोटो, वीडियो, म्यूजिक और दस्तावेज़ आपके अपने डिवाइस पर ही सुरक्षित रहते हैं। कोई भी फाइल बाहरी सर्वर पर अपलोड नहीं की जाती है।",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "2. आवश्यक अनुमतियां (Permissions Required)",
            icon = Icons.Default.PrivacyTip
        ) {
            Text(
                text = "• स्टोरेज अनुमति (MANAGE_EXTERNAL_STORAGE / Storage Permission): फाइलों को व्यवस्थित करने, जंक साफ करने और आर्काइव बनाने के लिए।\n• मीडिया अनुमति: केवल फोटो, वीडियो और ऑडियो चलाने के लिए।",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "3. सुरक्षित फ़ोल्डर (Safe Folder Security)",
            icon = Icons.Default.Lock
        ) {
            Text(
                text = "सुरक्षित फ़ोल्डर में रखी गई फाइलें आपके द्वारा चुने गए पिन (PIN) द्वारा सुरक्षित रहती हैं। आपका पिन केवल आपके डिवाइस में ही सेव रहता है।",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "4. डेवलपर संपर्क (Developer Contact)",
            icon = Icons.Default.Gavel
        ) {
            Text(
                text = "यदि आपके पास कोई प्रश्न है, तो बेझिझक संपर्क करें:\n\nईमेल: akashkumarmddcmmb@gmail.com\nडेवलपर: आकाश कुमार (Akash Kumar)",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        PolicySectionCard(
            title = "1. 100% Local Processing & Privacy First",
            icon = Icons.Default.Security
        ) {
            Text(
                text = "'Files by Akash Kumar' is engineered with absolute privacy in mind.\n\nAll core features including file scanning, junk cleaning, ZIP compression, media playback, and Safe Folder encryption run 100% locally on your Android device.\nNone of your personal files, photos, videos, or documents are ever uploaded to remote servers or shared with external parties.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "2. Device Permissions Usage",
            icon = Icons.Default.PrivacyTip
        ) {
            Text(
                text = "• Storage Access (MANAGE_EXTERNAL_STORAGE): Required strictly to index, categorize, clean junk files, and compress folders requested by the user.\n• Media Permissions: Used solely to provide smooth audio/video playback and full-screen image previews within the application.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "3. Safe Folder & PIN Protection",
            icon = Icons.Default.Lock
        ) {
            Text(
                text = "• Files placed inside the Safe Folder are protected with a custom PIN chosen by you.\n• Your PIN credentials and security question answers are saved locally in encrypted app storage. The developer has no access to your PIN or encrypted files.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "4. Contact & Support",
            icon = Icons.Default.Gavel
        ) {
            Text(
                text = "We do not sell, license, or monetize your personal file data.\n\nIf you have any questions or feedback regarding this Privacy Policy, please contact:\n• Email: akashkumarmddcmmb@gmail.com\n• Developer: Akash Kumar",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TermsAndConditionsContent(language: AppLanguage) {
    if (language == AppLanguage.HINDI) {
        PolicySectionCard(
            title = "1. शर्तों की स्वीकृति (Acceptance of Terms)",
            icon = Icons.Default.Gavel
        ) {
            Text(
                text = "'फाइल्स बाय आकाश कुमार' (Files by Akash Kumar) एप्लिकेशन का उपयोग करके आप इन शर्तों से सहमत होते हैं।",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "2. सही उपयोग (Acceptable Use)",
            icon = Icons.Default.Security
        ) {
            Text(
                text = "आप इस ऐप का उपयोग केवल वैध फाइल प्रबंधन, मीडिया प्लेबैक और आर्काइव कार्यों के लिए करने के लिए सहमत हैं।",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "3. फाइल सुरक्षा की जिम्मेदारी (File Safety)",
            icon = Icons.Default.PrivacyTip
        ) {
            Text(
                text = "ट्रैश (कचरा) से हमेशा के लिए हटाई गई फाइलें वापस नहीं लाई जा सकतीं। अपने सुरक्षित फ़ोल्डर का पिन (PIN) याद रखें।",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "4. सर्वाधिकार (Intellectual Property)",
            icon = Icons.Default.Gavel
        ) {
            Text(
                text = "इस एप्लिकेशन का कोड और डिज़ाइन पूर्णतः आकाश कुमार (Akash Kumar) के स्वामित्व में है।\n\nसंपर्क: akashkumarmddcmmb@gmail.com",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        PolicySectionCard(
            title = "1. Acceptance of Terms",
            icon = Icons.Default.Gavel
        ) {
            Text(
                text = "By downloading, installing, or using 'Files by Akash Kumar', you agree to be bound by these Terms and Conditions. If you do not agree to these terms, please refrain from using the application.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "2. Acceptable Use Policy",
            icon = Icons.Default.Security
        ) {
            Text(
                text = "• You agree to use this application solely for legitimate file management, junk cleaning, archive extraction, and media viewing on your Android device.\n• Misuse of storage permissions or attempting to compromise application security is strictly prohibited.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "3. User Responsibility & File Safety",
            icon = Icons.Default.PrivacyTip
        ) {
            Text(
                text = "• Permanent deletion from Trash cannot be undone by the application.\n• Users are solely responsible for safeguarding their Safe Folder PIN. The developer is not responsible for data loss due to forgotten PINs without recovery credentials.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PolicySectionCard(
            title = "4. Ownership & Intellectual Property",
            icon = Icons.Default.Gavel
        ) {
            Text(
                text = "All branding, source code, and design architecture of 'Files by Akash Kumar' are owned exclusively by developer Akash Kumar.\n\nFor legal or developer inquiries: akashkumarmddcmmb@gmail.com",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PolicySectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
