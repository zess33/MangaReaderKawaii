package com.kawaii.mangareader.ui.theme

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.kawaii.mangareader.domain.model.AppCustomIcon

object AppIconManager {

    fun setAppIcon(context: Context, customIcon: AppCustomIcon) {
        val pm = context.packageManager
        val packageName = context.packageName

        AppCustomIcon.entries.forEach { icon ->
            val componentName = ComponentName(packageName, icon.aliasName)
            val newState = if (icon == customIcon) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            try {
                pm.setComponentEnabledSetting(
                    componentName,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
