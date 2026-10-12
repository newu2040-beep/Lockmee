package com.example.platform.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.example.domain.model.AppLanguage

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

object StringResources {

    fun get(key: String, lang: AppLanguage): String {
        val map = when (lang) {
            AppLanguage.HINDI -> HindiStrings
            AppLanguage.NEPALI -> NepaliStrings
            AppLanguage.JAPANESE -> JapaneseStrings
            AppLanguage.GERMAN -> GermanStrings
            AppLanguage.SPANISH -> SpanishStrings
            AppLanguage.ARABIC -> ArabicStrings
            AppLanguage.ENGLISH -> EnglishStrings
        }
        return map[key] ?: EnglishStrings[key] ?: key
    }

    private val EnglishStrings = mapOf(
        "app_name" to "LockMe",
        "home" to "Home",
        "apps" to "Apps",
        "security" to "Security",
        "intruders" to "Intruders",
        "settings" to "Settings",
        "protection_status" to "App & Game Shield",
        "active_shield" to "Real-time Monitoring Active",
        "paused_shield" to "Protection Paused",
        "needs_setup" to "Action Required: Setup permissions",
        "choose_apps" to "Apps & Games",
        "security_center" to "Security Center",
        "change_pin" to "Change PIN Passcode",
        "relock_timer" to "Custom Relock Timer",
        "immediate" to "Immediately",
        "biometrics" to "Biometric / Face Unlock",
        "intruder_detection" to "Intruder Detection",
        "intruder_desc" to "Capture secret selfie when wrong passcode is entered",
        "theme_studio" to "Theme Studio",
        "language" to "System Language",
        "clear_all" to "Clear All",
        "delete" to "Delete",
        "cancel" to "Cancel",
        "confirm" to "Confirm",
        "unlock_to_continue" to "App Shield Active",
        "enter_pin" to "Enter PIN",
        "draw_pattern" to "Draw Pattern",
        "wrong_pin" to "Incorrect PIN. Try again.",
        "wrong_pattern" to "Incorrect Pattern. Try again.",
        "anti_uninstall" to "Anti-Uninstall (Device Admin)",
        "hide_launcher" to "Hide from Launcher"
    )

    private val HindiStrings = mapOf(
        "app_name" to "लॉकमी",
        "home" to "होम",
        "apps" to "ऐप्स",
        "security" to "सुरक्षा",
        "intruders" to "घुसपैठिए",
        "settings" to "सेटिंग्स",
        "protection_status" to "ऐप और गेम शील्ड",
        "active_shield" to "सक्रिय सुरक्षा सक्रिय है",
        "paused_shield" to "सुरक्षा रोकी गई",
        "needs_setup" to "अनुमतियों की आवश्यकता है",
        "choose_apps" to "ऐप्स और गेम्स चुनें",
        "security_center" to "सुरक्षा केंद्र",
        "change_pin" to "पिन पासवर्ड बदलें",
        "relock_timer" to "कस्टम री-लॉक टाइमर",
        "immediate" to "तुरंत",
        "biometrics" to "बायोमेट्रिक / फेस अनलॉक",
        "intruder_detection" to "घुसपैठिया पहचान",
        "intruder_desc" to "गलत पिन दर्ज करने पर सेल्फी लें",
        "theme_studio" to "थीम स्टूडियो",
        "language" to "भाषा चुनें",
        "clear_all" to "सभी हटाएं",
        "delete" to "हटाएं",
        "cancel" to "रद्द करें",
        "confirm" to "पुष्टि करें",
        "unlock_to_continue" to "सुरक्षित ऐप लॉक",
        "enter_pin" to "पिन दर्ज करें",
        "draw_pattern" to "पैटर्न बनाएं",
        "wrong_pin" to "गलत पिन, पुनः प्रयास करें",
        "wrong_pattern" to "गलत पैटर्न, पुनः प्रयास करें",
        "anti_uninstall" to "एंटी-अनइंस्टॉल सुरक्षा",
        "hide_launcher" to "ऐप छुपाएं"
    )

