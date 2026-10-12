package com.example.data.repository

import android.content.Context
import com.example.domain.model.IntruderCapture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object IntruderGalleryRepository {

    private val _captures = MutableStateFlow<List<IntruderCapture>>(emptyList())
    val captures: StateFlow<List<IntruderCapture>> = _captures.asStateFlow()

    suspend fun loadCaptures(context: Context) = withContext(Dispatchers.IO) {
        val intruderDir = File(context.filesDir, "intruders")
        if (!intruderDir.exists()) {
            _captures.value = emptyList()
            return@withContext
        }

        val files = intruderDir.listFiles { file ->
            file.isFile && (file.name.endsWith(".jpg", ignoreCase = true) || file.name.endsWith(".png", ignoreCase = true))
        } ?: emptyArray()

        val list = files.map { file ->
            val timestamp = file.lastModified()
            // Extract package name from filename if available: intruder_yyyyMMdd_HHmmss_SSS_package_name.jpg
            val nameParts = file.nameWithoutExtension.split("_")
            val targetPkg = if (nameParts.size >= 4) {
                nameParts.drop(3).joinToString(".")
            } else {
                "Protected App"
            }

            IntruderCapture(
                id = file.name,
                filePath = file.absolutePath,
                timestamp = timestamp,
                targetPackage = targetPkg,
                targetAppName = targetPkg.substringAfterLast(".").capitalize(),
                failedAttempts = 3
            )
        }.sortedByDescending { it.timestamp }

        _captures.value = list
    }

    suspend fun recordCapture(capture: IntruderCapture) {
        val current = _captures.value.toMutableList()
        current.add(0, capture)
        _captures.value = current
    }

    suspend fun deleteCapture(context: Context, capture: IntruderCapture) = withContext(Dispatchers.IO) {
        val file = File(capture.filePath)
        if (file.exists()) {
            file.delete()
        }
        val current = _captures.value.toMutableList()
        current.removeAll { it.id == capture.id }
        _captures.value = current
    }

    suspend fun clearAllCaptures(context: Context) = withContext(Dispatchers.IO) {
        val intruderDir = File(context.filesDir, "intruders")
        if (intruderDir.exists()) {
            intruderDir.listFiles()?.forEach {
                if (it.name != ".nomedia") {
                    it.delete()
                }
            }
        }
        _captures.value = emptyList()
    }
}
