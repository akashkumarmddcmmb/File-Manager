package com.example.model

fun AppLanguage.getCategoryTitle(category: FileCategoryType): String {
    return when (this) {
        AppLanguage.HINDI -> category.titleHi
        AppLanguage.ENGLISH -> category.titleEn
        AppLanguage.SPANISH -> when (category) {
            FileCategoryType.DOWNLOADS -> "Descargas"
            FileCategoryType.IMAGES -> "Imágenes"
            FileCategoryType.VIDEOS -> "Videos"
            FileCategoryType.AUDIO -> "Audio"
            FileCategoryType.DOCUMENTS -> "Documentos"
            FileCategoryType.APPS -> "Aplicaciones / APK"
            FileCategoryType.ARCHIVES -> "Archivos comprimidos"
            FileCategoryType.LARGE_FILES -> "Archivos grandes"
        }
        AppLanguage.MARATHI -> when (category) {
            FileCategoryType.DOWNLOADS -> "डाउनलोड्स"
            FileCategoryType.IMAGES -> "इमेजेस / फोटो"
            FileCategoryType.VIDEOS -> "व्हिडिओ"
            FileCategoryType.AUDIO -> "ऑडिओ"
            FileCategoryType.DOCUMENTS -> "दस्तऐवज"
            FileCategoryType.APPS -> "ॲप्स / APK"
            FileCategoryType.ARCHIVES -> "संग्रह (Zip)"
            FileCategoryType.LARGE_FILES -> "मोठ्या फायली"
        }
        AppLanguage.BENGALI -> when (category) {
            FileCategoryType.DOWNLOADS -> "ডাউনলোড"
            FileCategoryType.IMAGES -> "ছবি"
            FileCategoryType.VIDEOS -> "ভিডিও"
            FileCategoryType.AUDIO -> "অডিও"
            FileCategoryType.DOCUMENTS -> "নথি"
            FileCategoryType.APPS -> "অ্যাপস / APK"
            FileCategoryType.ARCHIVES -> "আর্কাইভ"
            FileCategoryType.LARGE_FILES -> "বড় ফাইল"
        }
        AppLanguage.TAMIL -> when (category) {
            FileCategoryType.DOWNLOADS -> "பதிவிறக்கங்கள்"
            FileCategoryType.IMAGES -> "படங்கள்"
            FileCategoryType.VIDEOS -> "வீடியோக்கள்"
            FileCategoryType.AUDIO -> "ஆடியோ"
            FileCategoryType.DOCUMENTS -> "ஆவணங்கள்"
            FileCategoryType.APPS -> "செயலிகள்"
            FileCategoryType.ARCHIVES -> "காப்பகங்கள்"
            FileCategoryType.LARGE_FILES -> "பெரிய கோப்புகள்"
        }
        AppLanguage.TELUGU -> when (category) {
            FileCategoryType.DOWNLOADS -> "డౌన్‌లోడ్‌లు"
            FileCategoryType.IMAGES -> "చిత్రాలు"
            FileCategoryType.VIDEOS -> "వీడియోలు"
            FileCategoryType.AUDIO -> "ఆడియో"
            FileCategoryType.DOCUMENTS -> "పత్రాలు"
            FileCategoryType.APPS -> "యాప్‌లు"
            FileCategoryType.ARCHIVES -> "ఆర్కైవ్‌లు"
            FileCategoryType.LARGE_FILES -> "పెద్ద ఫైళ్లు"
        }
        AppLanguage.GUJARATI -> when (category) {
            FileCategoryType.DOWNLOADS -> "ડાઉનલોડ્સ"
            FileCategoryType.IMAGES -> "ઈમેજીસ"
            FileCategoryType.VIDEOS -> "વિડીયો"
            FileCategoryType.AUDIO -> "ઓડિયો"
            FileCategoryType.DOCUMENTS -> "દસ્તાવેજો"
            FileCategoryType.APPS -> "એપ્સ"
            FileCategoryType.ARCHIVES -> "આર્કાઇવ્સ"
            FileCategoryType.LARGE_FILES -> "મોટી ફાઈલો"
        }
        AppLanguage.PUNJABI -> when (category) {
            FileCategoryType.DOWNLOADS -> "ਡਾਊਨਲੋਡ"
            FileCategoryType.IMAGES -> "ਤਸਵੀਰਾਂ"
            FileCategoryType.VIDEOS -> "ਵੀਡੀਓ"
            FileCategoryType.AUDIO -> "ਆਡੀਓ"
            FileCategoryType.DOCUMENTS -> "ਦਸਤਾਵੇਜ਼"
            FileCategoryType.APPS -> "ਐਪਸ"
            FileCategoryType.ARCHIVES -> "ਆਰਕਾਈਵ"
            FileCategoryType.LARGE_FILES -> "ਵੱਡੀਆਂ ਫਾਈਲਾਂ"
        }
        AppLanguage.FRENCH -> when (category) {
            FileCategoryType.DOWNLOADS -> "Téléchargements"
            FileCategoryType.IMAGES -> "Images"
            FileCategoryType.VIDEOS -> "Vidéos"
            FileCategoryType.AUDIO -> "Audio"
            FileCategoryType.DOCUMENTS -> "Documents"
            FileCategoryType.APPS -> "Applications"
            FileCategoryType.ARCHIVES -> "Archives"
            FileCategoryType.LARGE_FILES -> "Fichiers volumineux"
        }
        AppLanguage.ARABIC -> when (category) {
            FileCategoryType.DOWNLOADS -> "التنزيلات"
            FileCategoryType.IMAGES -> "الصور"
            FileCategoryType.VIDEOS -> "الفيديوهات"
            FileCategoryType.AUDIO -> "الصوتيات"
            FileCategoryType.DOCUMENTS -> "المستندات"
            FileCategoryType.APPS -> "التطبيقات"
            FileCategoryType.ARCHIVES -> "الأرشيف"
            FileCategoryType.LARGE_FILES -> "الملفات الكبيرة"
        }
    }
}

fun AppLanguage.getTabTitle(tab: MainTab): String {
    return when (this) {
        AppLanguage.HINDI -> when (tab) {
            MainTab.CLEAN -> "साफ़ करें"
            MainTab.BROWSE -> "ब्राउज़"
            MainTab.SHARE -> "शेयर"
        }
        AppLanguage.ENGLISH -> when (tab) {
            MainTab.CLEAN -> "Clean"
            MainTab.BROWSE -> "Browse"
            MainTab.SHARE -> "Share"
        }
        AppLanguage.SPANISH -> when (tab) {
            MainTab.CLEAN -> "Limpiar"
            MainTab.BROWSE -> "Explorar"
            MainTab.SHARE -> "Compartir"
        }
        AppLanguage.MARATHI -> when (tab) {
            MainTab.CLEAN -> "स्वच्छ करा"
            MainTab.BROWSE -> "ब्राउझ"
            MainTab.SHARE -> "शेअर"
        }
        AppLanguage.BENGALI -> when (tab) {
            MainTab.CLEAN -> "পরিষ্কার"
            MainTab.BROWSE -> "ব্রাউজ"
            MainTab.SHARE -> "শেয়ার"
        }
        AppLanguage.TAMIL -> when (tab) {
            MainTab.CLEAN -> "சுத்தம்"
            MainTab.BROWSE -> "உலாவு"
            MainTab.SHARE -> "பகிர்"
        }
        AppLanguage.TELUGU -> when (tab) {
            MainTab.CLEAN -> "శుభ్రం"
            MainTab.BROWSE -> "బ్రౌజ్"
            MainTab.SHARE -> "షేర్"
        }
        AppLanguage.GUJARATI -> when (tab) {
            MainTab.CLEAN -> "સાફ કરો"
            MainTab.BROWSE -> "બ્રાઉઝ"
            MainTab.SHARE -> "શેર"
        }
        AppLanguage.PUNJABI -> when (tab) {
            MainTab.CLEAN -> "ਸਾਫ਼ ਕਰੋ"
            MainTab.BROWSE -> "ਬ੍ਰਾਊਜ਼"
            MainTab.SHARE -> "ਸ਼ੇਅਰ"
        }
        AppLanguage.FRENCH -> when (tab) {
            MainTab.CLEAN -> "Nettoyer"
            MainTab.BROWSE -> "Parcourir"
            MainTab.SHARE -> "Partager"
        }
        AppLanguage.ARABIC -> when (tab) {
            MainTab.CLEAN -> "تنظيف"
            MainTab.BROWSE -> "تصفح"
            MainTab.SHARE -> "مشاركة"
        }
    }
}