    private val NepaliStrings = mapOf(
        "app_name" to "लकमी",
        "home" to "गृह",
        "apps" to "एप्स",
        "security" to "सुरक्षा",
        "intruders" to "अनाधिकृत",
        "settings" to "सेटिङहरू",
        "protection_status" to "एप तथा गेम सुरक्षा",
        "active_shield" to "सुरक्षा सक्रिय छ",
        "paused_shield" to "सुरक्षा रोकियो",
        "needs_setup" to "अनुमति आवश्यक छ",
        "choose_apps" to "एप्स छान्नुहोस्",
        "security_center" to "सुरक्षा केन्द्र",
        "change_pin" to "पिन परिवर्तन गर्नुहोस्",
        "relock_timer" to "री-लक टाइमर",
        "immediate" to "तुरुन्तै",
        "biometrics" to "बायोमेट्रिक / फेस लक",
        "intruder_detection" to "घुसपैठ पहिचान",
        "intruder_desc" to "गलत प्रयासमा तस्बिर खिच्नुहोस्",
        "theme_studio" to "थिम स्टुडियो",
        "language" to "भाषा",
        "clear_all" to "सबै मेटाउनुहोस्",
        "delete" to "मेटाउनुहोस्",
        "cancel" to "रद्द गर्नुहोस्",
        "confirm" to "निश्चय गर्नुहोस्",
        "unlock_to_continue" to "खोल्न प्रमाणित गर्नुहोस्",
        "enter_pin" to "पिन हाल्नुहोस्",
        "draw_pattern" to "ढाँचा कोर्नुहोस्",
        "wrong_pin" to "गलत पिन। पुनः प्रयास गर्नुहोस्।",
        "wrong_pattern" to "गलत ढाँचा। पुनः प्रयास गर्नुहोस्।",
        "anti_uninstall" to "अनइन्स्टल सुरक्षा",
        "hide_launcher" to "एप लुकाउनुहोस्"
    )

    private val JapaneseStrings = mapOf(
        "app_name" to "LockMe",
        "home" to "ホーム",
        "apps" to "アプリ",
        "security" to "セキュリティ",
        "intruders" to "侵入者検知",
        "settings" to "設定",
        "protection_status" to "アプリ保護シールド",
        "active_shield" to "保護作動中",
        "paused_shield" to "保護停止中",
        "needs_setup" to "権限の設定が必要です",
        "choose_apps" to "保護するアプリ",
        "security_center" to "セキュリティセンター",
        "change_pin" to "暗証番号の変更",
        "relock_timer" to "再ロックタイマー",
        "immediate" to "即座に",
        "biometrics" to "生体認証 / 顔認証",
        "intruder_detection" to "不正アクセス撮影",
        "intruder_desc" to "誤入力時に自動で写真を撮影",
        "theme_studio" to "テーマスタジオ",
        "language" to "言語設定",
        "clear_all" to "すべて削除",
        "delete" to "削除",
        "cancel" to "キャンセル",
        "confirm" to "確定",
        "unlock_to_continue" to "ロック解除してください",
        "enter_pin" to "暗証番号を入力",
        "draw_pattern" to "パターンを入力",
        "wrong_pin" to "暗証番号が間違っています",
        "wrong_pattern" to "パターンが間違っています",
        "anti_uninstall" to "アンインストール防止",
        "hide_launcher" to "アプリアイコンを非表示"
    )

    private val GermanStrings = mapOf(
        "app_name" to "LockMe",
        "home" to "Startseite",
        "apps" to "Apps",
        "security" to "Sicherheit",
        "intruders" to "Eindringlinge",
        "settings" to "Einstellungen",
        "protection_status" to "App-Schutzschild",
        "active_shield" to "Echtzeit-Schutz aktiv",
        "paused_shield" to "Schutz pausiert",
        "needs_setup" to "Berechtigungen erforderlich",
        "choose_apps" to "Apps & Spiele auswählen",
        "security_center" to "Sicherheitszentrum",
        "change_pin" to "PIN ändern",
        "relock_timer" to "Sperr-Verzögerung",
        "immediate" to "Sofort",
        "biometrics" to "Biometrie / Face Unlock",
        "intruder_detection" to "Eindringlings-Erkennung",
        "intruder_desc" to "Foto bei Fehlversuchen aufnehmen",
        "theme_studio" to "Design-Studio",
        "language" to "Sprache",
        "clear_all" to "Alle löschen",
        "delete" to "Löschen",
        "cancel" to "Abbrechen",
        "confirm" to "Bestätigen",
        "unlock_to_continue" to "Zur Fortsetzung entsperren",
        "enter_pin" to "PIN eingeben",
        "draw_pattern" to "Muster zeichnen",
        "wrong_pin" to "Falsche PIN. Erneut versuchen.",
        "wrong_pattern" to "Falsches Muster.",
        "anti_uninstall" to "Deinstallationsschutz",
        "hide_launcher" to "Icon aus Drawer verstecken"
    )

