package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateResult {
    data class Success(
        val updateAvailable: Boolean,
        val currentVersion: String,
        val latestVersion: String,
        val changelog: String,
        val downloadUrl: String,
        val releasePageUrl: String
    ) : UpdateResult()
    object NoUpdate : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"

    /**
     * Checks if a newer version of the app is available on GitHub releases.
     * @param context App context
     * @param githubRepo The GitHub repository path, e.g., "akashkumarmddcmmb/file-manager"
     */
    suspend fun checkUpdate(context: Context, githubRepo: String): UpdateResult = withContext(Dispatchers.IO) {
        val currentVersion = getAppVersionName(context)
        Log.d(TAG, "Current version: $currentVersion, checking repo: $githubRepo")

        try {
            val urlString = "https://api.github.com/repos/$githubRepo/releases/latest"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "FileManager-Update-Checker")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return@withContext UpdateResult.Error("Repository or Release not found on GitHub. Ensure your repo name is correct and has at least one Release.")
            } else if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext UpdateResult.Error("Failed to fetch update. Server response: $responseCode")
            }

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            val jsonObject = JSONObject(response.toString())
            val latestVersion = jsonObject.optString("tag_name", "").replace("v", "", ignoreCase = true).trim()
            val changelog = jsonObject.optString("body", "No changelog provided.")
            val releasePageUrl = jsonObject.optString("html_url", "")

            // Find APK download URL from assets
            var downloadUrl = ""
            val assetsArray: JSONArray? = jsonObject.optJSONArray("assets")
            if (assetsArray != null) {
                for (i in 0 until assetsArray.length()) {
                    val assetObj = assetsArray.getJSONObject(i)
                    val assetName = assetObj.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = assetObj.optString("browser_download_url", "")
                        break
                    }
                }
            }

            // Fallback to release page if no direct APK asset is attached to release
            if (downloadUrl.isEmpty()) {
                downloadUrl = releasePageUrl
            }

            val currentClean = currentVersion.replace("v", "", ignoreCase = true).trim()
            val isNew = isNewerVersion(currentClean, latestVersion)

            Log.d(TAG, "Latest version: $latestVersion, isNewer: $isNew, URL: $downloadUrl")

            UpdateResult.Success(
                updateAvailable = isNew,
                currentVersion = currentVersion,
                latestVersion = "v$latestVersion",
                changelog = changelog,
                downloadUrl = downloadUrl,
                releasePageUrl = releasePageUrl
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            UpdateResult.Error(e.localizedMessage ?: "Unknown network connection issue.")
        }
    }

    /**
     * Downloads an APK from [downloadUrl] and saves it to secure cache folder.
     * Updates [onProgress] with values from 0.0 to 1.0.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Download failed, server code: ${connection.responseCode}")
                return@withContext null
            }

            val fileLength = connection.contentLength
            val cacheDir = File(context.cacheDir, "updates")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            // Clean up any old update APKs first to save space
            cacheDir.listFiles()?.forEach { it.delete() }

            val apkFile = File(cacheDir, "update_release.apk")
            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val data = ByteArray(4096)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count
                        if (fileLength > 0) {
                            onProgress(total.toFloat() / fileLength.toFloat())
                        }
                        output.write(data, 0, count)
                    }
                    output.flush()
                }
            }
            return@withContext apkFile
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading APK: ${e.message}", e)
            null
        }
    }

    /**
     * Checks if the app is allowed to install packages.
     * If not, redirects the user to the Settings screen to enable it.
     * If allowed, triggers the install of [apkFile].
     * Returns true if permission is granted and installation is triggered.
     */
    fun checkPermissionAndInstall(context: Context, apkFile: File): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                // Request Permission by opening Settings
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch ACTION_MANAGE_UNKNOWN_APP_SOURCES: ${e.message}")
                }
                return false
            }
        }
        
        // We have permission (or SDK < 26), perform install directly!
        triggerInstall(context, apkFile)
        return true
    }

    /**
     * Installs the downloaded [apkFile] using FileProvider.
     */
    fun triggerInstall(context: Context, apkFile: File) {
        try {
            val apkUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Installation trigger failed: ${e.message}", e)
        }
    }

    /**
     * Gets the package version name of the current running app.
     */
    fun getAppVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * Compares two semantic version strings (e.g. "2.4.0" and "2.5.0")
     * Returns true if latest version is greater than current version.
     */
    fun isNewerVersion(current: String, latest: String): Boolean {
        try {
            val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
            val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }

            val maxLength = maxOf(currentParts.size, latestParts.size)
            for (i in 0 until maxLength) {
                val currentVal = if (i < currentParts.size) currentParts[i] else 0
                val latestVal = if (i < latestParts.size) latestParts[i] else 0

                if (latestVal > currentVal) return true
                if (latestVal < currentVal) return false
            }
        } catch (e: Exception) {
            // String comparison fallback
            return latest.compareTo(current, ignoreCase = true) > 0
        }
        return false
    }
}
