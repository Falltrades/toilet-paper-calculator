package com.stellasecret.smartshoppingcalculator

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

/**
 * JavaScript ↔ Kotlin bridge exposed as `window.AndroidBridge`.
 *
 * Usage from JS:
 *   AndroidBridge.showToast("Saved!");
 *   const dark = AndroidBridge.isSystemDarkMode();
 */
class AndroidBridge(private val context: Context) {

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun isSystemDarkMode(): Boolean {
        val nightMode = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    @JavascriptInterface
    fun getAppVersion(): String = BuildConfig.VERSION_NAME
}