    private val SpanishStrings = mapOf(
        "app_name" to "LockMe",
        "home" to "Inicio",
        "apps" to "Aplicaciones",
        "security" to "Seguridad",
        "intruders" to "Intrusos",
        "settings" to "Ajustes",
        "protection_status" to "Escudo de Aplicaciones",
        "active_shield" to "Protección activa",
        "paused_shield" to "Protección en pausa",
        "needs_setup" to "Permisos requeridos",
        "choose_apps" to "Elegir Apps y Juegos",
        "security_center" to "Centro de Seguridad",
        "change_pin" to "Cambiar PIN",
        "relock_timer" to "Tiempo de rebloqueo",
        "immediate" to "Inmediatamente",
        "biometrics" to "Biometría / Desbloqueo Facial",
        "intruder_detection" to "Captura de Intrusos",
        "intruder_desc" to "Foto secreta tras intentos fallidos",
        "theme_studio" to "Estudio de Temas",
        "language" to "Idioma del sistema",
        "clear_all" to "Borrar todo",
        "delete" to "Eliminar",
        "cancel" to "Cancelar",
        "confirm" to "Confirmar",
        "unlock_to_continue" to "Desbloquear para continuar",
        "enter_pin" to "Introducir PIN",
        "draw_pattern" to "Dibujar patrón",
        "wrong_pin" to "PIN incorrecto. Inténtelo de nuevo.",
        "wrong_pattern" to "Patrón incorrecto.",
        "anti_uninstall" to "Protección contra desinstalación",
        "hide_launcher" to "Ocultar del menú"
    )

    private val ArabicStrings = mapOf(
        "app_name" to "لوك مي",
        "home" to "الرئيسية",
        "apps" to "التطبيقات",
        "security" to "الأمان",
        "intruders" to "المتسللون",
        "settings" to "الإعدادات",
        "protection_status" to "درع حماية التطبيقات",
        "active_shield" to "المراقبة الفورية نشطة",
        "paused_shield" to "الحماية متوقفة مؤقتاً",
        "needs_setup" to "مطلوب منح الصلاحيات",
        "choose_apps" to "اختر التطبيقات والألعاب",
        "security_center" to "مركز الأمان",
        "change_pin" to "تغيير رمز PIN",
        "relock_timer" to "مؤقت إعادة القفل",
        "immediate" to "فوراً",
        "biometrics" to "البصمة / التعرف على الوجه",
        "intruder_detection" to "كشف المتسللين",
        "intruder_desc" to "التقاط صورة عند إدخال رمز خاطئ",
        "theme_studio" to "استوديو المظاهر",
        "language" to "لغة النظام",
        "clear_all" to "مسح الكل",
        "delete" to "حذف",
        "cancel" to "إلغاء",
        "confirm" to "تأكيد",
        "unlock_to_continue" to "قم بإلغاء القفل للمتابعة",
        "enter_pin" to "أدخل الرمز",
        "draw_pattern" to "ارسم النمط",
        "wrong_pin" to "رمز خاطئ. حاول مرة أخرى.",
        "wrong_pattern" to "نمط خاطئ.",
        "anti_uninstall" to "منع إلغاء التثبيت",
        "hide_launcher" to "إخفاء أيقونة التطبيق"
    )
}

@Composable
fun appString(key: String): String {
    val currentLang = LocalAppLanguage.current
    return StringResources.get(key, currentLang)
}
