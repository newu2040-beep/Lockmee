package com.example.domain.model

enum class AuthMethod(val displayName: String, val description: String) {
    PIN("PIN", "Quick numeric passcode (4-8 digits)"),
    PATTERN("Pattern", "Draw a connected pattern on a 3x3 grid"),
    PASSWORD("Password", "Alphanumeric passphrase")
}

enum class RelockPolicy(val displayName: String, val seconds: Int) {
    IMMEDIATE("Immediately on leave", 0),
    CUSTOM("Custom Relock Timer", -2),
    GRACE_15S("15 seconds grace", 15),
    GRACE_30S("30 seconds grace", 30),
    GRACE_1M("1 minute grace", 60),
    GRACE_3M("3 minutes grace", 180),
    GRACE_5M("5 minutes grace", 300),
    GRACE_10M("10 minutes grace", 600),
    SCREEN_OFF("When screen turns off", -1)
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ExpressivePalette(val displayName: String, val description: String) {
    DYNAMIC("Dynamic Material You", "Extracts tones from device wallpaper (Android 12+)"),
    INDIGO("Royal Indigo", "Material deep blue & violet expressive tones"),
    EMERALD("Emerald Cyber", "High-contrast jade security palette"),
    AMBER("Amber Sunset", "Warm expressive terracotta & gold palette"),
    MONOCHROME("OLED Monochrome", "Pure black, slate and minimal silver"),
    LAVENDER("Lavender Dream", "Soft soothing pastel purple and amethyst"),
    MINT("Fresh Mint", "Cool refreshing spearmint and teal"),
    OCEAN("Deep Ocean", "Vibrant ultramarine and azure marine tones"),
    MIDNIGHT("Midnight Blue", "Velvet dark twilight and sapphire"),
    ROSE("Crimson Rose", "Lush berry, petal magenta, and rubies"),
    SAGE("Earthy Sage", "Calm olive botanical leaves and muted greens"),
    PEACH("Warm Peach", "Playful soft coral, apricot, and sunny peach"),
    GRAPHITE("Sleek Graphite", "Charcoal industrial steel and metallic quartz"),
    AURORA("Northern Aurora", "Fluorescent polar violet and lime luminescence")
}

enum class LockscreenEffect(val displayName: String, val description: String) {
    BLUR_GLASS("Frosted Glass & Depth", "Modern blurred glass surface with soft specular highlights"),
    CYBER_SHIELD("Cyber Shield Grid", "Neon pulse outlines with high-tech security grid"),
    MINIMAL_OLED("Minimalist OLED", "Deep battery-saving pure contrast surface"),
    AURORA("Aurora Ambient Gradient", "Slow shifting glowing ambient organic aura")
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val isRtl: Boolean = false) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    NEPALI("ne", "Nepali", "नेपाली"),
    JAPANESE("ja", "Japanese", "日本語"),
    GERMAN("de", "German", "Deutsch"),
    SPANISH("es", "Spanish", "Español"),
    ARABIC("ar", "Arabic", "العربية", isRtl = true)
}

enum class AppCategory {
    ALL,
    GAMES,
    APPS,
    PROTECTED,
    UNPROTECTED
}

data class IntruderCapture(
    val id: String,
    val filePath: String,
    val timestamp: Long,
    val targetPackage: String,
    val targetAppName: String,
    val failedAttempts: Int
)

data class AppProtection(
    val packageName: String,
    val appName: String,
    val isProtected: Boolean,
    val isSystemApp: Boolean = false,
    val isGame: Boolean = false,
    val isFavorite: Boolean = false,
    val categoryName: String = "App",
    val customRelockSeconds: Int? = null,
    val installTime: Long = 0L,
    val versionName: String = ""
)

data class UserSecuritySettings(
    val isSetupComplete: Boolean = false,
    val authMethod: AuthMethod = AuthMethod.PIN,
    val biometricEnabled: Boolean = false,
    val relockPolicy: RelockPolicy = RelockPolicy.IMMEDIATE,
    val customRelockSeconds: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val expressivePalette: ExpressivePalette = ExpressivePalette.DYNAMIC,
    val lockscreenEffect: LockscreenEffect = LockscreenEffect.BLUR_GLASS,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val dynamicColorEnabled: Boolean = true,
    val compactModeEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val protectionPaused: Boolean = false,
    val failedAttempts: Int = 0,
    val lockOutUntilTimestamp: Long = 0L,
    // Intruder detection
    val intruderCaptureEnabled: Boolean = false,
    val intruderThresholdAttempts: Int = 3,
    // Device Admin & stealth
    val deviceAdminEnabled: Boolean = false,
    val hideFromLauncher: Boolean = false,
    val customAppAlias: String = "LockMe",
    val customLogoUri: String = ""
)

data class SecurityEvent(
    val id: String,
    val eventType: String,
    val timestamp: Long,
    val details: String,
    val isSuccess: Boolean
)

data class PermissionStatus(
    val usageAccessGranted: Boolean,
    val overlayGranted: Boolean,
    val notificationsGranted: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val deviceAdminGranted: Boolean,
    val cameraGranted: Boolean,
    val lastCheckedTimestamp: Long
) {
    val isProtectionReady: Boolean
        get() = usageAccessGranted
}