fun AppLanguage.getSettingsText(): String {
    return when (this) {
        AppLanguage.HINDI -> "सेटिंग्स"
        AppLanguage.ENGLISH -> "Settings"
        AppLanguage.SPANISH -> "Ajustes"
        AppLanguage.MARATHI -> "सेटिंग्ज"
        AppLanguage.BENGALI -> "সেটিংস"
        AppLanguage.TAMIL -> "அமைப்புகள்"
        AppLanguage.TELUGU -> "సెట్టింగ్‌లు"
        AppLanguage.GUJARATI -> "સેટિંગ્સ"
        AppLanguage.PUNJABI -> "ਸੈਟਿੰਗਾਂ"
        AppLanguage.FRENCH -> "Paramètres"
        AppLanguage.ARABIC -> "الإعدادات"
    }
}

fun AppLanguage.getSafeFolderText(): String {
    return when (this) {
        AppLanguage.HINDI -> "सेफ़ फ़ोल्डर"
        AppLanguage.ENGLISH -> "Safe Folder"
        AppLanguage.SPANISH -> "Carpeta segura"
        AppLanguage.MARATHI -> "सुरक्षित फोल्डर"
        AppLanguage.BENGALI -> "সুরক্ষিত ফোল্ডার"
        AppLanguage.TAMIL -> "பாதுகாப்பான கோப்புறை"
        AppLanguage.TELUGU -> "సురక్షిత ఫోల్డర్"
        AppLanguage.GUJARATI -> "સુરક્ષિત ફોલ્ડર"
        AppLanguage.PUNJABI -> "ਸੁਰੱਖਿਅਤ ਫੋਲਡਰ"
        AppLanguage.FRENCH -> "Dossier sécurisé"
        AppLanguage.ARABIC -> "المجلد الآمن"
    }
}

fun AppLanguage.getTrashText(): String {
    return when (this) {
        AppLanguage.HINDI -> "ट्रैश (कचरा)"
        AppLanguage.ENGLISH -> "Trash"
        AppLanguage.SPANISH -> "Papelera"
        AppLanguage.MARATHI -> "कचरा टोपली"
        AppLanguage.BENGALI -> "ট্র্যাশ"
        AppLanguage.TAMIL -> "குப்பை"
        AppLanguage.TELUGU -> "ట్రాష్"
        AppLanguage.GUJARATI -> "ટ્રેશ"
        AppLanguage.PUNJABI -> "ਟ੍ਰੈਸ਼"
        AppLanguage.FRENCH -> "Corbeille"
        AppLanguage.ARABIC -> "سلة المهملات"
    }
}

