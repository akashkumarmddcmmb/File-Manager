package com.example.update

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
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
