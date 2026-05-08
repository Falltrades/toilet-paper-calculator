package com.stellasecret.smartshoppingcalculator

import android.content.Context
import android.widget.Toast

/**
 * JavaScript ↔ Kotlin bridge exposed as `window.AndroidBridge`.
 *
 * This file is a source-only copy of the app module's AndroidBridge, placed in
 * the tests/unit test source set so that AndroidBridgeTest can compile without
 * depending on the Android app module or BuildConfig.
 *
 * Differences from the production version:
 *  - @JavascriptInterface annotations are omitted (android.webkit is not on the
 *    JVM test classpath; the annotation is only needed by the Android WebView runtime)
 *  - VERSION_NAME is a constant instead of BuildConfig.VERSION_NAME
 */
class AndroidBridge(private val context: Context) {

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun isSystemDarkMode(): Boolean {
        val nightMode = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    fun getAppVersion(): String = VERSION_NAME

    companion object {
        // Mirrors BuildConfig.VERSION_NAME from the app module.
        // Keep in sync with app/build.gradle versionName.
        const val VERSION_NAME = "1.0.0"
    }
}
