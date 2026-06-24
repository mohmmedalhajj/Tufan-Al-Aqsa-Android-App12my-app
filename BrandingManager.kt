package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import java.io.File
import java.io.InputStream

object BrandingManager {
    private const val PREFS_NAME = "branding_prefs"
    private const val KEY_APP_NAME = "branding_app_name"
    private const val KEY_AGENCY_NAME = "branding_agency_name"
    private const val KEY_OWNER_NAME = "branding_owner_name"
    private const val KEY_LOGO_ENABLED = "branding_logo_enabled"
    private const val KEY_QAT_TYPES = "branding_qat_types"

    // Default values
    const val DEFAULT_APP_NAME = "وكالة طوفان الأقصى"
    const val DEFAULT_AGENCY_NAME = "وكالة طوفان الأقصى"
    const val DEFAULT_OWNER_NAME = "أحمد منصور"
    const val DEFAULT_QAT_TYPES = "عود صعدي, فراد صعدي"

    var appName: String by mutableStateOf(DEFAULT_APP_NAME)
        internal set
    var agencyName: String by mutableStateOf(DEFAULT_AGENCY_NAME)
        internal set
    var ownerName: String by mutableStateOf(DEFAULT_OWNER_NAME)
        internal set
    var qatTypes: String by mutableStateOf(DEFAULT_QAT_TYPES)
        internal set
    var isLogoEnabled: Boolean by mutableStateOf(false)
        internal set

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        appName = prefs.getString(KEY_APP_NAME, DEFAULT_APP_NAME) ?: DEFAULT_APP_NAME
        agencyName = prefs.getString(KEY_AGENCY_NAME, DEFAULT_AGENCY_NAME) ?: DEFAULT_AGENCY_NAME
        ownerName = prefs.getString(KEY_OWNER_NAME, DEFAULT_OWNER_NAME) ?: DEFAULT_OWNER_NAME
        qatTypes = prefs.getString(KEY_QAT_TYPES, DEFAULT_QAT_TYPES) ?: DEFAULT_QAT_TYPES
        val logoFile = File(context.filesDir, "custom_brand_logo.png")
        isLogoEnabled = prefs.getBoolean(KEY_LOGO_ENABLED, false) && logoFile.exists()
    }

    fun saveBranding(context: Context, newAppName: String, newAgencyName: String, newOwnerName: String, newQatTypes: String, logoStream: InputStream? = null): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        editor.putString(KEY_APP_NAME, newAppName)
        editor.putString(KEY_AGENCY_NAME, newAgencyName)
        editor.putString(KEY_OWNER_NAME, newOwnerName)
        editor.putString(KEY_QAT_TYPES, newQatTypes)
        
        if (logoStream != null) {
            val logoFile = File(context.filesDir, "custom_brand_logo.png")
            try {
                if (logoFile.exists()) {
                    logoFile.delete()
                }
                logoFile.outputStream().use { output ->
                    logoStream.copyTo(output)
                }
                editor.putBoolean(KEY_LOGO_ENABLED, true)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        
        editor.apply()
        initialize(context)
        return true
    }

    fun restoreDefaults(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        val logoFile = File(context.filesDir, "custom_brand_logo.png")
        if (logoFile.exists()) {
            logoFile.delete()
        }
        
        initialize(context)
    }

    fun getCustomLogoBitmap(context: Context): Bitmap? {
        if (!isLogoEnabled) return null
        val logoFile = File(context.filesDir, "custom_brand_logo.png")
        return if (logoFile.exists()) {
            try {
                BitmapFactory.decodeFile(logoFile.absolutePath)
            } catch (e: Exception) {
                null
            }
        } else null
    }
}