fun AppLanguage.translate(key: String): String {
    if (this == AppLanguage.ENGLISH) return key
    
    val mapped = translationsMap[this]?.get(key)
    if (mapped != null) return mapped
    
    return when (this) {
        AppLanguage.HINDI -> when (key) {
            "Settings" -> "सेटिंग्स"
            "Done" -> "पूर्ण"
            "Close" -> "बंद करें"
            "Cancel" -> "रद्द करें"
            "Back" -> "पीछे"
            "Junk & Temporary Files Available to Clean" -> "अस्थायी व जंक फाइलें उपलब्ध हैं"
            "Cleaning Junk Files..." -> "जंक फाइलें साफ हो रही हैं..."
            "completed" -> "पूरा हुआ"
            "Clean All Junk" -> "सभी जंक साफ करें"
            "Cleanup Categories" -> "सफाई की श्रेणियां"
            "Your device is clean!" -> "आपका डिवाइस साफ है!"
            "Smart Storage & RAM Optimization" -> "स्मार्ट रैम व स्टोरेज बूस्ट"
            "Nearby Share" -> "आस-पास शेयर करें"
            "Send" -> "भेजें"
            "Receive" -> "प्राप्त करें"
            "High-Speed Wi-Fi Transfer" -> "हाई-स्पीड वाई-फाई ट्रांसफर"
            "Speeds up to 480 Mbps" -> "480 Mbps तक की स्पीड"
            "Searching for Nearby Devices..." -> "पास के डिवाइस की खोज..."
            "Ready to Receive Files" -> "प्राप्त करने के लिए तैयार"
            "General" -> "सामान्य"
            "Select App Language" -> "ऐप भाषा चुनें"
            "Low Battery Consumption Mode" -> "लो बैटरी खपत मोड"
            "Ultra Battery Saver" -> "अल्ट्रा बैटरी सेवर"
            "App & Legal Information" -> "ऐप और कानूनी जानकारी"
            "Version" -> "संस्करण"
            "Developer" -> "डेवलपर"
            "Check for Updates" -> "नए अपडेट की जांच करें"
            "Install latest APK update" -> "नवीनतम APK अपडेट इंस्टॉल करें"
            "Check" -> "जांचें"
            "Send Developer Feedback" -> "डेवलपर को प्रतिक्रिया भेजें"
            "Privacy Policy" -> "गोपनीयता नीति"
            "Terms of Service" -> "सेवा की शर्तें"
            "New Update Available! 🎉" -> "नया अपडेट उपलब्ध है! 🎉"
            "You are on the Latest Version!" -> "आप नवीनतम संस्करण पर हैं!"
            "Update Check Failed" -> "अपडेट जांच विफल रही"
            "No Updates Available" -> "कोई अपडेट उपलब्ध नहीं है"
            "Update Now" -> "अभी अपडेट करें"
            "Later" -> "बाद में"
            else -> key
        }
        AppLanguage.SPANISH -> when (key) {
            "Settings" -> "Ajustes"
            "Done" -> "Hecho"
            "Close" -> "Cerrar"
            "Cancel" -> "Cancelar"
            "Back" -> "Atrás"
            "Junk & Temporary Files Available to Clean" -> "Archivos temporales y de basura para limpiar"
            "Cleaning Junk Files..." -> "Limpiando archivos basura..."
            "completed" -> "completado"
            "Clean All Junk" -> "Limpiar toda la basura"
            "Cleanup Categories" -> "Categorías de limpieza"
            "Your device is clean!" -> "¡Tu dispositivo está limpio!"
            "Smart Storage & RAM Optimization" -> "Optimización inteligente de almacenamiento y RAM"
            "Nearby Share" -> "Compartir con cercanos"
            "Send" -> "Enviar"
            "Receive" -> "Recibir"
            "High-Speed Wi-Fi Transfer" -> "Transferencia Wi-Fi de alta velocidad"
            "Speeds up to 480 Mbps" -> "Velocidades de hasta 480 Mbps"
            "Searching for Nearby Devices..." -> "Buscando dispositivos cercanos..."
            "Ready to Receive Files" -> "Listo para recibir archivos"
            "General" -> "General"
            "Select App Language" -> "Seleccionar idioma de la aplicación"
            "Low Battery Consumption Mode" -> "Modo de bajo consumo de batería"
            "Ultra Battery Saver" -> "Ultra ahorro de batería"
            "App & Legal Information" -> "Información legal y de la aplicación"
            "Version" -> "Versión"
            "Developer" -> "Desarrollador"
            "Check for Updates" -> "Buscar actualizaciones"
            "Install latest APK update" -> "Instalar la última actualización de APK"
            "Check" -> "Comprobar"
            "Send Developer Feedback" -> "Enviar comentarios al desarrollador"
            "Privacy Policy" -> "Política de privacidad"
            "Terms of Service" -> "Términos de servicio"
            "New Update Available! 🎉" -> "¡Nueva actualización disponible! 🎉"
            "You are on the Latest Version!" -> "¡Estás en la última versión!"
            "Update Check Failed" -> "Error al comprobar actualizaciones"
            "No Updates Available" -> "No hay actualizaciones disponibles"
            "Update Now" -> "Actualizar ahora"
            "Later" -> "Más tarde"
            else -> key
        }
        AppLanguage.MARATHI -> when (key) {
            "Settings" -> "सेटिंग्ज"
            "Done" -> "पूर्ण"
            "Close" -> "बंद करा"
            "Cancel" -> "रद्द करा"
            "Back" -> "मागे"
            "Junk & Temporary Files Available to Clean" -> "साफ करण्यासाठी जंक आणि तात्पुरत्या फायली"
            "Cleaning Junk Files..." -> "जंक फायली साफ करत आहे..."
            "completed" -> "पूर्ण"
            "Clean All Junk" -> "सर्व जंक साफ करा"
            "Cleanup Categories" -> "साफसफाईच्या श्रेणी"
            "Your device is clean!" -> "तुमचे डिव्हाइस स्वच्छ आहे!"
            "Smart Storage & RAM Optimization" -> "स्मार्ट स्टोरेज आणि रॅम ऑप्टिमायझेशन"
            "Nearby Share" -> "जवळपास शेअर करा"
            "Send" -> "पाठवा"
            "Receive" -> "स्वीकारा"
            "High-Speed Wi-Fi Transfer" -> "हाय-स्पीड वाय-फाय ट्रान्सफर"
            "Speeds up to 480 Mbps" -> "480 Mbps पर्यंत वेग"
            "Searching for Nearby Devices..." -> "जवळपासचे डिव्हाइस शोधत आहे..."
            "Ready to Receive Files" -> "फायली स्वीकारण्यास तयार"
            "General" -> "सामान्य"
            "Select App Language" -> "अ‍ॅपची भाषा निवडा"
            "Low Battery Consumption Mode" -> "कमी बॅटरी वापर मोड"
            "Ultra Battery Saver" -> "अल्ट्रा बॅटरी सेव्हर"
            "App & Legal Information" -> "अ‍ॅप आणि कायदेशीर माहिती"
            "Version" -> "आवृत्ती"
            "Developer" -> "डेव्हलपर"
            "Check for Updates" -> "अपडेट तपासा"
            "Install latest APK update" -> "नवीनतम APK अपडेट स्थापित करा"
            "Check" -> "तपासा"
            "Send Developer Feedback" -> "डेव्हलपरला अभिप्राय पाठवा"
            "Privacy Policy" -> "गोपनीयता धोरण"
            "Terms of Service" -> "सेवा अटी"
            "New Update Available! 🎉" -> "नवीन अपडेट उपलब्ध आहे! 🎉"
            "You are on the Latest Version!" -> "तुम्ही नवीनतम आवृत्तीवर आहात!"
            "Update Check Failed" -> "अपडेट तपासणी अयशस्वी"
            "No Updates Available" -> "कोणतेही अपडेट उपलब्ध नाहीत"
            "Update Now" -> "आत्ताच अपडेट करा"
            "Later" -> "नंतर"
            else -> key
        }
        AppLanguage.BENGALI -> when (key) {
            "Settings" -> "সেটিংস"
            "Done" -> "সম্পন্ন"
            "Close" -> "বন্ধ করুন"
            "Cancel" -> "বাতিল"
            "Back" -> "ফিরে"
            "Junk & Temporary Files Available to Clean" -> "পরিষ্কারের জন্য জাঙ্ক ও টেম্পোরারি ফাইল"
            "Cleaning Junk Files..." -> "জাঙ্ক ফাইল পরিষ্কার করা হচ্ছে..."
            "completed" -> "সম্পন্ন"
            "Clean All Junk" -> "সব জাঙ্ক পরিষ্কার করুন"
            "Cleanup Categories" -> "পরিষ্কারের বিভাগ"
            "Your device is clean!" -> "আপনার ডিভাইস পরিষ্কার!"
            "Smart Storage & RAM Optimization" -> "স্মার্ট স্টোরেজ এবং র‌্যাম অপ্টিমাইজেশান"
            "Nearby Share" -> "কাছাকাছি শেয়ার করুন"
            "Send" -> "পাঠান"
            "Receive" -> "গ্রহণ করুন"
            "High-Speed Wi-Fi Transfer" -> "উচ্চ গতির ওয়াই-ফাই স্থানান্তর"
            "Speeds up to 480 Mbps" -> "480 Mbps পর্যন্ত গতি"
            "Searching for Nearby Devices..." -> "কাছাকাছি ডিভাইস অনুসন্ধান করা হচ্ছে..."
            "Ready to Receive Files" -> "ফাইল গ্রহণের জন্য প্রস্তুত"
            "General" -> "সাধারণ"
            "Select App Language" -> "অ্যাপের ভাষা নির্বাচন করুন"
            "Low Battery Consumption Mode" -> "কম ব্যাটারি খরচ মোড"
            "Ultra Battery Saver" -> "আল্ট্রা ব্যাটারি সেভার"
            "App & Legal Information" -> "অ্যাপ এবং আইনি তথ্য"
            "Version" -> "সংস্করণ"
            "Developer" -> "ডেভেলপার"
            "Check for Updates" -> "আপডেটের জন্য অনুসন্ধান"
            "Install latest APK update" -> "সর্বশেষ APK আপডেট ইনস্টল করুন"
            "Check" -> "যাচাই"
            "Send Developer Feedback" -> "ডেভেলপারকে মতামত পাঠান"
            "Privacy Policy" -> "গোপনীয়তা নীতি"
            "Terms of Service" -> "পরিষেবার শর্তাবলী"
            "New Update Available! 🎉" -> "নতুন আপডেট উপলব্ধ! 🎉"
            "You are on the Latest Version!" -> "আপনি সর্বশেষ সংস্করণে আছেন!"
            "Update Check Failed" -> "আপডেট পরীক্ষা ব্যর্থ হয়েছে"
            "No Updates Available" -> "কোনো আপডেট উপলব্ধ নেই"
            "Update Now" -> "এখনই আপডেট করুন"
            "Later" -> "পরে"
            else -> key
        }
        AppLanguage.TAMIL -> when (key) {
            "Settings" -> "அமைப்புகள்"
            "Done" -> "முடிந்தது"
            "Close" -> "மூடு"
            "Cancel" -> "ரத்துசெய்"
            "Back" -> "பின்னால்"
            "Junk & Temporary Files Available to Clean" -> "சுத்தம் செய்ய குப்பை மற்றும் தற்காலிக கோப்புகள்"
            "Cleaning Junk Files..." -> "குப்பை கோப்புகள் சுத்தம் செய்யப்படுகின்றன..."
            "completed" -> "முடிந்தது"
            "Clean All Junk" -> "அனைத்து குப்பைகளையும் சுத்தம் செய்"
            "Cleanup Categories" -> "சுத்தப்படுத்தும் பிரிவுகள்"
            "Your device is clean!" -> "உங்கள் சாதனம் சுத்தமாக உள்ளது!"
            "Smart Storage & RAM Optimization" -> "ஸ்மார்ட் சேமிப்பகம் & ரேம் மேம்படுத்தல்"
            "Nearby Share" -> "அருகிலுள்ள பகிர்வு"
            "Send" -> "அனுப்பு"
            "Receive" -> "பெறு"
            "High-Speed Wi-Fi Transfer" -> "அதிவேக வைஃபை பரிமாற்றம்"
            "Speeds up to 480 Mbps" -> "480 Mbps வரை வேகம்"
            "Searching for Nearby Devices..." -> "அருகிலுள்ள சாதனங்களைத் தேடுகிறது..."
            "Ready to Receive Files" -> "கோப்புகளைப் பெற సిద్ధంగా ఉంది"
            "General" -> "பொதுவான"
            "Select App Language" -> "பயன்பாட்டு மொழியைத் தேர்ந்தெடுக்கவும்"
            "Low Battery Consumption Mode" -> "குறைந்த பேட்டரி நுகர்வு முறை"
            "Ultra Battery Saver" -> "அல்ட்ரா பேட்டரி சேமிப்பான்"
            "App & Legal Information" -> "பயன்பாடு & சட்ட தகவல்"
            "Version" -> "பதிப்பு"
            "Developer" -> "டெவலப்பர்"
            "Check for Updates" -> "புதுப்பிப்புகளைச் சரிபார்க்கவும்"
            "Install latest APK update" -> "சமீபத்திய APK புதுப்பிப்பை நிறுவவும்"
            "Check" -> "சரிபார்"
            "Send Developer Feedback" -> "டெவலப்பர் கருத்துக்களை அனுப்பவும்"
            "Privacy Policy" -> "தனியுरीமைக் கொள்கை"
            "Terms of Service" -> "சேவை நிபந்தனைகள்"
            "New Update Available! 🎉" -> "புதிய புதுப்பிப்பு உள்ளது! 🎉"
            "You are on the Latest Version!" -> "நீங்கள் சமீபத்திய பதிப்பில் இருக்கிறீர்கள்!"
            "Update Check Failed" -> "புதுப்பிப்பு சரிபார்ப்பு தோல்வியடைந்தது"
            "No Updates Available" -> "புதுப்பிப்புகள் எதுவும் இல்லை"
            "Update Now" -> "இப்போது புதுப்பிக்கவும்"
            "Later" -> "பின்னர்"
            else -> key
        }
        AppLanguage.TELUGU -> when (key) {
            "Settings" -> "సెట్టింగ్‌లు"
            "Done" -> "పూర్తయింది"
            "Close" -> "మూసివేయి"
            "Cancel" -> "రద్దు చేయి"
            "Back" -> "వెనుకకు"
            "Junk & Temporary Files Available to Clean" -> "శుభ్రం చేయడానికి జంక్ & తాత్కాలిక ఫైల్‌లు"
            "Cleaning Junk Files..." -> "జంక్ ఫైల్‌లు శుభ్రం చేయబడుతున్నాయి..."
            "completed" -> "పూర్తయింది"
            "Clean All Junk" -> "అన్ని జంక్‌లను శుభ్రం చేయి"
            "Cleanup Categories" -> "శుభ్రపరిచే వర్గాలు"
            "Your device is clean!" -> "మీ పరికరం శుభ్రంగా ఉంది!"
            "Smart Storage & RAM Optimization" -> "స్మార్ట్ స్టోరేజ్ & ర్యామ్ ఆప్టిమైజేషన్"
            "Nearby Share" -> "సమీప భాగస్వామ్యం"
            "Send" -> "పంపు"
            "Receive" -> "స్వీకరించు"
            "High-Speed Wi-Fi Transfer" -> "హై-ఫోన్ వై-ఫై బదిలీ"
            "Speeds up to 480 Mbps" -> "480 Mbps వరకు వేగం"
            "Searching for Nearby Devices..." -> "సమీప పరికరాల కోసం శోధిస్తోంది..."
            "Ready to Receive Files" -> "ఫైళ్లను స్వీకరించడానికి సిద్ధంగా ఉంది"
            "General" -> "సాధారణ"
            "Select App Language" -> "యాప్ భాషను ఎంచుకోండి"
            "Low Battery Consumption Mode" -> "తక్కువ బ్యాటరీ వినియోగ మోడ్"
            "Ultra Battery Saver" -> "అల్ట్రా బ్యాటరీ సేవరు"
            "App & Legal Information" -> "యాప్ & చట్టపరమైన సమాచారం"
            "Version" -> "వెర్షన్"
            "Developer" -> "డెవలపర్"
            "Check for Updates" -> "అప్‌డేట్‌ల కోసం తనిఖీ చేయి"
            "Install latest APK update" -> "తాజా APK అప్‌డేట్‌ను ఇన్‌స్టాల్ చేయి"
            "Check" -> "తనిఖీ"
            "Send Developer Feedback" -> "డెవలపర్‌కు అభిప్రాయాన్ని పంపండి"
            "Privacy Policy" -> "గోప్యతా విధానం"
            "Terms of Service" -> "సేవా నిబంధనలు"
            "New Update Available! 🎉" -> "కొత్త అప్‌డేట్ అందుబాటులో ఉంది! 🎉"
            "You are on the Latest Version!" -> "మీరు తాజా వెర్షన్‌లో ఉన్నారు!"
            "Update Check Failed" -> "అప్‌డేట్ తనిఖీ విఫలమైంది"
            "No Updates Available" -> "అప్‌డేట్‌లు అందుబాటులో లేవు"
            "Update Now" -> "ఇప్పుడే అప్‌డేట్ చేయి"
            "Later" -> "తర్వాత"
            else -> key
        }
        AppLanguage.GUJARATI -> when (key) {
            "Settings" -> "સેટિંગ્સ"
            "Done" -> "પૂર્ણ"
            "Close" -> "બંધ કરો"
            "Cancel" -> "રદ કરો"
            "Back" -> "પાછા"
            "Junk & Temporary Files Available to Clean" -> "સાફ કરવા માટે જંક અને અસ્થાયી ફાઇલો"
            "Cleaning Junk Files..." -> "જંક ફાઇલો સાફ થઈ રહી છે..."
            "completed" -> "પૂર્ણ"
            "Clean All Junk" -> "બધા જંક સાફ કરો"
            "Cleanup Categories" -> "સફાઈ શ્રેણીઓ"
            "Your device is clean!" -> "તમારું ઉપકરણ સાફ છે!"
            "Smart Storage & RAM Optimization" -> "સ્માર્ટ સ્ટોરેજ અને રેમ ઓપ્ટિમાઇઝેશન"
            "Nearby Share" -> "નજીકમાં શેર કરો"
            "Send" -> "મોકલો"
            "Receive" -> "મેળવો"
            "High-Speed Wi-Fi Transfer" -> "હાઇ-स्पीड વાઇ-ફાઇ ટ્રાન્સફર"
            "Speeds up to 480 Mbps" -> "480 Mbps સુધીની ઝડપ"
            "Searching for Nearby Devices..." -> "નજીકના ઉપકરણો શોધી રહ્યાં છે..."
            "Ready to Receive Files" -> "ફાઇલો મેળવવા માટે તૈયાર છે"
            "General" -> "સામાન્ય"
            "Select App Language" -> "એપ્લિકેશન ભાષા પસંદ કરો"
            "Low Battery Consumption Mode" -> "ઓછી બેટરી વપરાશ મોડ"
            "Ultra Battery Saver" -> "અલ્ટ્રા બેટરી સેવર"
            "App & Legal Information" -> "એપ્લિકેશન અને કાનૂની માહિતી"
            "Version" -> "આવૃત્તિ"
            "Developer" -> "ડેવલપર"
            "Check for Updates" -> "અપડેટ્સ માટે તપાસો"
            "Install latest APK update" -> "નવીનતમ APK અપડેટ ઇન્સ્ટોલ કરો"
            "Check" -> "તપાસો"
            "Send Developer Feedback" -> "ડેવલપરને પ્રતિસાદ મોકલો"
            "Privacy Policy" -> "ગોપનીયતા નીતિ"
            "Terms of Service" -> "સેવાની શરતો"
            "New Update Available! 🎉" -> "નવું અપડેટ ઉપલબ્ધ છે! 🎉"
            "You are on the Latest Version!" -> "તમે નવીનતમ આવૃત્તિ પર છો!"
            "Update Check Failed" -> "અપડેટ તપાસ નિષ્ફળ ગઈ"
            "No Updates Available" -> "કોઈ અપડેટ્સ ઉપલબ્ધ નથી"
            "Update Now" -> "હમણાં અપડેટ કરો"
            "Later" -> "પછી"
            else -> key
        }
        AppLanguage.PUNJABI -> when (key) {
            "Settings" -> "ਸੈਟਿੰਗਾਂ"
            "Done" -> "ਪੂਰਾ"
            "Close" -> "ਬੰਦ ਕਰੋ"
            "Cancel" -> "ਰੱਦ ਕਰੋ"
            "Back" -> "ਪਿੱਛੇ"
            "Junk & Temporary Files Available to Clean" -> "ਸਾਫ਼ ਕਰਨ ਲਈ ਜੰਕ ਅਤੇ ਅਸਥਾਈ ਫਾਈਲਾਂ"
            "Cleaning Junk Files..." -> "ਜੰਕ ਫਾਈਲਾਂ ਸਾਫ਼ ਕੀਤੀਆਂ ਜਾ ਰਹੀਆਂ ਹਨ..."
            "completed" -> "ਪੂਰਾ"
            "Clean All Junk" -> "ਸਾਰਾ ਜੰਕ ਸਾਫ਼ ਕਰੋ"
            "Cleanup Categories" -> "ਸਫ਼ਾਈ ਸ਼੍ਰੇਣੀਆਂ"
            "Your device is clean!" -> "ਤੁਹਾਡਾ ਡਿਵਾਈਸ ਸਾਫ਼ ਹੈ!"
            "Smart Storage & RAM Optimization" -> "ਸਮਾਰਟ ਸਟੋਰేਜ ਅਤੇ ਰੈਮ ਆਪਟੀਮਾਈਜ਼ੇਸ਼ਨ"
            "Nearby Share" -> "ਨੇੜੇ ਸਾਂਝਾ ਕਰੋ"
            "Send" -> "ਭੇਜੋ"
            "Receive" -> "ਪ੍ਰਾਪਤ ਕਰੋ"
            "High-Speed Wi-Fi Transfer" -> "ਹਾਈ-ਸਪੀਡ ਵਾਈ-ਫਾਈ ਟ੍ਰਾਂਸਫਰ"
            "Speeds up to 480 Mbps" -> "480 Mbps ਤੱਕ ਦੀ ਸਪੀਡ"
            "Searching for Nearby Devices..." -> "ਨੇੜਲੇ ਡਿਵਾਈਸਾਂ ਦੀ ਖੋਜ ਕੀਤੀ ਜਾ ਰਹੀ ਹੈ..."
            "Ready to Receive Files" -> "ਫਾਈਲਾਂ ਪ੍ਰਾਪਤ ਕਰਨ ਲਈ ਤਿਆਰ"
            "General" -> "ਸਧਾਰਨ"
            "Select App Language" -> "ਐਪ ਭਾਸ਼ਾ ਚੁਣੋ"
            "Low Battery Consumption Mode" -> "ਘੱਟ ਬੈਟਰੀ ਖਪਤ ਮੋਡ"
            "Ultra Battery Saver" -> "ਅਲਟਰਾ ਬੈਟਰੀ ਸੇਵਰ"
            "App & Legal Information" -> "ਐਪ ਅਤੇ ਕਾਨੂੰਨੀ ਜਾਣਕਾਰੀ"
            "Version" -> "ਸੰਸਕਰਣ"
            "Developer" -> "ਡਿਵੈਲਪਰ"
            "Check for Updates" -> "ਅੱਪਡੇਟਾਂ ਦੀ ਜਾਂਚ ਕਰੋ"
            "Install latest APK update" -> "ਨਵੀਨਤਮ APK ਅੱਪਡੇਟ ਇੰਸਟਾਲ ਕਰੋ"
            "Check" -> "ਜਾਂਚ"
            "Send Developer Feedback" -> "ਡਿਵੈਲਪਰ ਫੀਡਬੈਕ ਭੇਜੋ"
            "Privacy Policy" -> "ਪ੍ਰਾਈਵੇਸੀ ਪਾਲਿਸੀ"
            "Terms of Service" -> "ਸੇਵਾ ਦੀਆਂ ਸ਼ਰਤਾਂ"
            "New Update Available! 🎉" -> "ਨਵਾਂ ਅੱਪਡੇট ਉਪਲਬਧ ਹੈ! 🎉"
            "You are on the Latest Version!" -> "ਤੁਸੀਂ ਨਵੀਨਤਮ ਸੰਸਕਰਣ 'ਤੇ ਹੋ!"
            "Update Check Failed" -> "ਅੱਪਡੇਟ ਜਾਂਚ ਅਸਫ਼ਲ ਰਹੀ"
            "No Updates Available" -> "ਕੋਈ ਅੱਪਡੇਟ ਉਪਲਬਧ ਨਹੀਂ ਹੈ"
            "Update Now" -> "ਹੁਣੇ ਅੱਪਡੇٹ ਕਰੋ"
            "Later" -> "ਬਾਅਦ ਵਿੱਚ"
            else -> key
        }
        AppLanguage.FRENCH -> when (key) {
            "Settings" -> "Paramètres"
            "Done" -> "Terminé"
            "Close" -> "Fermer"
            "Cancel" -> "Annuler"
            "Back" -> "Retour"
            "Junk & Temporary Files Available to Clean" -> "Fichiers temporaires et indésirables prêts à être nettoyés"
            "Cleaning Junk Files..." -> "Nettoyage des fichiers indésirables..."
            "completed" -> "terminé"
            "Clean All Junk" -> "Nettoyer tous les indésirables"
            "Cleanup Categories" -> "Catégories de nettoyage"
            "Your device is clean!" -> "Votre appareil est propre !"
            "Smart Storage & RAM Optimization" -> "Stockage intelligent et optimisation RAM"
            "Nearby Share" -> "Partage à proximité"
            "Send" -> "Envoyer"
            "Receive" -> "Recevoir"
            "High-Speed Wi-Fi Transfer" -> "Transfert Wi-Fi haut débit"
            "Speeds up to 480 Mbps" -> "Vitesses jusqu'à 480 Mbps"
            "Searching for Nearby Devices..." -> "Recherche d'appareils à proximité..."
            "Ready to Receive Files" -> "Prêt à recevoir des fichiers"
            "General" -> "Général"
            "Select App Language" -> "Sélectionner la langue"
            "Low Battery Consumption Mode" -> "Mode basse consommation"
            "Ultra Battery Saver" -> "Ultra économiseur de batterie"
            "App & Legal Information" -> "Informations légales et de l'application"
            "Version" -> "Version"
            "Developer" -> "Développeur"
            "Check for Updates" -> "Vérifier les mises à jour"
            "Install latest APK update" -> "Installer la dernière mise à jour APK"
            "Check" -> "Vérifier"
            "Send Developer Feedback" -> "Envoyer des commentaires au développeur"
            "Privacy Policy" -> "Politique de confidentialité"
            "Terms of Service" -> "Conditions d'utilisation"
            "New Update Available! 🎉" -> "Nouvelle mise à jour disponible ! 🎉"
            "You are on the Latest Version!" -> "Vous utilisez la dernière version !"
            "Update Check Failed" -> "Échec de la vérification de la mise à jour"
            "No Updates Available" -> "Aucune mise à jour disponible"
            "Update Now" -> "Mettre à jour maintenant"
            "Later" -> "Plus tard"
            else -> key
        }
        AppLanguage.ARABIC -> when (key) {
            "Settings" -> "الإعدادات"
            "Done" -> "تم"
            "Close" -> "إغلاق"
            "Cancel" -> "إلغاء"
            "Back" -> "رجوع"
            "Junk & Temporary Files Available to Clean" -> "ملفات مؤقتة وغير مرغوب فيها جاهزة للتنظيف"
            "Cleaning Junk Files..." -> "جاري تنظيف الملفات غير المرغوب فيها..."
            "completed" -> "اكتمل"
            "Clean All Junk" -> "تنظيف كل الملفات غير المقربة"
            "Cleanup Categories" -> "فئات التنظيف"
            "Your device is clean!" -> "جهازك نظيف!"
            "Smart Storage & RAM Optimization" -> "تحسين الذاكرة والتخزين الذكي"
            "Nearby Share" -> "المشاركة عن قرب"
            "Send" -> "إرسال"
            "Receive" -> "استلام"
            "High-Speed Wi-Fi Transfer" -> "نقل واي فاي عالي السرعة"
            "Speeds up to 480 Mbps" -> "سرعات تصل إلى 480 ميجابت في الثانية"
            "Searching for Nearby Devices..." -> "جاري البحث عن أجهزة مجاورة..."
            "Ready to Receive Files" -> "جاهز لاستلام الملفات"
            "General" -> "عام"
            "Select App Language" -> "اختر لغة التطبيق"
            "Low Battery Consumption Mode" -> "وضع استهلاك البطارية المنخفض"
            "Ultra Battery Saver" -> "توفير البطارية الفائق"
            "App & Legal Information" -> "معلومات التطبيق والمعلومات القانونية"
            "Version" -> "الإصدار"
            "Developer" -> "المطور"
            "Check for Updates" -> "التحقق من وجود تحديثات"
            "Install latest APK update" -> "تثبيت أحدث تحديث APK"
            "Check" -> "تحقق"
            "Send Developer Feedback" -> "إرسال تعليقات للمطور"
            "Privacy Policy" -> "سياسة الخصوصية"
            "Terms of Service" -> "شروط الخدمة"
            "New Update Available! 🎉" -> "يتوفر تحديث جديد! 🎉"
            "You are on the Latest Version!" -> "أنت على أحدث إصدار!"
            "Update Check Failed" -> "فشل التحقق من التحديث"
            "No Updates Available" -> "لا توجد تحديثات متاحة"
            "Update Now" -> "تحديث الآن"
            "Later" -> "لاحقاً"
            else -> key
        }
        AppLanguage.ENGLISH -> key
    }
}

