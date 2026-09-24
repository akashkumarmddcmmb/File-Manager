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
