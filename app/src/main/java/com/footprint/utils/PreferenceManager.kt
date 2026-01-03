package com.footprint.utils

import android.content.Context
import android.content.SharedPreferences
import com.footprint.ui.theme.ThemeMode

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("footprint_prefs", Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() {
            val name = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
            return try {
                ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }
        set(value) {
            prefs.edit().putString("theme_mode", value.name).apply()
        }

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean("is_first_launch", true)
        set(value) = prefs.edit().putBoolean("is_first_launch", value).apply()

    var nickname: String
        get() = prefs.getString("user_nickname", "旅行者") ?: "旅行者"
        set(value) = prefs.edit().putString("user_nickname", value).apply()

    var avatarId: String
        get() = prefs.getString("user_avatar_id", "avatar_1") ?: "avatar_1"
        set(value) = prefs.edit().putString("user_avatar_id", value).apply()

    var themeStyle: com.footprint.ui.theme.AppThemeStyle
        get() {
            val name = prefs.getString("theme_style", com.footprint.ui.theme.AppThemeStyle.CLASSIC.name)
            return try {
                com.footprint.ui.theme.AppThemeStyle.valueOf(name ?: com.footprint.ui.theme.AppThemeStyle.CLASSIC.name)
            } catch (e: Exception) {
                com.footprint.ui.theme.AppThemeStyle.CLASSIC
            }
        }
        set(value) {
            prefs.edit().putString("theme_style", value.name).apply()
        }

    var hasSeededV5: Boolean
        get() = prefs.getBoolean("has_seeded_v5", false)
        set(value) = prefs.edit().putBoolean("has_seeded_v5", value).apply()
}