private val translationsMap = mapOf(
    AppLanguage.HINDI to mapOf(
        "Recent files" to "हाल ही की फाइलें",
        "See all >" to "सभी देखें >",
        "Categories" to "श्रेणियां",
        "Collections" to "संग्रह",
        "Starred files" to "पसंदीदा फाइलें",
        "Safe folder" to "सुरक्षित फ़ोल्डर",
        "Trash" to "ट्रैश (कचरा)",
        "Storage devices" to "स्टोरेज डिवाइसेस",
        "Refresh devices" to "रिफ्रेश करें",
        "0 items" to "0 आइटम",
        "Phone Memory" to "फोन मेमोरी",
        "SD Card" to "एसडी कार्ड",
        "Move to Safe folder" to "सुरक्षित फ़ोल्डर में भेजें",
        "Move to Trash" to "ट्रैश में भेजें",
        "Create New Folder" to "नया फ़ोल्डर बनाएं",
        "Create a new folder in current directory:" to "वर्तमान फ़ोल्डर में नया फ़ोल्डर बनाएं:",
        "Folder Name" to "फ़ोल्डर का नाम",
        "Create" to "बनाएं",
        "Cancel" to "रद्द करें",
        "Rename Folder" to "फ़ोल्डर का नाम बदलें",
        "New Name" to "नया नाम",
        "Folder renamed successfully" to "फ़ोल्डर का नाम बदला गया",
        "Failed to rename folder" to "नाम नहीं बदला जा सका",
        "Rename" to "बदलें",
        "Delete Folder?" to "फ़ोल्डर हटाएं?",
        "Folder deleted" to "फ़ोल्डर हटा दिया गया",
        "Failed to delete folder" to "हटाया नहीं जा सका",
        "Delete" to "हटाएं"
    ),
    AppLanguage.SPANISH to mapOf(
        "Recent files" to "Archivos recientes",
        "See all >" to "Ver todo >",
        "Categories" to "Categorías",
        "Collections" to "Colecciones",
        "Starred files" to "Archivos destacados",
        "Safe folder" to "Carpeta segura",
        "Trash" to "Papelera",
        "Storage devices" to "Dispositivos de almacenamiento",
        "Refresh devices" to "Actualizar dispositivos",
        "0 items" to "0 elementos",
        "Phone Memory" to "Memoria del teléfono",
        "SD Card" to "Tarjeta SD",
        "Move to Safe folder" to "Mover a Carpeta segura",
        "Move to Trash" to "Mover a la Papelera",
        "Create New Folder" to "Crear nueva carpeta",
        "Create a new folder in current directory:" to "Crear una nueva carpeta en el directorio actual:",
        "Folder Name" to "Nombre de la carpeta",
        "Create" to "Crear",
        "Cancel" to "Cancelar",
        "Rename Folder" to "Renombrar carpeta",
        "New Name" to "Nuevo nombre",
        "Folder renamed successfully" to "Carpeta renombrada con éxito",
        "Failed to rename folder" to "Error al renombrar la carpeta",
        "Rename" to "Renombrar",
        "Delete Folder?" to "¿Eliminar carpeta?",
        "Folder deleted" to "Carpeta eliminada",
        "Failed to delete folder" to "Error al eliminar la carpeta",
        "Delete" to "Eliminar"
    ),
    AppLanguage.MARATHI to mapOf(
        "Recent files" to "अलीकडील फायली",
        "See all >" to "सर्व पहा >",
        "Categories" to "वर्ग / श्रेणी",
        "Collections" to "संग्रह",
        "Starred files" to "स्टार केलेल्या फायली",
        "Safe folder" to "सुरक्षित फोल्डर",
        "Trash" to "कचरा टोपली",
        "Storage devices" to "स्टोरेज डिव्हाइसेस",
        "Refresh devices" to "रिफ्रेश करा",
        "0 items" to "0 आयटम",
        "Phone Memory" to "फोन मेमरी",
        "SD Card" to "एसडी कार्ड",
        "Move to Safe folder" to "सुरक्षित फोल्डरमध्ये हलवा",
        "Move to Trash" to "कचरा टोपलीत हलवा",
        "Create New Folder" to "नवीन फोल्डर बनवा",
        "Create a new folder in current directory:" to "सध्याच्या फोल्डरमध्ये नवीन फोल्डर बनवा:",
        "Folder Name" to "फोल्डरचे नाव",
        "Create" to "बनवा",
        "Cancel" to "रद्द करा",
        "Rename Folder" to "फोल्डरचे नाव बदला",
        "New Name" to "नवीन नाव",
        "Folder renamed successfully" to "फोल्डरचे नाव यशस्वीरित्या बदलले",
        "Failed to rename folder" to "नाव बदलण्यात अयशस्वी",
        "Rename" to "नाव बदला",
        "Delete Folder?" to "फोल्डर हटवायचे?",
        "Folder deleted" to "फोल्डर हटवले",
        "Failed to delete folder" to "हटवण्यात अयशस्वी",
        "Delete" to "हटवा"
    ),
    AppLanguage.BENGALI to mapOf(
        "Recent files" to "সাম্প্রতিক ফাইল",
        "See all >" to "সব দেখুন >",
        "Categories" to "বিভাগ",
        "Collections" to "সংগ্রহ",
        "Starred files" to "তারকাচিহ্নিত ফাইল",
        "Safe folder" to "সুরক্ষিত ফোল্ডার",
        "Trash" to "ট্র্যাশ",
        "Storage devices" to "স্টোরেজ ডিভাইস",
        "Refresh devices" to "রিফ্রেশ করুন",
        "0 items" to "0 আইটেม",
        "Phone Memory" to "ফোন মেমরি",
        "SD Card" to "এসডি কার্ড",
        "Move to Safe folder" to "সুরক্ষিত ফোল্ডারে সরান",
        "Move to Trash" to "ট্র্যাশে সরান",
        "Create New Folder" to "নতুন ফোল্ডার তৈরি করুন",
        "Create a new folder in current directory:" to "বর্তমান ডিরেক্টরিতে নতুন ফোল্ডার তৈরি করুন:",
        "Folder Name" to "ফোল্ডারের নাম",
        "Create" to "তৈরি করুন",
        "Cancel" to "বাতিল করুন",
        "Rename Folder" to "ফোল্ডারের নাম পরিবর্তন করুন",
        "New Name" to "নতুন নাম",
        "Folder renamed successfully" to "ফোল্ডারের নাম পরিবর্তন সফল হয়েছে",
        "Failed to rename folder" to "নাম পরিবর্তন করতে ব্যর্থ হয়েছে",
        "Rename" to "পরিবর্তন করুন",
        "Delete Folder?" to "ফোল্ডার মুছে ফেলবেন?",
        "Folder deleted" to "ফোল্ডার মুছে ফেলা হয়েছে",
        "Failed to delete folder" to "মুছে ফেলতে ব্যর্থ হয়েছে",
        "Delete" to "মুছে ফেলুন"
    ),
    AppLanguage.TAMIL to mapOf(
        "Recent files" to "சமீபத்திய கோப்புகள்",
        "See all >" to "அனைத்தையும் காண்க >",
        "Categories" to "பிரிவுகள்",
        "Collections" to "சேகரிப்புகள்",
        "Starred files" to "நட்சத்திரக் கோப்புகள்",
        "Safe folder" to "பாதுகாப்பான கோப்புறை",
        "Trash" to "குப்பை",
        "Storage devices" to "சேமிப்பக சாதனங்கள்",
        "Refresh devices" to "சாதனங்களைப் புதுப்பி",
        "0 items" to "0 உருப்படிகள்",
        "Phone Memory" to "தொலைபேசி நினைவகம்",
        "SD Card" to "எஸ்டி கார்டு",
        "Move to Safe folder" to "பாதுகாப்பான கோப்புறைக்கு நகர்த்து",
        "Move to Trash" to "குப்பைக்கு நகர்த்து",
        "Create New Folder" to "புதிய கோப்புறையை உருவாக்கு",
        "Create a new folder in current directory:" to "தற்போதைய கோப்பகத்தில் புதிய கோப்புறையை உருவாக்கு:",
        "Folder Name" to "கோப்புறை பெயர்",
        "Create" to "உருவாக்கு",
        "Cancel" to "ரத்துசெய்",
        "Rename Folder" to "கோப்புறை பெயரை மாற்று",
        "New Name" to "புதிய பெயர்",
        "Folder renamed successfully" to "கோப்புறை பெயர் மாற்றப்பட்டது",
        "Failed to rename folder" to "கோப்புறை பெயரை மாற்ற முடியவில்லை",
        "Rename" to "பெயர் மாற்று",
        "Delete Folder?" to "கோப்புறையை நீக்கவா?",
        "Folder deleted" to "கோப்புறை நீக்கப்பட்டது",
        "Failed to delete folder" to "நீக்க முடியவில்லை",
        "Delete" to "நீக்கு"
    ),
    AppLanguage.TELUGU to mapOf(
        "Recent files" to "ఇటీవలి ఫైళ్లు",
        "See all >" to "అన్నీ చూడండి >",
        "Categories" to "వర్గాలు",
        "Collections" to "సేకరణలు",
        "Starred files" to "స్టార్ చేసిన ఫైళ్లు",
        "Safe folder" to "సురక్షిత ఫోల్డర్",
        "Trash" to "ట్రాష్",
        "Storage devices" to "స్టోరేజ్ పరికరాలు",
        "Refresh devices" to "రిఫ్రెష్ చేయండి",
        "0 items" to "0 అంశాలు",
        "Phone Memory" to "ఫోన్ మెమరీ",
        "SD Card" to "SD కార్డ్",
        "Move to Safe folder" to "సురక్షిత ఫోల్డర్‌కు తరలించు",
        "Move to Trash" to "ట్రాష్‌కు తరలించు",
        "Create New Folder" to "కొత్త ఫోల్డర్ సృష్టించు",
        "Create a new folder in current directory:" to "ప్రస్తుత డైరెక్టరీలో కొత్త ఫోల్డర్ సృష్టించు:",
        "Folder Name" to "ఫోల్డర్ పేరు",
        "Create" to "సృష్టించు",
        "Cancel" to "రద్దు చేయి",
        "Rename Folder" to "ఫోల్డర్ పేరు మార్చు",
        "New Name" to "కొత్త పేరు",
        "Folder renamed successfully" to "ఫోల్డర్ పేరు విజయవంతంగా మార్చబడింది",
        "Failed to rename folder" to "పేరు మార్చడం విఫలమైంది",
        "Rename" to "పేరు మార్చు",
        "Delete Folder?" to "ఫోల్డర్ తొలగించాలా?",
        "Folder deleted" to "ఫోల్డర్ తొలగించబడింది",
        "Failed to delete folder" to "తొలగించడం విఫలమైంది",
        "Delete" to "తొలగించు"
    ),
    AppLanguage.GUJARATI to mapOf(
        "Recent files" to "તાજેતરની ફાઇલો",
        "See all >" to "બધું જુઓ >",
        "Categories" to "કેટેગરીઝ",
        "Collections" to "સંગ્રહ",
        "Starred files" to "તારાંકિત ફાઇલો",
        "Safe folder" to "સુરક્ષિત ફોલ્ડર",
        "Trash" to "ટ્રેશ",
        "Storage devices" to "સ્ટોરેજ ઉપકરણો",
        "Refresh devices" to "રીફ્રેશ કરો",
        "0 items" to "0 આઇટમ",
        "Phone Memory" to "ફોન મેમરી",
        "SD Card" to "એસડી કાર્ડ",
        "Move to Safe folder" to "સુરક્ષિત ફોલ્ડરમાં ખસેડો",
        "Move to Trash" to "ટ્રેશમાં ખસેડો",
        "Create New Folder" to "નવું ફોલ્ડર બનાવો",
        "Create a new folder in current directory:" to "વર્તમાન ડિરેક્ટરીમાં નવું ફોલ્ડર બનાવો:",
        "Folder Name" to "ફોલ્ડરનું નામ",
        "Create" to "બનાવો",
        "Cancel" to "રદ કરો",
        "Rename Folder" to "ફોલ્ડરનું નામ બદલો",
        "New Name" to "નવું નામ",
        "Folder renamed successfully" to "ફોલ્ડરનું નામ સફળતાપૂર્વક બદલાયું",
        "Failed to rename folder" to "નામ બદલવામાં નિષ્ફળ",
        "Rename" to "નામ બદલો",
        "Delete Folder?" to "ફોલ્ડર કાઢી નાખવું છે?",
        "Folder deleted" to "ફોલ્ડર કાઢી નાખવામાં આવ્યું",
        "Failed to delete folder" to "કાઢી નાખવામાં નિષ્ફળ",
        "Delete" to "કાઢી નાખો"
    ),
    AppLanguage.PUNJABI to mapOf(
        "Recent files" to "ਹਾਲੀਆ ਫਾਈਲਾਂ",
        "See all >" to "ਸਭ ਦੇਖੋ >",
        "Categories" to "ਸ਼੍ਰੇਣੀਆਂ",
        "Collections" to "ਸੰਗ੍ਰਹਿ",
        "Starred files" to "ਤਾਰਾ ਚਿੰਨ੍ਹਿਤ ਫਾਈਲਾਂ",
        "Safe folder" to "ਸੁਰੱਖਿਅਤ ਫੋਲਡਰ",
        "Trash" to "ਟ੍ਰੈਸ਼",
        "Storage devices" to "ਸਟੋਰੇਜ ਡਿਵਾਈਸਾਂ",
        "Refresh devices" to "ਰਿਫ੍ਰੈਸ਼ ਕਰੋ",
        "0 items" to "0 ਆਈਟਮਾਂ",
        "Phone Memory" to "ਫ਼ੋਨ ਮੈਮੋਰੀ",
        "SD Card" to "SD ਕਾਰਡ",
        "Move to Safe folder" to "ਸੁਰੱਖਿਅਤ ਫੋਲਡਰ ਵਿੱਚ ਭੇਜੋ",
        "Move to Trash" to "ਟ੍ਰੈਸ਼ ਵਿੱਚ ਭੇਜੋ",
        "Create New Folder" to "ਨਵਾਂ ਫੋਲਡਰ ਬਣਾਓ",
        "Create a new folder in current directory:" to "ਮੌਜੂਦਾ ਡਾਇਰੈਕਟਰੀ ਵਿੱਚ ਨਵਾਂ ਫੋਲਡਰ ਬਣਾਓ:",
        "Folder Name" to "ਫੋਲਡਰ ਦਾ ਨਾਮ",
        "Create" to "ਬਣਾਓ",
        "Cancel" to "ਰੱਦ ਕਰੋ",
        "Rename Folder" to "ਫੋਲਡਰ ਦਾ ਨਾਮ ਬਦਲੋ",
        "New Name" to "ਨਵਾਂ ਨਾਮ",
        "Folder renamed successfully" to "ਫੋਲਡਰ ਦਾ ਨਾਮ ਸਫਲਤਾਪੂਰਵਕ ਬਦਲਿਆ ਗਿਆ",
        "Failed to rename folder" to "ਨਾਮ ਬਦਲਣ ਵਿੱਚ ਅਸਫਲ",
        "Rename" to "ਨਾਮ ਬਦਲੋ",
        "Delete Folder?" to "ਫੋਲਡਰ ਹਟਾਉਣਾ ਹੈ?",
        "Folder deleted" to "ਫੋਲਡਰ ਹਟਾਇਆ ਗਿਆ",
        "Failed to delete folder" to "ਹਟਾਉਣ ਵਿੱਚ ਅਸਫਲ",
        "Delete" to "ਹਟਾਓ"
    ),
    AppLanguage.FRENCH to mapOf(
        "Recent files" to "Fichiers récents",
        "See all >" to "Voir tout >",
        "Categories" to "Catégories",
        "Collections" to "Collections",
        "Starred files" to "Favoris",
        "Safe folder" to "Dossier sécurisé",
        "Trash" to "Corbeille",
        "Storage devices" to "Périphériques",
        "Refresh devices" to "Actualiser",
        "0 items" to "0 éléments",
        "Phone Memory" to "Mémoire interne",
        "SD Card" to "Carte SD",
        "Move to Safe folder" to "Sécuriser dans le dossier",
        "Move to Trash" to "Mettre à la corbeille",
        "Create New Folder" to "Créer un dossier",
        "Create a new folder in current directory:" to "Créer un dossier dans le répertoire actuel:",
        "Folder Name" to "Nom du dossier",
        "Create" to "Créer",
        "Cancel" to "Annuler",
        "Rename Folder" to "Renommer le dossier",
        "New Name" to "Nouveau nom",
        "Folder renamed successfully" to "Dossier renommé avec succès",
        "Failed to rename folder" to "Échec du renommage",
        "Rename" to "Renommer",
        "Delete Folder?" to "Supprimer le dossier?",
        "Folder deleted" to "Dossier supprimé",
        "Failed to delete folder" to "Échec de la suppression",
        "Delete" to "Supprimer"
    ),
    AppLanguage.ARABIC to mapOf(
        "Recent files" to "الملفات الأخيرة",
        "See all >" to "عرض الكل >",
        "Categories" to "الفئات",
        "Collections" to "المجموعات",
        "Starred files" to "الملفات المميزة",
        "Safe folder" to "المجلد الآمن",
        "Trash" to "سلة المهملات",
        "Storage devices" to "أجهزة التخزين",
        "Refresh devices" to "تحديث الأجهزة",
        "0 items" to "٠ عنصر",
        "Phone Memory" to "ذاكرة الهاتف",
        "SD Card" to "بطاقة SD",
        "Move to Safe folder" to "نقل إلى المجلد الآمن",
        "Move to Trash" to "نقل إلى سلة المهملات",
        "Create New Folder" to "إنشاء مجلد جديد",
        "Create a new folder in current directory:" to "إنشاء مجلد جديد في المجلد الحالي:",
        "Folder Name" to "اسم المجلد",
        "Create" to "إنشاء",
        "Cancel" to "إلغاء",
        "Rename Folder" to "إعادة تسمية المجلد",
        "New Name" to "الاسم الجديد",
        "Folder renamed successfully" to "تم إعادة تسمية المجلد بنجاح",
        "Failed to rename folder" to "فشلت إعادة تسمية المجلد",
        "Rename" to "إعادة تسمية",
        "Delete Folder?" to "حذف المجلد؟",
        "Folder deleted" to "تم حذف المجلد",
        "Failed to delete folder" to "فشل حذف المجلد",
        "Delete" to "حذف"
    )
)
