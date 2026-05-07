package com.seazon.feedus.data

import com.russhwolf.settings.Settings

class AppSettings {
    private val settings = Settings()
    private var appPreferences: AppPreferences? = null

    fun getAppPreferences(): AppPreferences {
        if (appPreferences == null) {
            appPreferences = AppPreferences(
                unreadMax = settings.getInt("unreadMax", 0),
                starredCount = settings.getInt("starredCount", 0),
                translationModelName = settings.getString("translationModelName", ""),
            )
        }
        return appPreferences!!
    }


    fun saveAppPreferences(appPreferences: AppPreferences) {
        this.appPreferences = appPreferences
        settings.putInt("unreadMax", appPreferences.unreadMax)
        settings.putInt("starredCount", appPreferences.starredCount)
        settings.putString("translationModelName", appPreferences.translationModelName)
    }

    fun clear() {
        settings.clear()
        appPreferences = null
    }
}

data class AppPreferences(
    val unreadMax: Int,
    val starredCount: Int,
    val translationModelName: String = "",
)