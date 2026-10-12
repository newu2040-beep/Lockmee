package com.example.platform.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.preferences.SettingsDataStore

class LockMeForegroundService : Service() {

    private lateinit var settingsDataStore: SettingsDataStore
    private var screenReceiver: android.content.BroadcastReceiver? = null

    companion object {
        private const val CHANNEL_ID = "lockme_service_channel"
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, LockMeForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LockMeForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        settingsDataStore = SettingsDataStore(applicationContext)
        createNotificationChannel()

        val notification = buildServiceNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Register screen off receiver for SCREEN_OFF relock policy
        screenReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    AppLockEngine.onScreenOff()
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenReceiver, filter)

        // Start active engine
        AppLockEngine.startMonitoring(applicationContext, settingsDataStore)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLockEngine.startMonitoring(applicationContext, settingsDataStore)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
        AppLockEngine.stopMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LockMe App Protection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors foreground activity to protect your selected apps"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildServiceNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LockMe is Active")
            .setContentText("Your selected apps are shielded and protected")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
