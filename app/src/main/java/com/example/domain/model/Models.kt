package com.example.domain.model

enum class AuthMethod(val displayName: String, val description: String) {
    PIN("PIN", "Quick numeric passcode (4-8 digits)"),
    PATTERN("Pattern", "Draw a connected pattern on a 3x3 grid"),
    PASSWORD("Password", "Alphanumeric passphrase")
}

enum class RelockPolicy(val displayName: String, val seconds: Int) {
    IMMEDIATE("Immediately on leave", 0),
    GRACE_30S("30 seconds grace", 30),
    GRACE_1M("1 minute grace", 60),
    GRACE_5M("5 minutes grace", 300),
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
    MONOCHROME("OLED Monochrome", "Pure black, slate and minimal silver")
}

enum class AppCategory {
    ALL,
    GAMES,
    APPS,
    PROTECTED,
    UNPROTECTED
}

data class AppProtection(
    val packageName: String,
    val appName: String,
    val isProtected: Boolean,
    val isSystemApp: Boolean = false,
    val isGame: Boolean = false,
    val categoryName: String = "App",
    val installTime: Long = 0L,
    val versionName: String = ""
)

data class UserSecuritySettings(
    val isSetupComplete: Boolean = false,
    val authMethod: AuthMethod = AuthMethod.PIN,
    val biometricEnabled: Boolean = false,
    val relockPolicy: RelockPolicy = RelockPolicy.IMMEDIATE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val expressivePalette: ExpressivePalette = ExpressivePalette.DYNAMIC,
    val dynamicColorEnabled: Boolean = true,
    val compactModeEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val protectionPaused: Boolean = false,
    val failedAttempts: Int = 0,
    val lockOutUntilTimestamp: Long = 0L
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
    val lastCheckedTimestamp: Long
) {
    val isProtectionReady: Boolean
        get() = usageAccessGranted
}
