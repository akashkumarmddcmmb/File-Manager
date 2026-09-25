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
                        text = when (language) {
                            AppLanguage.HINDI -> "बिना इंटरनेट के पास के दोस्तों को फोटो, वीडियो और फाइलें तेजी से भेजें।"
                            AppLanguage.SPANISH -> "Envíe y reciba archivos rápidamente sin Internet utilizando Wi-Fi Direct y Bluetooth."
                            AppLanguage.MARATHI -> "वाय-फाय डायरेक्ट आणि ब्लूटूथचा वापर करून इंटरनेटशिवाय जलद फाइल्स पाठवा आणि मिळवा."
                            AppLanguage.BENGALI -> "ওয়াই-ফাই ডাইরেক্ট এবং ব্লুটুথ ব্যবহার করে ইন্টারনেট ছাড়াই দ্রুত ফাইল পাঠান এবং গ্রহণ করুন।"
                            AppLanguage.TAMIL -> "வைஃபை டைரக்ட் மற்றும் புளூடூத் பயன்படுத்தி இணையம் இல்லாமல் கோப்புகளை விரைவாக அனுப்பவும் பெறவும்."
                            AppLanguage.TELUGU -> "వై-ఫై డైరెక్ట్ మరియు బ్లూటూత్ ఉపయోగించి ఇంటర్నెట్ లేకుండా ఫైల్‌లను వేగంగా పంపండి మరియు స్వీకరించండి."
                            AppLanguage.GUJARATI -> "વાઇ-ફાઇ ડાયરેક્ટ અને બ્લૂટૂથનો ઉપયોગ કરીને ઇન્ટरનેટ વિના ઝડપથી ફાઇલો મોકલો અને મેળવો."
                            AppLanguage.PUNJABI -> "ਵਾਈ-ਫਾਈ ਡਾਇਰੈਕਟ ਅਤੇ ਬਲੂਟੁੱਥ ਦੀ ਵਰਤੋਂ ਕਰਕੇ ਇੰਟਰਨੈਟ ਤੋਂ ਬਿਨਾਂ ਤੇਜ਼ੀ ਨਾਲ ਫਾਈਲਾਂ ਭੇਜੋ ਅਤੇ ਪ੍ਰਾਪत ਕਰੋ।"
                            AppLanguage.FRENCH -> "Envoyez et recevez des fichiers rapidement sans Internet grâce au Wi-Fi Direct et Bluetooth."
                            AppLanguage.ARABIC -> "أرسل واستقبل الملفات بسرعة بدون إنترنت باستخدام واي فاي المباشر وبلوتوث."
                            else -> "Send and receive files fast without internet using Wi-Fi Direct & Bluetooth."
                        },
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
                        text = when (language) {
                            AppLanguage.HINDI -> "• मोबाइल डेटा (इन्टरनेट) की आवश्यकता नहीं है।\n• एंड-टू-एंड एन्क्रिप्टेड पीयर-टू-पीयर ट्रांसफर।\n• क्यूआर कोड (QR Code) से तुरंत पेयरिंग।"
                            AppLanguage.SPANISH -> "• No se requiere conexión a Internet o datos móviles.\n• Transferencia cifrada de extremo a extremo.\n• Emparejamiento instantáneo mediante código QR."
                            AppLanguage.MARATHI -> "• मोबाईल डेटा किंवा इंटरनेट कनेक्शनची आवश्यकता नाही.\n• एंड-टू-एंड एनक्रिप्टेड पीअर-टू-पीअर ट्रान्सफर.\n• झटपट QR कोड स्कॅनिंग पेअरिंग."
                            AppLanguage.BENGALI -> "• কোনো মোবাইল ডাটা বা ইন্টারনেট সংযোগের প্রয়োজন নেই।\n• এন্ড-টু-ধাপে এনক্রিপ্ট করা পিয়ার-টু-পিয়ার স্থানান্তর।\n• তাত্ক্ষণিক কিউআর কোড স্ক্যানিং জোড়া।"
                            AppLanguage.TAMIL -> "• மொபைல் டேட்டா அல்லது இணைய இணைப்பு தேவையில்லை.\n• எண்ட்-டு-எண்ட் என்க்ரிப்ட் செய்யப்பட்ட பியர்-டு-பியர் பரிமாற்றம்.\n• உடனடி QR குறியீடு ஸ்கேனிங் இணைத்தல்."
                            AppLanguage.TELUGU -> "• మొబైల్ డేటా లేదా ఇంటర్నెట్ కనెక్షన్ అవసరం లేదు.\n• ఎండ్-టు-ఎండ్ ఎన్‌క్రిప్టెడ్ పీర్-టు-పీర్ బదిలీ.\n• తక్షణ QR కోడ్ స్కానింగ్ జత చేయడం."
                            AppLanguage.GUJARATI -> "• મોબાઇલ ડેટા અથવા ઇન્ટરનેટ કનેક્શનની જરૂર નથી.\n• એન્ડ-ટુ-એન્ડ એન્ક્રિપ્ટેડ પીઅર-ટુ-પીઅર ટ્રાન્સફર.\n• ઇન્સ્ટન્ટ QR કોડ સ્કેનિંગ જોડી."
                            AppLanguage.PUNJABI -> "• ਕੋਈ ਮੋਬਾਈਲ ਡੇਟਾ ਜਾਂ ਇੰਟਰਨੈਟ ਕਨੈਕਸ਼ਨ ਦੀ ਲੋੜ ਨਹੀਂ ਹੈ।\n• ਐਂਡ-ਟੂ-ਐਂਡ ਐਨਕ੍ਰਿਪਟਡ ਪੀਅਰ-ਟੂ-ਪੀਅਰ ਟ੍ਰਾਂਸਫਰ।\n• ਤੁਰੰਤ QR ਕੋਡ ਸਕੈਨਿੰਗ ਜੋੜੀ।"
                            AppLanguage.FRENCH -> "• Aucune donnée mobile ou connexion Internet requise.\n• Transfert sécurisé chiffré de bout en bout.\n• Association instantanée par code QR."
                            AppLanguage.ARABIC -> "• لا يتطلب اتصالاً بالإنترنت أو بيانات الهاتف المحمول.\n• نقل مشفر من الطرف إلى الطرف بين الأجهزة.\n• اقتران فوري عبر مسح رمز QR."
                            else -> "• No mobile data or internet connection required.\n• End-to-end encrypted peer-to-peer transfer.\n• Instant QR code scanning pairing."
                        },
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
                        text = when (language) {
                            AppLanguage.HINDI -> "कृपया प्राप्तकर्ता के फोन में 'प्राप्त करें' (Receive) बटन दबाएं।"
                            AppLanguage.SPANISH -> "Por favor, pídale al destinatario que toque 'Recibir' en su dispositivo."
                            AppLanguage.MARATHI -> "कृपया प्राप्तकर्त्याला त्यांच्या डिव्हाइसवर 'मिळवा' (Receive) टॅप करण्यास सांगा."
                            AppLanguage.BENGALI -> "দয়া করে প্রাপককে তার ডিভাইসে 'গ্রহণ করুন' (Receive) আলতো চাপতে বলুন।"
                            AppLanguage.TAMIL -> "பெறுநரை அவரது சாதனத்தில் 'பெறு' (Receive) என்பதைத் தட்டுமாறு கேட்கவும்."
                            AppLanguage.TELUGU -> "దయచేసి గ్రహీతను వారి పరికరంలో 'స్వీకరించు' (Receive) నొక్కమని అడగండి."
                            AppLanguage.GUJARATI -> "કૃપા કરીને પ્રાપ્તકર્તાને તેમના ઉપકરણ પર 'મેળવો' (Receive) ટેપ કરવા કહો."
                            AppLanguage.PUNJABI -> "ਕਿਰਪਾ ਕਰਕੇ ਪ੍ਰਾਪਤਕਰਤਾ ਨੂੰ ਉਹਨਾਂ ਦੇ ਡਿਵਾਈਸ 'ਤੇ 'ਪ੍ਰਾਪਤ ਕਰੋ' (Receive) 'ਤੇ ਟੈਪ ਕਰਨ ਲਈ ਕਹੋ।"
                            AppLanguage.FRENCH -> "Veuillez demander au destinataire d'appuyer sur 'Recevoir' sur son appareil."
                            AppLanguage.ARABIC -> "يرجى الطلب من المستلم الضغط على 'استلام' في جهازه."
                            else -> "Please ask the recipient to tap 'Receive' on their device."
                        },
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
                        text = when (language) {
                            AppLanguage.HINDI -> "भेजने वाला इस क्यूआर कोड को स्कैन कर सकता है।"
                            AppLanguage.SPANISH -> "El remitente puede escanear este código QR para iniciar la transferencia."
                            AppLanguage.MARATHI -> "पाठवणारा ट्रान्सफर सुरू करण्यासाठी हा QR कोड स्कॅन करू शकतो."
                            AppLanguage.BENGALI -> "প্রেরক স্থানান্তর শুরু করতে এই কিউআর কোডটি স্ক্যান করতে পারেন।"
                            AppLanguage.TAMIL -> "அனுப்புநர் பரிமாற்றத்தைத் தொடங்க இந்த QR குறியீட்டை ஸ்கேன் செய்யலாம்."
                            AppLanguage.TELUGU -> "బదిలీని ప్రారంభించడానికి పంపినవారు ఈ QR కోడ్‌ను స్కాన్ చేయవచ్చు."
                            AppLanguage.GUJARATI -> "મોકલનાર ટ્રાન્સફર શરૂ કરવા માટે આ QR કોડ સ્કેન કરી શકે છે."
                            AppLanguage.PUNJABI -> "ਭੇਜਣ ਵਾਲਾ ਟ੍ਰਾਂਸਫਰ ਸ਼ੁਰੂ ਕਰਨ ਲਈ ਇਸ QR ਕੋਡ ਨੂੰ ਸਕੈਨ ਕਰ ਸਕਦਾ ਹੈ।"
                            AppLanguage.FRENCH -> "L'expéditeur peut scanner ce code QR pour lancer le transfert."
                            AppLanguage.ARABIC -> "يمكن للمرسل مسح رمز الاستجابة السريعة (QR) لبدء النقل."
                            else -> "The sender can scan this QR code to initiate transfer."
                        },
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
